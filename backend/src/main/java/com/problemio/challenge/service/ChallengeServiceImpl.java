package com.problemio.challenge.service;

import com.problemio.challenge.domain.Challenge;
import com.problemio.challenge.domain.ChallengeRanking;
import com.problemio.challenge.dto.ChallengeDto;
import com.problemio.challenge.dto.ChallengeRankingResponse;
import com.problemio.challenge.dto.ChallengeResultResponse;
import com.problemio.challenge.dto.ChallengeStartResponse;
import com.problemio.challenge.dto.LeaderboardResponse;
import com.problemio.challenge.mapper.ChallengeMapper;
import com.problemio.challenge.mapper.ChallengeRankingMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.dto.QuestionResponse;
import com.problemio.quiz.mapper.QuestionMapper;
import com.problemio.submission.domain.Submission;
import com.problemio.submission.dto.QuizAnswerResponse;
import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.mapper.SubmissionMapper;
import com.problemio.submission.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService {

    private final ChallengeMapper challengeMapper;
    private final SubmissionService submissionService;
    private final SubmissionMapper submissionMapper; // For general submission checks
    private final QuestionMapper questionMapper; 
    private final ChallengeRankingMapper challengeRankingMapper; // For challenge specific logic

    @Override
    @Transactional(readOnly = true)
    public List<ChallengeDto> getChallenges() {
        return challengeMapper.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChallengeDto getChallenge(Long challengeId) {
        Challenge challenge = challengeMapper.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND)); 
        return toDto(challenge);
    }

    @Override
    @Transactional
    public ChallengeStartResponse startChallenge(Long userId, Long challengeId) {
        Challenge challenge = challengeMapper.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        if (challenge.getEndAt() != null && LocalDateTime.now().isAfter(challenge.getEndAt())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Long submissionId = submissionService.createSubmission(challenge.getTargetQuizId(), userId, challengeId);

        List<QuestionResponse> questions = questionMapper.findByQuizId(challenge.getTargetQuizId()).stream()
                .map(q -> QuestionResponse.builder()
                        .id(q.getId())
                        .order(q.getQuestionOrder())
                        .imageUrl(q.getImageUrl())
                        .description(q.getDescription())
                        .build())
                .collect(Collectors.toList());

        Collections.shuffle(questions);


        return ChallengeStartResponse.builder()
                .challengeId(challenge.getId())
                .submissionId(submissionId)
                .challengeType(challenge.getChallengeType())
                .timeLimit(challenge.getTimeLimit())
                .questions(questions)
                .build();
    }

    @Override
    @Transactional
    public QuizAnswerResponse submitAnswer(Long userId, Long challengeId, QuizSubmissionRequest request) {
        Challenge challenge = challengeMapper.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // Time Attack Verification
        if ("TIME_ATTACK".equals(challenge.getChallengeType()) && request.getSubmissionId() != null) {
             Submission submission = submissionMapper.findById(request.getSubmissionId())
                     .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
             
             if (submission.getSubmittedAt() != null) {
                 long diffSeconds = java.time.Duration.between(submission.getSubmittedAt(), LocalDateTime.now()).getSeconds();
                 // 5s buffer for network latency
                 if (diffSeconds > challenge.getTimeLimit() + 5) { 
                     throw new BusinessException(ErrorCode.ACCESS_DENIED); 
                 }
             }
        }

        return submissionService.submitQuiz(challenge.getTargetQuizId(), userId, request);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "ranking", key = "#challengeId + '-' + #userId")
    public ChallengeResultResponse getChallengeResult(Long userId, Long challengeId) {
        Submission submission = challengeRankingMapper.findSubmissionByUserIdAndChallengeId(userId, challengeId);
        
        if (submission == null) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        int rank = challengeRankingMapper.getLiveRanking(
            challengeId, 
            submission.getCorrectCount(), 
            submission.getPlayTime(), 
            submission.getSubmittedAt()
        );

        double playTimeSeconds = submission.getPlayTime() != null ? submission.getPlayTime() : 0.0;
        String formattedTime = String.format("%.3f", playTimeSeconds);

        return ChallengeResultResponse.builder()
                .challengeId(challengeId)
                .submissionId(submission.getId())
                .rank(rank)
                .correctCount(submission.getCorrectCount())
                .playTime(submission.getPlayTime())
                .formattedTime(formattedTime)
                .challengeType(challengeMapper.findById(challengeId).map(Challenge::getChallengeType).orElse("UNKNOWN"))
                .build();
    }

    @Override
    @Transactional
    public void finalizeChallenge(Long challengeId) {
        // 1. Get all submissions
        List<Submission> submissions = challengeRankingMapper.findSubmissionsByChallengeId(challengeId);

        if (submissions.isEmpty()) {
            return;
        }

        // 2. Filter Best Submission per User
        // Use a Map to keep the best one: Key=UserId
        java.util.Map<Long, Submission> bestSubmissionsMap = new java.util.HashMap<>();
        
        for (Submission s : submissions) {
            if (!bestSubmissionsMap.containsKey(s.getUserId())) {
                bestSubmissionsMap.put(s.getUserId(), s);
            } else {
                Submission existing = bestSubmissionsMap.get(s.getUserId());
                // Compare: Correct DESC, PlayTime ASC, SubmittedAt ASC
                boolean isBetter = false;
                if (s.getCorrectCount() > existing.getCorrectCount()) {
                    isBetter = true;
                } else if (s.getCorrectCount() == existing.getCorrectCount()) {
                    if (s.getPlayTime() < existing.getPlayTime()) {
                        isBetter = true;
                    } else if (s.getPlayTime().equals(existing.getPlayTime())) {
                        if (s.getSubmittedAt().isBefore(existing.getSubmittedAt())) {
                            isBetter = true;
                        }
                    }
                }
                
                if (isBetter) {
                    bestSubmissionsMap.put(s.getUserId(), s);
                }
            }
        }
        
        List<Submission> bestSubmissions = new java.util.ArrayList<>(bestSubmissionsMap.values());

        // 3. Sort Best Submissions
        bestSubmissions.sort((s1, s2) -> {
            if (s1.getCorrectCount() != s2.getCorrectCount()) {
                return s2.getCorrectCount() - s1.getCorrectCount(); 
            }
            if (!s1.getPlayTime().equals(s2.getPlayTime())) {
                return Double.compare(s1.getPlayTime(), s2.getPlayTime());
            }
            return s1.getSubmittedAt().compareTo(s2.getSubmittedAt()); 
        });

        // 3. Prepare Ranking Entities
        List<ChallengeRanking> rankings = new java.util.ArrayList<>();
        for (int i = 0; i < bestSubmissions.size(); i++) {
            Submission s = bestSubmissions.get(i);
            
            ChallengeRanking ranking = new ChallengeRanking();
            ranking.setChallengeId(challengeId);
            ranking.setUserId(s.getUserId());
            ranking.setRanking(i + 1);
            ranking.setScore((double) s.getCorrectCount());
            ranking.setPlayTime(s.getPlayTime());
            ranking.setCreatedAt(LocalDateTime.now());
            
            rankings.add(ranking);
        }

        // 4. Reset and Insert
        challengeRankingMapper.deleteRankingsByChallengeId(challengeId);
        challengeRankingMapper.insertRankings(rankings);
    }

    @Override
    @Transactional // removed readOnly=true because it might trigger lazy finalization (write)
    @Cacheable(value = "leaderboard", key = "#challengeId")
    public List<ChallengeRankingResponse> getTopRankings(Long challengeId) {
        // Enriched DTOs with challengeType
        List<ChallengeRankingResponse> topRankings = resolveTopRankings(challengeId);
        
        String type = challengeMapper.findById(challengeId).map(Challenge::getChallengeType).orElse("UNKNOWN");
        topRankings.forEach(r -> r.setChallengeType(type));
        
        return topRankings;
    }

    @Override
    @Transactional // removed readOnly=true
    public LeaderboardResponse getLeaderboard(Long challengeId, Long userId) {
        // 1. Top Rankings (Lazy Finalization if needed)
        List<ChallengeRankingResponse> topRankings = resolveTopRankings(challengeId);
        
        String type = challengeMapper.findById(challengeId).map(Challenge::getChallengeType).orElse("UNKNOWN");
        topRankings.forEach(r -> r.setChallengeType(type));

        // 2. My Ranking
        ChallengeRankingResponse myRanking = null;
        if (userId != null) {
            myRanking = findMyBestRanking(userId, challengeId, type);
        }
        
        // Return 0-score ranking if user has no record (User request: "10등 + 내 기록 0점")
        if (myRanking == null) {
             myRanking = ChallengeRankingResponse.builder()
                .challengeId(challengeId)
                .userId(userId) // Can be null if guest
                .ranking(0)
                .score(0.0)
                .playTime(0.0)
                .nickname(userId == null ? "Guest" : "Me")
                .challengeType(type)
                .build();
        }

        return LeaderboardResponse.builder()
                .topRankings(topRankings)
                .myRanking(myRanking)
                .build();
    }
    
    // Unifies logic for finding "My Ranking" whether Active or Archived
    private ChallengeRankingResponse findMyBestRanking(Long userId, Long challengeId, String challengeType) {
         boolean isExpired = isChallengeExpired(challengeId);
         
         if (isExpired) {
             // 1. Try Archive
             ChallengeRankingResponse ranking = challengeRankingMapper.loginUserRanking(userId, challengeId);
             if (ranking != null) {
                 ranking.setChallengeType(challengeType);
                 return ranking;
             }
             // If not in archive, fallback to Live logic below? 
             // Logic: If finalizeChallenge ran, it used live submissions. 
             // If "lazy finalization" happens in resolveTopRankings just before this, key submissions are in archive.
             // If user was duplicate or somehow missed, checking live is safer fallback?
             // Actually if finalized, Archive IS the source of truth.
             // But for safety/robustness, if Archive is empty for user, maybe they are not in valid set.
         }
         
         // 2. Try Live (Active or Fallback)
         Submission s = challengeRankingMapper.findSubmissionByUserIdAndChallengeId(userId, challengeId);
         if (s != null) {
             int rank = challengeRankingMapper.getLiveRanking(
                 challengeId, 
                 s.getCorrectCount(), 
                 s.getPlayTime(), 
                 s.getSubmittedAt()
             );
             
             return ChallengeRankingResponse.builder()
                     .challengeId(challengeId)
                     .userId(userId)
                     .ranking(rank)
                     .nickname("Me")
                     .score((double) s.getCorrectCount())
                     .playTime(s.getPlayTime())
                     .challengeType(challengeType)
                     .build();
         }
         
         return null;
    }
    
    // Helper method for Lazy Finalization
    private List<ChallengeRankingResponse> resolveTopRankings(Long challengeId) {
        Challenge challenge = challengeMapper.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));
        
        boolean isExpired = challenge.getEndAt() != null && LocalDateTime.now().isAfter(challenge.getEndAt());
        
        if (isExpired) {
            // Check if archived
            if (!challengeRankingMapper.existsByChallengeId(challengeId)) {
                // Lazy Finalization
                finalizeChallenge(challengeId);
            }
            // Return from Archive
            return challengeRankingMapper.challengeTotalRanking(challengeId, 10);
        } else {
            // Return from Live
            return challengeRankingMapper.findLiveTopRankingsByChallengeId(challengeId, 10);
        }
    }
    
    private boolean isChallengeExpired(Long challengeId) {
        Challenge challenge = challengeMapper.findById(challengeId).orElse(null);
        return challenge != null && challenge.getEndAt() != null && LocalDateTime.now().isAfter(challenge.getEndAt());
    }

    private final com.problemio.quiz.mapper.QuizMapper quizMapper;

    private ChallengeDto toDto(Challenge challenge) {
        // Fetch target quiz
        com.problemio.quiz.domain.Quiz quiz = quizMapper.findById(challenge.getTargetQuizId()).orElse(null);
        com.problemio.quiz.dto.QuizResponse quizResponse = null;
        
        if (quiz != null) {
            quizResponse = com.problemio.quiz.dto.QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .thumbnailUrl(quiz.getThumbnailUrl())
                // Basic fields enough for thumbnail display
                .build();
        }

        return ChallengeDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .challengeType(challenge.getChallengeType())
                .timeLimit(challenge.getTimeLimit())
                .startAt(challenge.getStartAt())
                .endAt(challenge.getEndAt())
                .targetQuiz(quizResponse)
                .build();
    }
}

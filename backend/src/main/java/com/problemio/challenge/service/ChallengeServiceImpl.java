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

        // 2. Sort: Correct DESC -> PlayTime ASC -> SubmittedAt ASC
        submissions.sort((s1, s2) -> {
            if (s1.getCorrectCount() != s2.getCorrectCount()) {
                return s2.getCorrectCount() - s1.getCorrectCount(); 
            }
            if (!s1.getPlayTime().equals(s2.getPlayTime())) {
                return s1.getPlayTime() - s2.getPlayTime(); 
            }
            return s1.getSubmittedAt().compareTo(s2.getSubmittedAt()); 
        });

        // 3. Prepare Ranking Entities
        List<ChallengeRanking> rankings = new java.util.ArrayList<>();
        for (int i = 0; i < submissions.size(); i++) {
            Submission s = submissions.get(i);
            
            ChallengeRanking ranking = new ChallengeRanking();
            ranking.setChallengeId(challengeId);
            ranking.setUserId(s.getUserId());
            ranking.setRanking(i + 1);
            ranking.setScore((double) s.getCorrectCount());
            ranking.setPlayTime(s.getPlayTime());
            
            rankings.add(ranking);
        }

        // 4. Reset and Insert
        challengeRankingMapper.deleteRankingsByChallengeId(challengeId);
        challengeRankingMapper.insertRankings(rankings);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "leaderboard", key = "#challengeId")
    public List<ChallengeRankingResponse> getTopRankings(Long challengeId) {
        List<ChallengeRankingResponse> topRankings = challengeRankingMapper.findLiveTopRankingsByChallengeId(challengeId, 10);
        
        String type = challengeMapper.findById(challengeId).map(Challenge::getChallengeType).orElse("UNKNOWN");
        // Enrich DTOs with challengeType locally if not fetched by query (Query fetches submission data mostly)
        // Actually mapper query SHOULD fetch it or we set it here.
        topRankings.forEach(r -> r.setChallengeType(type));
        
        return topRankings;
    }

    @Override
    @Transactional(readOnly = true)
    public LeaderboardResponse getLeaderboard(Long challengeId, Long userId) {
        // 1. Top Rankings (Reusing logic, calling mapper directly to avoid self-invocation proxy issues unless necessary)
        List<ChallengeRankingResponse> topRankings = challengeRankingMapper.findLiveTopRankingsByChallengeId(challengeId, 10);
        String type = challengeMapper.findById(challengeId).map(Challenge::getChallengeType).orElse("UNKNOWN");
        topRankings.forEach(r -> r.setChallengeType(type));

        // 2. My Ranking
        ChallengeRankingResponse myRanking = null;
        if (userId != null) {
             Submission s = challengeRankingMapper.findSubmissionByUserIdAndChallengeId(userId, challengeId);
                 
             if (s != null) {
                 int rank = challengeRankingMapper.getLiveRanking(
                     challengeId, 
                     s.getCorrectCount(), 
                     s.getPlayTime(), 
                     s.getSubmittedAt()
                 );
                 
                 myRanking = ChallengeRankingResponse.builder()
                         .challengeId(challengeId)
                         .userId(userId)
                         .ranking(rank)
                         .nickname("Me") // Placeholder, frontend has user info
                         .score((double) s.getCorrectCount())
                         .playTime(s.getPlayTime())
                         .challengeType(type)
                         .build();
             }
        }
        
        // Default empty ranking for guest or no-submission
        if (myRanking == null) {
             myRanking = ChallengeRankingResponse.builder()
                .ranking(0)
                .score(0.0)
                .playTime(0)
                .nickname(userId == null ? "Guest" : "Me")
                .challengeType(type)
                .build();
        }

        return LeaderboardResponse.builder()
                .topRankings(topRankings)
                .myRanking(myRanking)
                .build();
    }

    private ChallengeDto toDto(Challenge challenge) {
        return ChallengeDto.builder()
                .id(challenge.getId())
                .title(challenge.getTitle())
                .description(challenge.getDescription())
                .challengeType(challenge.getChallengeType())
                .timeLimit(challenge.getTimeLimit())
                .startAt(challenge.getStartAt())
                .endAt(challenge.getEndAt())
                .build();
    }
}

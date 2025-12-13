package com.problemio.submission.service;

import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.domain.Question;
import com.problemio.quiz.domain.QuestionAnswer;
import com.problemio.quiz.mapper.QuestionAnswerMapper;
import com.problemio.quiz.mapper.QuestionMapper;
import com.problemio.quiz.mapper.QuizMapper;
import com.problemio.submission.domain.Submission;
import com.problemio.submission.domain.SubmissionDetail;
import com.problemio.submission.dto.QuizAnswerResponse;
import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.dto.QuizSubmissionResponse;
import com.problemio.submission.mapper.SubmissionDetailMapper;
import com.problemio.submission.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionDetailMapper submissionDetailMapper;
    private final QuizMapper quizMapper;
    private final QuestionMapper questionMapper;
    private final QuestionAnswerMapper questionAnswerMapper;

    @Override
    @Transactional
    public QuizAnswerResponse submitQuiz(Long quizId, Long userIdOrNull, QuizSubmissionRequest request) {
        // 퀴즈 존재 여부 확인
        quizMapper.findById(quizId).orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 문제 검증 및 정답 목록 조회
        Question question = questionMapper.findById(request.getQuestionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
        if (!question.getQuizId().equals(quizId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        List<QuestionAnswer> answers = questionAnswerMapper.findByQuestionId(request.getQuestionId());
        boolean correct = isAnswerCorrect(request.getAnswerText(), answers);

        Submission submission = resolveSubmission(quizId, userIdOrNull, request.getSubmissionId());

        // 기존 제출이 있으면 삭제 후 새로 저장하여 정답 개수 재계산
        boolean hadCorrect = submissionDetailMapper.findBySubmissionIdAndQuestionId(submission.getId(), question.getId())
                .map(SubmissionDetail::isCorrect)
                .orElse(false);
        submissionDetailMapper.deleteBySubmissionIdAndQuestionId(submission.getId(), question.getId());

        SubmissionDetail detail = new SubmissionDetail();
        detail.setSubmissionId(submission.getId());
        detail.setQuestionId(question.getId());
        detail.setUserAnswer(request.getAnswerText());
        detail.setCorrect(correct);
        submissionDetailMapper.insertSubmissionDetail(detail);

        int correctCount = submission.getCorrectCount();
        if (hadCorrect && !correct) {
            correctCount -= 1;
        } else if (!hadCorrect && correct) {
            correctCount += 1;
        }
        
        // Challenge: Calculate and update playTime (submittedAt = challenge start time)
        if (submission.getSubmittedAt() != null) {
            long diffSeconds = java.time.Duration.between(submission.getSubmittedAt(), LocalDateTime.now()).getSeconds();
            submission.setPlayTime((int) diffSeconds);
            submissionMapper.updatePlayTime(submission.getId(), (int) diffSeconds);
        }

        submissionMapper.updateCorrectCount(submission.getId(), correctCount);

        int answeredCount = submissionDetailMapper.countBySubmissionId(submission.getId());
        submission.setCorrectCount(correctCount);

        return QuizAnswerResponse.builder()
                .submissionId(submission.getId())
                .questionId(question.getId())
                .correct(correct)
                .correctAnswer(answers.isEmpty() ? null : answers.get(0).getAnswerText())
                .correctAnswers(answers.stream().map(QuestionAnswer::getAnswerText).toList())
                .imageUrl(question.getImageUrl())
                .totalQuestions(submission.getTotalQuestions())
                .answeredCount(answeredCount)
                .correctCount(correctCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QuizSubmissionResponse getSubmissionResult(Long submissionId) {
        Submission submission = submissionMapper.findById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));

        int answeredCount = submissionDetailMapper.countBySubmissionId(submissionId);

        return QuizSubmissionResponse.builder()
                .submissionId(submission.getId())
                .totalQuestions(submission.getTotalQuestions())
                .correctCount(submission.getCorrectCount())
                .answeredCount(answeredCount)
                .build();
    }

    private Submission resolveSubmission(Long quizId, Long userIdOrNull, Long submissionId) {
        if (submissionId == null) {
            int totalQuestions = questionMapper.findByQuizId(quizId).size();

            Submission submission = new Submission();
            submission.setQuizId(quizId);
            submission.setUserId(userIdOrNull);
            submission.setTotalQuestions(totalQuestions);
            submission.setCorrectCount(0);
            submission.setSubmittedAt(LocalDateTime.now());
            submissionMapper.insertSubmission(submission);
            return submission;
        }

        Submission submission = submissionMapper.findById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));

        if (!submission.getQuizId().equals(quizId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return submission;
    }

    private boolean isAnswerCorrect(String userAnswer, List<QuestionAnswer> answers) {
        String normalizedUser = normalize(userAnswer);
        return answers.stream()
                .map(QuestionAnswer::getAnswerText)
                .map(this::normalize)
                .anyMatch(ans -> ans.equals(normalizedUser));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
    @Override
    @Transactional
    public Long createSubmission(Long quizId, Long userId, Long challengeId) {
        int totalQuestions = questionMapper.findByQuizId(quizId).size();

        Submission submission = new Submission();
        submission.setQuizId(quizId);
        submission.setUserId(userId);
        submission.setChallengeId(challengeId);
        submission.setTotalQuestions(totalQuestions);
        submission.setCorrectCount(0);
        submission.setSubmittedAt(LocalDateTime.now()); // Start time
        submission.setPlayTime(0);

        submissionMapper.insertSubmission(submission);
        return submission.getId();
    }
}

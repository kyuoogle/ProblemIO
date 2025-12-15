package com.problemio.submission.service;

import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.dto.QuizAnswerResponse;
import com.problemio.submission.dto.QuizSubmissionResponse;

public interface SubmissionService {

    QuizAnswerResponse submitQuiz(Long quizId, Long userIdOrNull, QuizSubmissionRequest request);

    QuizSubmissionResponse getSubmissionResult(Long submissionId);

    Long createSubmission(Long quizId, Long userId, Long challengeId);
}

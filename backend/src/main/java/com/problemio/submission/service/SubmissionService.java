package com.problemio.submission.service;

import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.dto.QuizSubmissionResponse;

public interface SubmissionService {

    QuizSubmissionResponse submitQuiz(Long quizId, Long userIdOrNull, QuizSubmissionRequest request);

    QuizSubmissionResponse getSubmissionResult(Long submissionId);
}

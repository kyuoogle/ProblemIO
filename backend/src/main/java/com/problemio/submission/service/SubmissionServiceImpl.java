package com.problemio.submission.service;

import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.dto.QuizSubmissionResponse;
import com.problemio.submission.mapper.SubmissionDetailMapper;
import com.problemio.submission.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionMapper submissionMapper;
    private final SubmissionDetailMapper submissionDetailMapper;

    @Override
    public QuizSubmissionResponse submitQuiz(Long quizId, Long userIdOrNull, QuizSubmissionRequest request) {
        // TODO
        return null;
    }

    @Override
    public QuizSubmissionResponse getSubmissionResult(Long submissionId) {
        // TODO
        return null;
    }
}

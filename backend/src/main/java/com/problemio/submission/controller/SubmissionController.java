package com.problemio.submission.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.dto.QuizSubmissionResponse;
import com.problemio.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes/{quizId}/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizSubmissionRequest request) {
        // TODO: userIdOrNull은 인증정보에서 가져오거나 null
        return null;
    }

    @GetMapping("/{submissionId}")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> getSubmissionResult(
            @PathVariable Long quizId,
            @PathVariable Long submissionId) {
        // TODO
        return null;
    }
}

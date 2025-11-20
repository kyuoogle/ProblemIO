package com.problemio.submission.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.submission.dto.QuizSubmissionRequest;
import com.problemio.submission.dto.QuizSubmissionResponse;
import com.problemio.submission.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/api/quizzes/{quizId}/submissions")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizSubmissionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: userIdOrNull은 인증정보에서 가져오거나 null (비회원 가능)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/api/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<QuizSubmissionResponse>> getSubmissionResult(
            @PathVariable Long submissionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 엔드포인트 경로 수정
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

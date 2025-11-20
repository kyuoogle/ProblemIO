package com.problemio.quiz.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.quiz.dto.*;
import com.problemio.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) String keyword) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @RequestBody @Valid QuizCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: userId는 인증 정보에서 가져오기
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody @Valid QuizUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: PUT 메서드 추가 (기존 PATCH는 유지)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuizPatch(
            @PathVariable Long quizId,
            @RequestBody QuizUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long quizId) {
        // TODO
        return null;
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> getQuiz(@PathVariable Long quizId) {
        // TODO
        return null;
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getPublicQuizzes() {
        // TODO
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getMyQuizzes() {
        // TODO
        return null;
    }

    @PostMapping("/{quizId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> likeQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 엔드포인트 경로 수정 (/likes -> /like)
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{quizId}/like")
    public ResponseEntity<ApiResponse<Map<String, Object>>> unlikeQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 엔드포인트 경로 수정 (/likes -> /like)
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

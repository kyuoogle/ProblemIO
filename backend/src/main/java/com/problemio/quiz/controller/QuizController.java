package com.problemio.quiz.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.quiz.dto.*;
import com.problemio.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @PostMapping
    public ResponseEntity<ApiResponse<QuizResponse>> createQuiz(
            @RequestBody @Valid QuizCreateRequest request) {
        // TODO: userId는 인증 정보에서 가져오기
        return null;
    }

    @PatchMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponse>> updateQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizUpdateRequest request) {
        // TODO
        return null;
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

    @PostMapping("/{quizId}/likes")
    public ResponseEntity<ApiResponse<Void>> likeQuiz(@PathVariable Long quizId) {
        // TODO
        return null;
    }

    @DeleteMapping("/{quizId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikeQuiz(@PathVariable Long quizId) {
        // TODO
        return null;
    }
}

package com.problemio.question.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.question.dto.QuestionAnswerDto;
import com.problemio.question.dto.QuestionCreateRequest;
import com.problemio.question.dto.QuestionUpdateRequest;
import com.problemio.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes/{quizId}/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> createQuestion(
            @PathVariable Long quizId,
            @RequestBody @Valid QuestionCreateRequest request,
            @RequestBody(required = false) List<QuestionAnswerDto> answers) {
        // TODO
        return null;
    }

    @PatchMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Void>> updateQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestBody QuestionUpdateRequest request,
            @RequestBody(required = false) List<QuestionAnswerDto> answers) {
        // TODO
        return null;
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long quizId,
            @PathVariable Long questionId) {
        // TODO
        return null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionCreateRequest>>> getQuestions(@PathVariable Long quizId) {
        // TODO
        return null;
    }
}

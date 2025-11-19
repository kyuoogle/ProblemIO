package com.problemio.comment.controller;

import com.problemio.comment.dto.CommentCreateRequest;
import com.problemio.comment.dto.CommentResponse;
import com.problemio.comment.service.CommentService;
import com.problemio.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes/{quizId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable Long quizId,
            @RequestBody @Valid CommentCreateRequest request) {
        // TODO: userId는 인증정보에서
        return null;
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Long quizId,
            @PathVariable Long commentId,
            @RequestBody @Valid CommentCreateRequest request) {
        // TODO
        return null;
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long quizId,
            @PathVariable Long commentId) {
        // TODO
        return null;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(@PathVariable Long quizId) {
        // TODO
        return null;
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long quizId,
            @PathVariable Long commentId) {
        // TODO
        return null;
    }

    @DeleteMapping("/{commentId}/likes")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable Long quizId,
            @PathVariable Long commentId) {
        // TODO
        return null;
    }
}

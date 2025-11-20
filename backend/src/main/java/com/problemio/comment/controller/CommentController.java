package com.problemio.comment.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.problemio.comment.dto.CommentCreateRequest;
import com.problemio.comment.dto.CommentResponse;
import com.problemio.comment.service.CommentService;
import com.problemio.global.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> likeComment(
            @PathVariable Long quizId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{commentId}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable Long quizId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

package com.problemio.comment.service;

import com.problemio.comment.dto.CommentCreateRequest;
import com.problemio.comment.dto.CommentResponse;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(Long quizId, Long userId, CommentCreateRequest request);

    CommentResponse updateComment(Long commentId, Long userId, CommentCreateRequest request);

    void deleteComment(Long commentId, Long userId);

    List<CommentResponse> getComments(Long quizId);

    void likeComment(Long commentId, Long userId);

    void unlikeComment(Long commentId, Long userId);
}

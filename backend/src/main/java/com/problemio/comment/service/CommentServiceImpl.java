package com.problemio.comment.service;

import com.problemio.comment.dto.CommentCreateRequest;
import com.problemio.comment.dto.CommentResponse;
import com.problemio.comment.mapper.CommentLikeMapper;
import com.problemio.comment.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;

    @Override
    public CommentResponse createComment(Long quizId, Long userId, CommentCreateRequest request) {
        // TODO
        return null;
    }

    @Override
    public CommentResponse updateComment(Long commentId, Long userId, CommentCreateRequest request) {
        // TODO
        return null;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        // TODO
    }

    @Override
    public List<CommentResponse> getComments(Long quizId) {
        // TODO
        return null;
    }

    @Override
    public void likeComment(Long commentId, Long userId) {
        // TODO
    }

    @Override
    public void unlikeComment(Long commentId, Long userId) {
        // TODO
    }
}

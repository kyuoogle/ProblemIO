package com.problemio.comment.mapper;

import com.problemio.comment.domain.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentMapper {

    void insertComment(Comment comment);

    void updateComment(Comment comment);

    void softDeleteComment(@Param("id") Long id);

    Optional<Comment> findById(@Param("id") Long id);

    List<Comment> findByQuizId(@Param("quizId") Long quizId);
}

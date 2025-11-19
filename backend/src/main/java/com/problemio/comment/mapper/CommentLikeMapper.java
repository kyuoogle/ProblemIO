package com.problemio.comment.mapper;

import com.problemio.comment.domain.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface CommentLikeMapper {

    void insertCommentLike(CommentLike like);

    void deleteCommentLike(@Param("userId") Long userId, @Param("commentId") Long commentId);

    Optional<CommentLike> findByUserIdAndCommentId(@Param("userId") Long userId,
                                                   @Param("commentId") Long commentId);

    int countByCommentId(@Param("commentId") Long commentId);
}

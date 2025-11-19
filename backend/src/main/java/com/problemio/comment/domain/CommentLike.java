package com.problemio.comment.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentLike {

    private Long userId;
    private Long commentId;
    private LocalDateTime createdAt;
}

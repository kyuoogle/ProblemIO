package com.problemio.comment.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Comment {

    private Long id;
    private Long quizId;
    private Long userId;
    private String content;
    private int likeCount;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

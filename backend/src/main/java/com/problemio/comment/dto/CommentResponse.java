package com.problemio.comment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CommentResponse {

    private Long id;
    private Long userId;
    private String content;
    private int likeCount;
    private boolean deleted;
    private LocalDateTime createdAt;
}

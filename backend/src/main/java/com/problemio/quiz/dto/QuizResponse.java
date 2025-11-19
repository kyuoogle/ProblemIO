package com.problemio.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResponse {

    private Long id;
    private Long userId;
    private String title;
    private String description;
    private String thumbnailUrl;
    private boolean isPublic;
    private int likeCount;
    private int playCount;
}

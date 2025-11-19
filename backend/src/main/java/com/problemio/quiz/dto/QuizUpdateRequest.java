package com.problemio.quiz.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizUpdateRequest {

    private String title;
    private String description;
    private String thumbnailUrl;
    private Boolean isPublic;
}

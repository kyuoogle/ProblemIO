package com.problemio.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizSummaryDto {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private int likeCount;
    private int playCount;
}

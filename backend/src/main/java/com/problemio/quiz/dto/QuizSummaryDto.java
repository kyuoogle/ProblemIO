package com.problemio.quiz.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSummaryDto {

    private Long id;
    private String title;
    private String thumbnailUrl;
    private int likeCount;
    private int playCount;

}

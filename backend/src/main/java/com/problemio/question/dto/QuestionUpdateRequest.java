package com.problemio.question.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionUpdateRequest {

    private Integer questionOrder;
    private String imageUrl;
    private String description;
}

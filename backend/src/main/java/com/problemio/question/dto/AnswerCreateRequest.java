package com.problemio.question.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerCreateRequest {
    private String answerText; //

    private Integer sortOrder;
}

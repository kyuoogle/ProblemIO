package com.problemio.question.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionAnswerDto {

    private Long id;
    private String answerText;
    private int sortOrder;
}

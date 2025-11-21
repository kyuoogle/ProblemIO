package com.problemio.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QuestionCreateRequest {

    private Integer questionOrder;

    @NotBlank
    private String imageUrl;

    private String description;

    private List<AnswerCreateRequest> answers;
}

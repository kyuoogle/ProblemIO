package com.problemio.question.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionCreateRequest {

    private int questionOrder;

    @NotBlank
    private String imageUrl;

    private String description;
}

package com.problemio.question.service;

import com.problemio.question.dto.QuestionAnswerDto;
import com.problemio.question.dto.QuestionCreateRequest;
import com.problemio.question.dto.QuestionUpdateRequest;

import java.util.List;

public interface QuestionService {

    void createQuestion(Long quizId, QuestionCreateRequest request, List<QuestionAnswerDto> answers);

    void updateQuestion(Long questionId, QuestionUpdateRequest request, List<QuestionAnswerDto> answers);

    void deleteQuestion(Long questionId);

    List<QuestionCreateRequest> getQuestionsByQuizId(Long quizId);
}

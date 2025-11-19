package com.problemio.question.service;

import com.problemio.question.dto.QuestionAnswerDto;
import com.problemio.question.dto.QuestionCreateRequest;
import com.problemio.question.dto.QuestionUpdateRequest;
import com.problemio.question.mapper.QuestionAnswerMapper;
import com.problemio.question.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;
    private final QuestionAnswerMapper questionAnswerMapper;

    @Override
    public void createQuestion(Long quizId, QuestionCreateRequest request, List<QuestionAnswerDto> answers) {
        // TODO
    }

    @Override
    public void updateQuestion(Long questionId, QuestionUpdateRequest request, List<QuestionAnswerDto> answers) {
        // TODO
    }

    @Override
    public void deleteQuestion(Long questionId) {
        // TODO
    }

    @Override
    public List<QuestionCreateRequest> getQuestionsByQuizId(Long quizId) {
        // TODO: 별도 Response DTO 만들어도 됨
        return null;
    }
}

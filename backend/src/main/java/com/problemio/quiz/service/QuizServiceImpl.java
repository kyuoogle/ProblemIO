package com.problemio.quiz.service;

import com.problemio.quiz.dto.*;
import com.problemio.quiz.mapper.QuizLikeMapper;
import com.problemio.quiz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizMapper quizMapper;
    private final QuizLikeMapper quizLikeMapper;

    @Override
    public QuizResponse createQuiz(Long userId, QuizCreateRequest request) {
        // TODO
        return null;
    }

    @Override
    public QuizResponse updateQuiz(Long userId, Long quizId, QuizUpdateRequest request) {
        // TODO
        return null;
    }

    @Override
    public void deleteQuiz(Long userId, Long quizId) {
        // TODO
    }

    @Override
    public QuizResponse getQuiz(Long quizId) {
        // TODO
        return null;
    }

    @Override
    public List<QuizSummaryDto> getPublicQuizzes() {
        // TODO
        return null;
    }

    @Override
    public List<QuizSummaryDto> getUserQuizzes(Long userId) {
        // TODO
        return null;
    }

    @Override
    public void likeQuiz(Long userId, Long quizId) {
        // TODO
    }

    @Override
    public void unlikeQuiz(Long userId, Long quizId) {
        // TODO
    }
}

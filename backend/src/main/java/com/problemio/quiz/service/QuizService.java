package com.problemio.quiz.service;

import com.problemio.quiz.dto.*;

import java.util.List;

public interface QuizService {

    QuizResponse createQuiz(Long userId, QuizCreateRequest request);

    QuizResponse updateQuiz(Long userId, Long quizId, QuizUpdateRequest request);

    void deleteQuiz(Long userId, Long quizId);

    QuizResponse getQuiz(Long quizId);

    List<QuizSummaryDto> getPublicQuizzes();

    List<QuizSummaryDto> getUserQuizzes(Long userId);

    void likeQuiz(Long userId, Long quizId);

    void unlikeQuiz(Long userId, Long quizId);
}

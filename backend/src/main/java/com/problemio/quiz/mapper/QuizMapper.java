package com.problemio.quiz.mapper;

import com.problemio.quiz.domain.Quiz;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QuizMapper {

    void insertQuiz(Quiz quiz);

    void updateQuiz(Quiz quiz);

    Optional<Quiz> findById(@Param("id") Long id);

    List<Quiz> findPublicQuizzes();

    List<Quiz> findQuizzesByUserId(@Param("userId") Long userId);

    void incrementPlayCount(@Param("id") Long id);
}

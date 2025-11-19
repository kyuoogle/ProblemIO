package com.problemio.quiz.mapper;

import com.problemio.quiz.domain.QuizLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface QuizLikeMapper {

    void insertQuizLike(QuizLike quizLike);

    void deleteQuizLike(@Param("userId") Long userId, @Param("quizId") Long quizId);

    Optional<QuizLike> findByUserIdAndQuizId(@Param("userId") Long userId, @Param("quizId") Long quizId);

    int countByQuizId(@Param("quizId") Long quizId);
}

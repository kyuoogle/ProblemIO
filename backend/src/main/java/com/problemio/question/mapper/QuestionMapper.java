package com.problemio.question.mapper;

import com.problemio.question.domain.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface QuestionMapper {

    void insertQuestion(Question question);

    void updateQuestion(Question question);

    void deleteQuestion(@Param("id") Long id);

    Optional<Question> findById(@Param("id") Long id);

    List<Question> findByQuizId(@Param("quizId") Long quizId);

    void deleteByQuizId(@Param("quizId") Long quizId);
}
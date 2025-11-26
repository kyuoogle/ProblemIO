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

    void deleteQuiz(@Param("id") Long id);

    Optional<Quiz> findById(@Param("id") Long id);

    List<Quiz> findPublicQuizzes();

    List<Quiz> findQuizzesByUserId(@Param("userId") Long userId);

    void incrementPlayCount(@Param("id") Long id);

    List<Quiz> searchQuizzes(@Param("offset") int offset,
                             @Param("size") int size,
                             @Param("sort") String sort,
                             @Param("keyword") String keyword);

    int countQuizzes(@Param("keyword") String keyword);

    void incrementLikeCount(@Param("id") Long id);

    void decrementLikeCount(@Param("id") Long id);

    // 퀴즈 작성자만 조회: 자기 퀴즈에 좋아요 못하게 막기 위함
    Long findUserIdByQuizId(@Param("id") Long id);
}

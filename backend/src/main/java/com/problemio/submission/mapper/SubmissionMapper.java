package com.problemio.submission.mapper;

import com.problemio.submission.domain.Submission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface SubmissionMapper {

    void insertSubmission(Submission submission);

    Optional<Submission> findById(@Param("id") Long id);

    List<Submission> findByQuizId(@Param("quizId") Long quizId);

    List<Submission> findByUserId(@Param("userId") Long userId);
}

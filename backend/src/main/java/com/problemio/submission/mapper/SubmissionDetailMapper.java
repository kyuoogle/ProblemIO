package com.problemio.submission.mapper;

import com.problemio.submission.domain.SubmissionDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SubmissionDetailMapper {

    void insertSubmissionDetail(SubmissionDetail detail);

    void insertSubmissionDetails(@Param("details") List<SubmissionDetail> details);

    List<SubmissionDetail> findBySubmissionId(@Param("submissionId") Long submissionId);
}

package com.problemio.follow.mapper;

import com.problemio.follow.domain.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {

    void insertFollow(Follow follow);

    void deleteFollow(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    List<Follow> findFollowers(@Param("userId") Long userId);

    List<Follow> findFollowings(@Param("userId") Long userId);
}

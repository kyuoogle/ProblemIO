package com.problemio.follow.service;

import com.problemio.user.dto.UserSummaryDto;

import java.util.List;

public interface FollowService {

    void follow(Long followerId, Long followingId);

    void unfollow(Long followerId, Long followingId);

    List<UserSummaryDto> getFollowers(Long userId);

    List<UserSummaryDto> getFollowings(Long userId);
}

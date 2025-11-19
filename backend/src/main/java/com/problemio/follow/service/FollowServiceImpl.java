package com.problemio.follow.service;

import com.problemio.follow.mapper.FollowMapper;
import com.problemio.user.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    @Override
    public void follow(Long followerId, Long followingId) {
        // TODO
    }

    @Override
    public void unfollow(Long followerId, Long followingId) {
        // TODO
    }

    @Override
    public List<UserSummaryDto> getFollowers(Long userId) {
        // TODO
        return null;
    }

    @Override
    public List<UserSummaryDto> getFollowings(Long userId) {
        // TODO
        return null;
    }
}

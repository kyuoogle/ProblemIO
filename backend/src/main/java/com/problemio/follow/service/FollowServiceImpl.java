package com.problemio.follow.service;

import com.problemio.follow.domain.Follow;
import com.problemio.follow.dto.FollowUserDto;
import com.problemio.follow.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowMapper followMapper;

    @Override
    @Transactional
    public void follow(Long followerId, Long targetUserId) {
        if (followerId.equals(targetUserId)) {
            // 자기 자신 팔로우 방지
            return;
        }

        int exists = followMapper.exists(followerId, targetUserId);
        if (exists > 0) {
            // 이미 팔로우 중이면 무시 (또는 예외 던져도 됨)
            return;
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(targetUserId)
                .build();

        followMapper.insert(follow);
    }

    @Override
    @Transactional
    public void unfollow(Long followerId, Long targetUserId) {
        followMapper.delete(followerId, targetUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long targetUserId) {
        return followMapper.exists(followerId, targetUserId) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowers(Long userId) {
        return followMapper.findFollowers(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowUserDto> getFollowings(Long userId) {
        return followMapper.findFollowings(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getFollowerCount(Long userId) {
        return followMapper.countFollowers(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public int getFollowingCount(Long userId) {
        return followMapper.countFollowings(userId);
    }
}

// src/main/java/com/problemio/follow/controller/FollowController.java
package com.problemio.follow.controller;

import com.problemio.follow.dto.FollowUserDto;
import com.problemio.follow.service.FollowService;
import com.problemio.global.common.ApiResponse;
import com.problemio.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    // 팔로우
    @PostMapping("/{targetUserId}")
    public ApiResponse<Void> follow(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @PathVariable Long targetUserId) {
        Long loginUserId = userDetails.getUser().getId(); // 프로젝트에 맞게 수정
        followService.follow(loginUserId, targetUserId);
        return ApiResponse.success(null);
    }

    // 언팔로우
    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> unfollow(@AuthenticationPrincipal CustomUserDetails userDetails,
                                      @PathVariable Long targetUserId) {
        Long loginUserId = userDetails.getUser().getId(); // 프로젝트에 맞게 수정
        followService.unfollow(loginUserId, targetUserId);
        return ApiResponse.success(null);
    }

    // 내가 이 사람을 팔로우하고 있는지 여부
    @GetMapping("/{targetUserId}/status")
    public ApiResponse<Boolean> isFollowing(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long targetUserId) {
        Long loginUserId = userDetails.getUser().getId();
        boolean result = followService.isFollowing(loginUserId, targetUserId);
        return ApiResponse.success(result);
    }

    // 특정 유저의 팔로워 목록
    @GetMapping("/{userId}/followers")
    public ApiResponse<List<FollowUserDto>> getFollowers(@PathVariable Long userId) {
        return ApiResponse.success(followService.getFollowers(userId));
    }

    // 특정 유저가 팔로우하는 목록
    @GetMapping("/{userId}/followings")
    public ApiResponse<List<FollowUserDto>> getFollowings(@PathVariable Long userId) {
        return ApiResponse.success(followService.getFollowings(userId));
    }
}

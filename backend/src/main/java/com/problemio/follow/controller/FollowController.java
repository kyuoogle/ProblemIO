package com.problemio.follow.controller;

import com.problemio.follow.service.FollowService;
import com.problemio.global.common.ApiResponse;
import com.problemio.user.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Map<String, Object>>> follow(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: followerId는 인증 정보에서
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{userId}/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 내 팔로워 목록
    @GetMapping("/me/followers")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getMyFollowers(
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 내 팔로잉 목록
    @GetMapping("/me/followings")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getMyFollowings(
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

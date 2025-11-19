package com.problemio.follow.controller;

import com.problemio.follow.service.FollowService;
import com.problemio.global.common.ApiResponse;
import com.problemio.user.dto.UserSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> follow(@PathVariable Long userId) {
        // TODO: followerId는 인증 정보에서
        return null;
    }

    @DeleteMapping("/follow")
    public ResponseEntity<ApiResponse<Void>> unfollow(@PathVariable Long userId) {
        // TODO
        return null;
    }

    @GetMapping("/followers")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getFollowers(@PathVariable Long userId) {
        // TODO
        return null;
    }

    @GetMapping("/followings")
    public ResponseEntity<ApiResponse<List<UserSummaryDto>>> getFollowings(@PathVariable Long userId) {
        // TODO
        return null;
    }
}

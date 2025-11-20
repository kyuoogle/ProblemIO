package com.problemio.user.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.problemio.user.dto.UserSummaryDto;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 인증 유저 기반
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UserResponse request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 마이페이지 상단 요약
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getMySummary(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 내가 만든 퀴즈 목록
    @GetMapping("/me/quizzes")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getMyQuizzes(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 내가 좋아요한 퀴즈 목록
    @GetMapping("/me/liked-quizzes")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getMyLikedQuizzes(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 팔로잉 유저들의 퀴즈 목록
    @GetMapping("/me/following-quizzes")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getFollowingQuizzes(
            @RequestParam(defaultValue = "latest") String sort,
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

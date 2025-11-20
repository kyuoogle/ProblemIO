package com.problemio.user.controller;

import com.problemio.global.auth.CustomUserDetails;
import com.problemio.global.common.ApiResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSummaryDto;
import com.problemio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 특정 유저 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userId)));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(userDetails.getUser().getId())));
    }

    // 프로필 수정
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestBody UserResponse request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, request)));
    }

    // 비밀번호 변경
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        userService.changePassword(userId, request.get("oldPassword"), request.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 탈퇴 (비밀번호 확인을 위해 Body로 받거나 Header로 받음. 여기선 Body로 가정)
    @PostMapping("/me/withdraw")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        userService.deleteAccount(userId, request.get("password"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 마이페이지 상단 요약
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getMySummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        return ResponseEntity.ok(ApiResponse.success(userService.getMySummary(userId)));
    }

    // (이하 퀴즈 관련 메서드는 동일)
}
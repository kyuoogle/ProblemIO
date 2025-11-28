package com.problemio.user.controller;

import com.problemio.global.auth.CustomUserDetails;
import com.problemio.global.common.ApiResponse;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.service.QuizService;
import com.problemio.user.dto.UserPopoverResponse;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSummaryDto;
import com.problemio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final QuizService quizService;

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
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestPart(value = "data") UserResponse request,
            @RequestPart(value = "file", required = false) MultipartFile file, // [체크] 이름이 'file'이어야 함
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long myId = userDetails.getUser().getId();
        // 로그 추가 (파일이 들어왔는지 확인용)
        if (file != null && !file.isEmpty()) {
            System.out.println(">>> [Controller] 파일 수신 성공: " + file.getOriginalFilename());
        } else {
            System.out.println(">>> [Controller] 파일이 없거나 비어있음");
        }

        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(myId, request, file)));
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

    // 회원 탈퇴 (비밀번호 확인을 위해 Body로 받거나 Header로 받음.)
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

    // 닉네임 중복 확인
    @GetMapping("/checkNickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestParam String nickname) {
        userService.checkNicknameDuplicate(nickname);
        // 예외가 발생하지 않으면 사용 가능한 닉네임
        return ResponseEntity.ok(ApiResponse.success(true));
    }

    // Popover용
    @GetMapping("/{userId}/popover")
    public ResponseEntity<ApiResponse<UserPopoverResponse>> getUserPopover(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long viewerId = userDetails.getUser().getId();  // 현재 로그인한 나
        UserPopoverResponse res = userService.getUserPopover(userId, viewerId);
        return ResponseEntity.ok(ApiResponse.success(res));
    }

    // 마이 페이지 내 좋아요한 퀴즈, 팔로우한 유저의 퀴즈 조회
    // 내가 팔로우한 유저들의 퀴즈 목록
    @GetMapping("/me/quizzes/followings")
    public ResponseEntity<ApiResponse<java.util.List<QuizSummaryDto>>> getQuizzesOfFollowings(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        var data = quizService.getQuizzesOfFollowings(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 내가 좋아요한 퀴즈 목록
    @GetMapping("/me/quizzes/liked")
    public ResponseEntity<ApiResponse<java.util.List<QuizSummaryDto>>> getLikedQuizzes(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUser().getId();
        var data = quizService.getLikedQuizzes(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    // 결과창에서 제작자의 다른 문제 조회
    @GetMapping("/{userId}/quizzes")
    public ResponseEntity<ApiResponse<List<QuizSummaryDto>>> getUserQuizzes(
            @PathVariable Long userId
    ) {
        List<QuizSummaryDto> quizzes = quizService.getUserQuizzes(userId);
        return ResponseEntity.ok(ApiResponse.success(quizzes));
    }
}
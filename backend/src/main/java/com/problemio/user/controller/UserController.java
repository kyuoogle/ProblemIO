package com.problemio.user.controller;

import com.problemio.global.auth.CustomUserDetails;
import com.problemio.global.common.ApiResponse;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.service.QuizService;
import com.problemio.user.dto.UserPopoverResponse;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSummaryDto;
import com.problemio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final QuizService quizService;
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.s3.url:}")
    private String s3BaseUrl;

    // 특정 유저 조회
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails // 로그인 여부 확인용
    ) {
        Long viewerId = (userDetails != null) ? userDetails.getUser().getId() : null;
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(userId, viewerId)));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(requireLogin(userDetails))));
    }

    // 프로필 수정
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @RequestPart(value = "data") UserResponse request,
            @RequestPart(value = "file", required = false) MultipartFile file, // [체크] 이름이 'file'이어야 함
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long myId = requireLogin(userDetails);
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
        Long userId = requireLogin(userDetails);
        userService.changePassword(userId, request.get("oldPassword"), request.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 회원 탈퇴 (비밀번호 확인을 위해 Body로 받거나 Header로 받음.)
    @PostMapping("/me/withdraw")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireLogin(userDetails);
        userService.deleteAccount(userId, request.get("password"));
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 마이페이지 상단 요약
    @GetMapping("/me/summary")
    public ResponseEntity<ApiResponse<UserSummaryDto>> getMySummary(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = requireLogin(userDetails);
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
        // 1. 로그인 여부에 따라 viewerId 결정 (비로그인이면 null)
        Long viewerId = (userDetails != null) ? userDetails.getUser().getId() : null;

        // 2. 서비스 호출
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
        Long userId = requireLogin(userDetails);
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
        Long userId = requireLogin(userDetails);
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

    // 리소스(테마, 아바타, 팝오버) 목록 조회
    @GetMapping("/resources/{type}")
    public ResponseEntity<ApiResponse<List<String>>> getResources(@PathVariable String type) {
        // 허용된 타입인지 확인
        if (!List.of("theme", "avatar", "popover").contains(type)) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("BAD_REQUEST", "Invalid resource type"));
        }

        // S3에서 prefix별 이미지 객체를 조회
        String prefix = "public/" + type.toLowerCase();
        List<String> fileNames = listS3Images(prefix);
        return ResponseEntity.ok(ApiResponse.success(fileNames));
    }

    private List<String> listS3Images(String prefix) {
        try {
            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix.endsWith("/") ? prefix : prefix + "/")
                    .build();

            return s3Client.listObjectsV2(req).contents().stream()
                    .map(S3Object::key)
                    .filter(this::isImageFile)
                    .map(this::buildUrlFromKey)
                    .toList();
        } catch (Exception e) {
            // 빈 리스트 반환 (프론트에서는 기본 테마/팝오버를 사용하도록 유도)
            return List.of();
        }
    }

    private String buildUrlFromKey(String key) {
        if (s3BaseUrl != null && !s3BaseUrl.isBlank()) {
            String base = s3BaseUrl.endsWith("/") ? s3BaseUrl : s3BaseUrl + "/";
            String cleanKey = key.startsWith("/") ? key.substring(1) : key;
            return base + cleanKey;
        }
        return key; // 설정이 없으면 키 자체를 반환
    }

    private boolean isImageFile(String filename) {
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
               lower.endsWith(".png") || lower.endsWith(".gif") || 
               lower.endsWith(".webp") || lower.endsWith(".svg");
    }

    private Long requireLogin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }
        return userDetails.getUser().getId();
    }
}

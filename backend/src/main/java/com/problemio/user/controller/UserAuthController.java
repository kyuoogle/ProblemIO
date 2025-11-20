package com.problemio.user.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.problemio.global.common.ApiResponse;
import com.problemio.user.dto.TokenResponse;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;
import com.problemio.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody @Valid UserSignupRequest request) {
        UserResponse response = userService.signup(request);

        // ApiResponse.success() 같은 static 메서드가 있다고 가정했습니다.
        // 만약 없다면 new ApiResponse<>("회원가입 성공", response) 형태로 쓰시면 됩니다.
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@RequestBody @Valid UserLoginRequest request) {
        TokenResponse tokenResponse = userService.login(request);

        return ResponseEntity.ok(ApiResponse.success(tokenResponse));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails) {
        // @AuthenticationPrincipal: 토큰에서 파싱한 유저 정보(UserDetails)를 바로 가져옵니다.
        // userDetails가 null이면 SecurityConfig에서 이미 403 에러로 막아줍니다.

        userService.logout(userDetails.getUsername()); // getUsername() == email
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 구현 필요
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}

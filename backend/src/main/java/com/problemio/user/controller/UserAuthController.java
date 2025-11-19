package com.problemio.user.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;
import com.problemio.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse>> signup(@RequestBody @Valid UserSignupRequest request) {
        // TODO
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody @Valid UserLoginRequest request) {
        // TODO: JWT 토큰 문자열 반환
        return null;
    }
}

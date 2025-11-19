package com.problemio.user.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;
import com.problemio.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiResponse<String>> login(@RequestBody @Valid UserLoginRequest request) {
        String accessToken = userService.login(request);

        return ResponseEntity
                .ok(ApiResponse.success(accessToken));
    }
}

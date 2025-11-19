package com.problemio.user.controller;

import com.problemio.global.common.ApiResponse;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        // TODO
        return null;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        // TODO: 인증 유저 기반
        return null;
    }

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody UserResponse request) {
        // TODO
        return null;
    }
}

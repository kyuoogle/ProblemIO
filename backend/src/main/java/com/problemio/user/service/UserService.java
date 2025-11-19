package com.problemio.user.service;

import com.problemio.user.domain.User;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;

public interface UserService {

    UserResponse signup(UserSignupRequest request);

    String login(UserLoginRequest request); // JWT 토큰 문자열 반환 가정

    User getUserById(Long id);

    UserResponse getCurrentUserProfile(Long userId);

    UserResponse updateProfile(Long userId, UserResponse request); // 프로필 수정용 DTO 따로 빼도 됨
}

package com.problemio.user.service;

import com.problemio.user.domain.User;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    // TODO: PasswordEncoder, JwtTokenProvider 등 의존성 추가 예정

    @Override
    public UserResponse signup(UserSignupRequest request) {
        // TODO
        return null;
    }

    @Override
    public String login(UserLoginRequest request) {
        // TODO
        return null;
    }

    @Override
    public User getUserById(Long id) {
        // TODO
        return null;
    }

    @Override
    public UserResponse getCurrentUserProfile(Long userId) {
        // TODO
        return null;
    }

    @Override
    public UserResponse updateProfile(Long userId, UserResponse request) {
        // TODO
        return null;
    }
}

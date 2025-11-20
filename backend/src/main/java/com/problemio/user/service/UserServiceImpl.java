package com.problemio.user.service;

import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.user.domain.User;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSummaryDto;
import com.problemio.user.mapper.UserAuthMapper;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper; // 비밀번호 검증용
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserResponse request) {
        request.setId(userId);
        userMapper.updateProfile(request);
        return getUserById(userId);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        //  현재 비밀번호 확인을 위해 AuthMapper 사용
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode. INVALID_LOGIN);
        }

        // 2. 새 비밀번호 암호화 및 변경
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId, String password) {
        // 비밀번호 확인
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode. INVALID_LOGIN);
        }

        //  탈퇴 처리
        userMapper.deleteUser(userId);
    }

    @Override
    public UserSummaryDto getMySummary(Long userId) {
        UserResponse user = getUserById(userId);
        return UserSummaryDto.builder()
                .userId(userId)
                .nickname(user.getNickname())
                .followerCount(userMapper.countFollowers(userId))
                .followingCount(userMapper.countFollowings(userId))
                .createdQuizCount(userMapper.countCreatedQuizzes(userId))
                .build();
    }

    @Override
    public List<QuizSummaryDto> getMyQuizzes(Long userId) {
        // return userMapper.findMyQuizzes(userId);
        return List.of();
    }

    @Override
    public List<QuizSummaryDto> getMyLikedQuizzes(Long userId) {
        // return userMapper.findMyLikedQuizzes(userId);
        return List.of();
    }

    @Override
    public List<QuizSummaryDto> getFollowingQuizzes(Long userId) {
        // return userMapper.findFollowingQuizzes(userId);
        return List.of();
    }
}
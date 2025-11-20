package com.problemio.user.service;

import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.global.jwt.JwtTokenProvider;
import com.problemio.user.domain.RefreshToken;
import com.problemio.user.domain.User;
import com.problemio.user.dto.TokenResponse;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;
import com.problemio.user.mapper.RefreshTokenMapper;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Override
    @Transactional
    public UserResponse signup(UserSignupRequest request) {
        // 이메일 중복 검사
        if (userMapper.findByEmail(request.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATED);
        }
        // 닉네임 중복 검사
        if (userMapper.findByNickname(request.getNickname()).isPresent()) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }

        // 유저 객체 생성 및 비밀번호 암호화
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // DB 저장
        userMapper.insertUser(user);

        // 응답 반환
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    // 로그인
    @Override
    @Transactional
    public TokenResponse login(UserLoginRequest request) {
        // 유저 조회
        User user = userMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        // 탈퇴 여부 확인 (is_Deleted 체크)
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        // 토큰 발급 (Access + Refresh)
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // Refresh Token DB 저장 (기존 토큰이 있다면 삭제 후 새로 저장)
        refreshTokenMapper.deleteByUserId(user.getId());

        refreshTokenMapper.save(RefreshToken.builder()
                .userId(user.getId())
                .tokenValue(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(2)) // 2주 유효
                .build());

        return new TokenResponse(accessToken, refreshToken);
    }

    // 토큰 재발급
    @Override
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // DB에서 토큰 조회
        RefreshToken dbToken = refreshTokenMapper.findByTokenValue(refreshToken)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));

        // 유저 존재 확인
        User user = userMapper.findById(dbToken.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 탈퇴 여부
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // Access Token만 새로 발급 (Refresh Token은 그대로 유지)
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail());

        return new TokenResponse(newAccessToken, refreshToken);
    }

    // 로그아웃
    @Override
    @Transactional
    public void logout(String email) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // DB에서 Refresh Token 삭제
        refreshTokenMapper.deleteByUserId(user.getId());
    }

    // 회원 탈퇴
    @Override
    @Transactional
    public void withdraw(String email, String password) {
        User user = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 재확인
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        // Refresh Token 삭제
        refreshTokenMapper.deleteByUserId(user.getId());

        // 유저 상태 변경 (Soft Delete: is_deleted = 1)
        userMapper.userDelete(user.getId());
    }
}
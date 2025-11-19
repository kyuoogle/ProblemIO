package com.problemio.user.service;

import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.user.domain.User;
import com.problemio.global.jwt.JwtTokenProvider;
import com.problemio.user.dto.UserLoginRequest;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSignupRequest;
import com.problemio.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider; // JWT 생성 컴포넌트


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

        // User 엔티티 생성 및 비밀번호 암호화
        User user = new User();
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        //  DB 저장
        userMapper.insertUser(user);

        //  응답 DTO 생성
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(null)
                .statusMessage(null)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public String login(UserLoginRequest request) {
        // 이메일로 유저 조회
        // 유저가 없으면 INVALID_LOGIN 예외 발생 (보안상 아이디/비번 중 뭐 틀렸는지 안 알려줌)
        User user = userMapper.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_LOGIN));

        // 비밀번호 검증
        // 암호화된 비번과 입력받은 비번 비교
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        // 탈퇴한 회원인지 확인
        if (user.isDeleted()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 토큰 생성 및 반환
        return jwtTokenProvider.createToken(user.getEmail());
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

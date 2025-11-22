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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    // 파일 저장 경로 상수 정의
    private static final String ROOT_PATH = "C:/upload/";
    private static final String PROFILE_SUB_DIR = "profile/";

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    // 프로필 업데이트
    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserResponse request, MultipartFile file) {
        // 1. 기존 유저 정보 조회 (삭제할 옛날 파일 경로를 알기 위해)
        UserResponse oldUser = getUserById(userId);
        String oldFilePath = oldUser.getProfileImageUrl();

        // 2. 새 이미지가 들어왔다면 파일 저장 처리
        if (file != null && !file.isEmpty()) {
            try {
                // 기존 파일 삭제 로직
                deleteOldProfileImage(oldFilePath);

                //  새 파일 저장 로직
                String savedPath = saveProfileImage(file);

                //  DTO에 새 경로 주입
                request.setProfileImageUrl(savedPath);
            } catch (IOException e) {
                throw new RuntimeException("파일 저장 중 오류가 발생했습니다.", e);
            }
        } else {
            // 파일이 없으면 기존 이미지 경로 유지
            if (request.getProfileImageUrl() == null) {
                request.setProfileImageUrl(oldFilePath);
            }
        }

        // 3. DB 업데이트
        request.setId(userId);
        userMapper.updateProfile(request);

        return getUserById(userId);
    }

    // 파일 저장 로직
    private String saveProfileImage(MultipartFile file) throws IOException {
        // 1. 저장할 디렉토리 경로 생성
        String saveDirectoryPath = ROOT_PATH + PROFILE_SUB_DIR;
        File folder = new File(saveDirectoryPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        // 2. 유니크한 파일명 생성
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";

        String savedFilename = UUID.randomUUID().toString() + extension;

        // 3. 실제 파일 저장
        File destination = new File(saveDirectoryPath + savedFilename);
        file.transferTo(destination);

        // 4. DB에 저장할 접근 URL 반환
        return "/uploads/" + PROFILE_SUB_DIR + savedFilename;
    }

    // 기존 파일 삭제 Helper
    private void deleteOldProfileImage(String dbFilePath) {
        if (dbFilePath != null && !dbFilePath.isEmpty()) {
            // "/uploads/"를 제거하고 ROOT_PATH("C:/upload/")를 붙임
            String localPath = dbFilePath.replace("/uploads/", ROOT_PATH);

            File oldFile = new File(localPath);

            if (oldFile.exists()) {
                if (oldFile.delete()) {
                    System.out.println(">>> [File Delete] 기존 프로필 삭제 성공: " + localPath);
                } else {
                    System.out.println(">>> [File Delete] 삭제 실패 (파일 점유됨 등): " + localPath);
                }
            }
        }
    }

    // 비밀번호 변경
    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
    }

    // 소프트 삭제
    @Override
    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

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
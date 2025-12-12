package com.problemio.user.service;

import com.problemio.comment.mapper.CommentLikeMapper;
import com.problemio.comment.mapper.CommentMapper;
import com.problemio.follow.mapper.FollowMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.quiz.domain.Quiz;
import com.problemio.quiz.dto.QuizSummaryDto;
import com.problemio.quiz.mapper.QuizLikeMapper;
import com.problemio.quiz.mapper.QuizMapper;
import com.problemio.quiz.service.QuizService;
import com.problemio.submission.mapper.SubmissionDetailMapper;
import com.problemio.submission.mapper.SubmissionMapper;
import com.problemio.user.domain.User;
import com.problemio.user.dto.UserPopoverResponse;
import com.problemio.user.dto.UserResponse;
import com.problemio.user.dto.UserSummaryDto;
import com.problemio.user.mapper.RefreshTokenMapper;
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
    private static final String ROOT_PATH = "C:/public/upload/";
    private static final String PROFILE_SUB_DIR = "profile/";

    private final UserMapper userMapper;
    private final UserAuthMapper userAuthMapper;
    private final PasswordEncoder passwordEncoder;
    private final QuizService quizService;
    private final QuizMapper quizMapper;
    private final QuizLikeMapper quizLikeMapper;
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final FollowMapper followMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionDetailMapper submissionDetailMapper;
    private final RefreshTokenMapper refreshTokenMapper;

    @Override
    public UserResponse getUserById(Long id) {
        return userMapper.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public UserResponse getUserProfile(Long userId, Long viewerId) {
        // 비로그인 사용자(viewerId == null)라면 0L로 처리
        long vId = (viewerId == null) ? 0L : viewerId;
        UserResponse user = userMapper.findUserProfile(userId, vId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
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

        // 상태 메시지 유효성 검사 (최대 20자)
        if (request.getStatusMessage() != null && request.getStatusMessage().length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
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

        // 1) 인증 토큰 정리
        refreshTokenMapper.deleteByUserId(userId);

        // 2) 팔로우 관계 제거
        followMapper.deleteByUserId(userId);

        // 3) 내가 눌렀던 퀴즈 좋아요 삭제 + 카운트 보정
        List<Long> likedQuizIds = quizLikeMapper.findQuizIdsByUserId(userId);
        if (!likedQuizIds.isEmpty()) {
            quizLikeMapper.deleteByUserId(userId);
            likedQuizIds.forEach(quizMapper::decrementLikeCount);
        }

        // 4) 내가 눌렀던 댓글 좋아요 삭제 + 카운트 보정
        List<Long> likedCommentIds = commentLikeMapper.findLikedCommentIdsByUser(userId);
        if (!likedCommentIds.isEmpty()) {
            commentLikeMapper.deleteByUserId(userId);
            likedCommentIds.forEach(commentMapper::decreaseLikeCount);
        }

        // 5) 내가 작성한 댓글 삭제 + 그 댓글 좋아요 정리
        List<Long> myCommentIds = commentMapper.findIdsByUserId(userId);
        if (!myCommentIds.isEmpty()) {
            commentLikeMapper.deleteByCommentIds(myCommentIds);
            commentMapper.deleteByUserId(userId);
        }

        // 6) 내가 만든 제출 기록 제거 (상세 먼저)
        List<Long> mySubmissionIds = submissionMapper.findIdsByUserId(userId);
        if (!mySubmissionIds.isEmpty()) {
            submissionDetailMapper.deleteBySubmissionIds(mySubmissionIds);
            submissionMapper.deleteByUserId(userId);
        }

        // 7) 내가 만든 퀴즈 삭제 (내 퀴즈에 달린 댓글/좋아요/제출도 QuizService에서 정리)
        List<Quiz> myQuizzes = quizMapper.findQuizzesByUserId(userId);
        for (Quiz quiz : myQuizzes) {
            quizService.deleteQuiz(userId, quiz.getId());
        }

        // 8) 개인정보/식별자 비우기 후 소프트 삭제 (닉네임/이메일 중복 방지)
        String tombstone = "deleted_" + UUID.randomUUID();
        userMapper.anonymizeCredentials(userId, tombstone + "@deleted.local", tombstone);
        // 9) 최종 사용자 소프트 삭제
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

    //  닉네임 중복 확인 구현
    @Override
    @Transactional(readOnly = true)
    public void checkNicknameDuplicate(String nickname) {
        if (userMapper.existsByNickname(nickname) > 0) {
            // 중복된 경우 예외를 던져 컨트롤러가 에러 응답을 하게 함
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
            // ErrorCode.DUPLICATE_NICKNAME이 없다면 DUPLICATE_VALUE 등을 사용하거나 새로 추가하세요.
        }
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


    @Override
    public UserPopoverResponse getUserPopover(Long userId, Long viewerId) {
        // 1. 매퍼에 넘길 ID 결정 (null이면 0L을 넘겨서 SQL에서 아무도 팔로우하지 않은 것처럼 처리)
        Long searchViewerId = (viewerId == null) ? 0L : viewerId;

        UserPopoverResponse res = userMapper.findUserPopover(userId, searchViewerId);

        if (res == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 내 자신인지 여부 세팅 (viewerId가 null이면 무조건 false)
        if (viewerId == null) {
            res.setMe(false);
        } else {
            res.setMe(userId.equals(viewerId));
        }

        return res;
    }
}

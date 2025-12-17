package com.problemio.user.service;

import com.problemio.comment.mapper.CommentLikeMapper;
import com.problemio.comment.mapper.CommentMapper;
import com.problemio.follow.mapper.FollowMapper;
import com.problemio.global.exception.BusinessException;
import com.problemio.global.exception.ErrorCode;
import com.problemio.global.service.S3Service;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final S3Service s3Service;

    // S3 경로 상수
    private static final String PROFILE_DIR = "public/upload/profile";

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
        long vId = (viewerId == null) ? 0L : viewerId;
        UserResponse user = userMapper.findUserProfile(userId, vId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UserResponse request, MultipartFile file) {
        UserResponse oldUser = getUserById(userId);
        String oldFilePath = oldUser.getProfileImageUrl();

        if (file != null && !file.isEmpty()) {
            
            // 기존 파일 삭제 (Service에 위임)
            s3Service.delete(oldFilePath);

            // 새 파일 저장 (Service에 위임)
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.lastIndexOf('.') > -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            String s3Key = PROFILE_DIR + "/" + UUID.randomUUID() + extension;
            
            String savedPath = s3Service.upload(file, s3Key);
            request.setProfileImageUrl(savedPath);
            
        } else {
            if (request.getProfileImageUrl() == null) {
                request.setProfileImageUrl(oldFilePath);
            }
        }

        if (request.getStatusMessage() != null && request.getStatusMessage().length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        request.setId(userId);
        userMapper.updateProfile(request);

        return getUserById(userId);
    }

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

    @Override
    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = userAuthMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_LOGIN);
        }

        refreshTokenMapper.deleteByUserId(userId);
        followMapper.deleteByUserId(userId);

        List<Long> likedQuizIds = quizLikeMapper.findQuizIdsByUserId(userId);
        if (!likedQuizIds.isEmpty()) {
            quizLikeMapper.deleteByUserId(userId);
            likedQuizIds.forEach(quizMapper::decrementLikeCount);
        }

        List<Long> likedCommentIds = commentLikeMapper.findLikedCommentIdsByUser(userId);
        if (!likedCommentIds.isEmpty()) {
            commentLikeMapper.deleteByUserId(userId);
            likedCommentIds.forEach(commentMapper::decreaseLikeCount);
        }

        List<Long> myCommentIds = commentMapper.findIdsByUserId(userId);
        if (!myCommentIds.isEmpty()) {
            commentMapper.anonymizeByUserId(userId);
        }

        List<Quiz> myQuizzes = quizMapper.findQuizzesByUserId(userId);
        for (Quiz quiz : myQuizzes) {
            quizService.deleteQuiz(userId, quiz.getId());
        }

        String tombstone = "deleted_" + UUID.randomUUID();
        userMapper.anonymizeCredentials(userId, tombstone + "@deleted.local", tombstone);
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
    @Transactional(readOnly = true)
    public void checkNicknameDuplicate(String nickname) {
        if (userMapper.existsByNickname(nickname) > 0) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATED);
        }
    }

    @Override
    public List<QuizSummaryDto> getMyQuizzes(Long userId) {
        return List.of();
    }

    @Override
    public List<QuizSummaryDto> getMyLikedQuizzes(Long userId) {
        return List.of();
    }

    @Override
    public List<QuizSummaryDto> getFollowingQuizzes(Long userId) {
        return List.of();
    }

    @Override
    public UserPopoverResponse getUserPopover(Long userId, Long viewerId) {
        Long searchViewerId = (viewerId == null) ? 0L : viewerId;
        UserPopoverResponse res = userMapper.findUserPopover(userId, searchViewerId);

        if (res == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (viewerId == null) {
            res.setMe(false);
        } else {
            res.setMe(userId.equals(viewerId));
        }

        return res;
    }
}

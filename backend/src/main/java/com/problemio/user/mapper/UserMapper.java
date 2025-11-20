package com.problemio.user.mapper;

import com.problemio.user.dto.UserResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    // 1. 마이페이지/상대방 프로필 조회 (비밀번호 제외된 DTO)
    Optional<UserResponse> findById(Long id);

    // 2. 프로필 수정 (닉네임, 이미지, 상태메시지)
    void updateProfile(UserResponse user);

    // 3. 비밀번호 변경
    void updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    // 4. 회원 탈퇴 (Soft Delete)
    void deleteUser(Long id);

    // 5. 마이페이지 통계용 카운트
    int countFollowers(Long userId);
    int countFollowings(Long userId);
    int countCreatedQuizzes(Long userId); // 퀴즈 DTO는 없어도 개수는 셀 수 있음
}
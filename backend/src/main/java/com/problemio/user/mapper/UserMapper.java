package com.problemio.user.mapper;

import com.problemio.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    // Id로 찾기
    Optional<User> findById(@Param("id") Long id);

    // 이메일 검색
    Optional<User> findByEmail(@Param("email") String email);

    // 닉네임 검색
    Optional<User> findByNickname(@Param("nickname") String nickname);

    // 회원가입
    void insertUser(User user);

    // 비밀번호 변경 (파라미터 3개 -> @Param 필수)
    void updateUserPassword(@Param("id") Long id,
                            @Param("email") String email,
                            @Param("passwordHash") String passwordHash);

    // 상태메세지 + 이미지 변경
    void updateUserStatus(@Param("id") Long id,
                          @Param("profileImageUrl") String profileImageUrl,
                          @Param("statusMessage") String statusMessage);

    // 유저 삭제 (소프트 삭제)
    void userDelete(@Param("id") Long id);
}
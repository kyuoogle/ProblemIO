package com.problemio.user.mapper;

import com.problemio.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<User> findById(@Param("id") Long id);

    Optional<User> findByEmail(@Param("email") String email);

    Optional<User> findByNickname(@Param("nickname") String nickname);

    void insertUser(User user);

    void updateUser(User user);
}

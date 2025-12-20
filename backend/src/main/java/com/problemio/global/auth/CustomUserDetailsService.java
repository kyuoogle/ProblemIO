package com.problemio.global.auth;

import com.problemio.user.domain.User;
import com.problemio.user.mapper.UserAuthMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAuthMapper userAuthMapper;

    @Override
    @Cacheable(cacheNames = "userDetails", key = "#email")
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // DB에서 조회
        User user = userAuthMapper.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일을 가진 사용자를 찾을 수 없습니다: " + email));

        // UserDetails로 변환하여 반환
        return new CustomUserDetails(user);
    }
}

package com.problemio.global.config;

import com.problemio.global.jwt.JwtAuthenticationFilter;
import com.problemio.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 비밀번호 암호화
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 보안 설정
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService
    ) throws Exception {

        http
                //  CORS: WebConfig의 설정을 사용
                .cors(Customizer.withDefaults())

                //  CSRF: JWT 사용이므로 비활성화
                .csrf(csrf -> csrf.disable())

                //  인가 정책
                .authorizeHttpRequests(auth -> auth
                        // 인증(회원가입, 로그인, 토큰 재발급)
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/reissue").permitAll()

                        // 퀴즈 조회 및 풀이 제출은 누구나 가능
                        .requestMatchers("/api/quizzes/**").permitAll()

                        // 이메일 인증 관련
                        .requestMatchers("/api/auth/email/**").permitAll()

                        // 유저, 팔로우 정보는 인증 필요 (명시적 작성)
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/follows/**").authenticated()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())

                //  JWT 필터 등록
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}

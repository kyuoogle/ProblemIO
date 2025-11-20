package com.problemio.global.config;

import com.problemio.global.jwt.JwtAuthenticationFilter;
import com.problemio.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private JwtTokenProvider jwtTokenProvider;
    private UserDetailsService userDetailsService;

    // 비밀번호 암호화를 위한 PasswordEncoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // 강력한 해시 암호화 제공
    }

    
    // 유저 접속 제한 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // 1. 인증(회원가입, 로그인)은 당연히 누구나 가능
                        // 로그아웃 때문에 전체로 넣지는 않음
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/reissue").permitAll()

                        // 2.  퀴즈 조회 및 풀이 제출도 누구나 가능하게 변경
                        // (GET /api/quizzes/**, POST /api/quizzes/**/submit 등)
                        .requestMatchers("/api/quizzes/**").permitAll()

                        // 이메일 인증 관련
                        .requestMatchers("/api/auth/email/**").permitAll()
                        
                        // 나머지는 인증 필요 (예: 마이페이지, 퀴즈 생성/삭제 등)
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())

                // JWT 필터 등록
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
    
    // 5173 프론트와 연결
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. 프론트엔드 주소 허용 (5173 포트)
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        // 2. 허용할 HTTP 메서드 (GET, POST, PUT, DELETE 등)
        configuration.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));

        // 3. 허용할 헤더 (Authorization, Content-Type 등)
        configuration.setAllowedHeaders(List.of("*"));

        // 4. 자격 증명 허용 (쿠키, Authorization 헤더 등 포함 요청 시 필수)
        configuration.setAllowCredentials(true);

        // 설정 적용 범위 (모든 경로에 대해 적용)
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

package com.problemio.global.config;

import com.problemio.global.jwt.JwtAuthenticationFilter;
import com.problemio.global.jwt.JwtTokenProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Objects;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserCache userCache(CacheManager cacheManager) {
        Cache cache = Objects.requireNonNull(cacheManager.getCache("userDetails"), "userDetails cache not found");
        return new SpringCacheBasedUserCache(cache);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService,
            UserCache userCache
    ) {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService, userCache);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. Auth endpoints (로그인, 회원가입 등)
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/reissue").permitAll()
                        .requestMatchers("/api/auth/email/**").permitAll() // 이메일 인증 관련

                        // [Admin] 관리자 전용
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 2. Static/file access
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/theme/**").permitAll()    
                        .requestMatchers("/popover/**").permitAll()  
                        .requestMatchers("/avatar/**").permitAll()   
                        .requestMatchers(HttpMethod.POST, "/api/files/**").authenticated()

                        // 3. Comments (댓글: 조회/작성/수정/삭제는 허용, 좋아요는 인증 필요)
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/*/comments").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/comments/*/replies").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/comments").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/comments/*/likes").authenticated()

                        // 4. Public quiz and submission endpoints
                        // (퀴즈 조회, 제출, 결과 조회는 누구나 가능)
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/submissions").permitAll()
                        .requestMatchers("/api/submissions/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rankings/**").permitAll()
                        
                        // Challenges
                        .requestMatchers(HttpMethod.GET, "/api/challenges", "/api/challenges/*").permitAll()
                        .requestMatchers("/api/challenges/**").authenticated()

                        // (퀴즈 생성/수정/삭제는 인증된 유저만)
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").authenticated()

                        // 5. User lookups (유저 정보 조회 관련)
                        // '/me'는 구체적인 경로이므로 와일드카드(*)보다 위에 있어야 함
                        .requestMatchers("/api/users/checkNickname").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me", "/api/users/me/**").authenticated()

                        // [추가됨] 팝오버 정보 조회 (게스트 허용)
                        .requestMatchers(HttpMethod.GET, "/api/users/*/popover").permitAll()

                        // 커스텀 아이템 정보 조회 (누구나 가능)
                        .requestMatchers(HttpMethod.GET, "/api/items/definitions").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/items/my").authenticated()

                        // 타인 퀴즈 목록 및 프로필 조회 (게스트 허용)
                        .requestMatchers(HttpMethod.GET, "/api/users/*/quizzes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*").permitAll()

                        // 6. 그 외 나머지 요청
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/follows/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(basic -> basic.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

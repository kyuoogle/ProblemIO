package com.problemio.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Image 저장, 불러오기 로직
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 프로필 사진 변경은 upload/profile로 처리
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:///C:/upload/profile/");
        
        // 확인 해볼것. quiz 썸네일과 문제 파일 분리 ? 
        // 문제 생성시 아래의 로직 작동 upload 에 들어감 
        // todo: 디렉토리 생성해서 파일 넣게 하기
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/upload/");
    }
}
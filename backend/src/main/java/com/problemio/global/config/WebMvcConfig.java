package com.problemio.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    // Image 저장, 불러오기 로직
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 로컬 업로드 경로 매핑 제거 (S3 사용으로 전환됨)
        /*
        // 프로필 사진 변경은 upload/profile로 처리
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:///C:/public/upload/profile/");
        
        // 문제 생성시 아래의 로직 작동 upload 에 들어감 
        // todo: 디렉토리 생성해서 파일 넣게 하기
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///C:/public/upload/");

        // 테마 폴더 매핑
        registry.addResourceHandler("/theme/**")
                .addResourceLocations("file:///C:/public/theme/");

        // 팝오버 폴더 매핑
        registry.addResourceHandler("/popover/**")
                .addResourceLocations("file:///C:/public/popover/");
                
        // 아바타 폴더 매핑
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:///C:/public/avatar/");
        */
    }
}
package com.example.appcenter_project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

     @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /images/** 요청을 /app/images/ 폴더에서 찾게 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:/app/images/")  // 로컬 파일 시스템 경로
                .setCachePeriod(3600);
         registry.addResourceHandler("/files/**")
                 .addResourceLocations("file:/app/files/")  // 로컬 파일 시스템 경로
                 .setCachePeriod(3600);
    }
}

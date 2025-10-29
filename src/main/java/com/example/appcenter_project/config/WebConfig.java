package com.example.appcenter_project.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.TimeZone;

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

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .timeZone(TimeZone.getTimeZone("Asia/Seoul"))
                .modules(new JavaTimeModule())
                .build();
    }
}

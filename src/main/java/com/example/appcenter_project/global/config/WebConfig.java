package com.example.appcenter_project.global.config;

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
        String baseDir = System.getProperty("user.dir");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + baseDir + "/images/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + baseDir + "/files/")
                .setCachePeriod(3600);
    }
}

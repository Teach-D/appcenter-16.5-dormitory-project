package com.example.appcenter_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // API 기본 정보
        Info info = new Info()
                .title("앱센터 16.5기 기숙사 swagger")
                .description("앱센터 16.5기 기숙사 swagger")
                .version("1.0.0");

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("로컬 개발 서버");

        // 보안 스키마 (JWT Bearer Token)
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        // 보안 요구 사항 (전역 적용)
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        // OpenAPI 객체 구성
        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .addSecurityItem(securityRequirement);
    }
}
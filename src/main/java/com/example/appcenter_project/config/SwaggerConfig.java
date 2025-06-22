package com.example.appcenter_project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${devServer}")
    private String devServer;

    @Bean
    public OpenAPI customOpenAPI() {

        // API 기본 정보
        Info info = new Info()
                .title("appcenter 16.5 dormitory API")
                .description("appcenter 16.5 dormitory API")
                .version("1.0.0");

        // 서버 정보
        Server server = new Server()
                .url(devServer)
                .description("배포 서버");

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

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .servers(List.of(server))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth))
                .addSecurityItem(securityRequirement);
    }
}
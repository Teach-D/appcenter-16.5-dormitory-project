package com.example.appcenter_project.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(description = "리프레시 토큰 입력, Bearer 를 포함하세요")
@Getter
public class RequestTokenDto {

    @Schema(description = "Bearer 토큰 형태로 입력", 
            example = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwic3R1ZGVudE51bWJlciI6InN0cg")
    private String refreshToken;
}

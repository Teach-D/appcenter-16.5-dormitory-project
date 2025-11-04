package com.example.appcenter_project.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseLoginDto {

    private String accessToken;
    private String refreshToken;
    private String role;
}

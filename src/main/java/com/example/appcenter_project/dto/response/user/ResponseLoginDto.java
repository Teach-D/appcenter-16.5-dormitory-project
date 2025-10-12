package com.example.appcenter_project.dto.response.user;

import com.example.appcenter_project.enums.user.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseLoginDto {

    private String accessToken;
    private String refreshToken;
    private String role;
}

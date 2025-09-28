package com.example.appcenter_project.dto.response.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserRole {

    private String studentNumber;
    private String role;
}

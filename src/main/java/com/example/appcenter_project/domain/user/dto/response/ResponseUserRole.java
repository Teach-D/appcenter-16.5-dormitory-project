package com.example.appcenter_project.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserRole {

    private String studentNumber;
    private String role;
}

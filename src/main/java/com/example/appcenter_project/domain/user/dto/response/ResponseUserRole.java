package com.example.appcenter_project.domain.user.dto.response;

import com.example.appcenter_project.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserRole {

    private String studentNumber;
    private String role;

    public static ResponseUserRole from(User user) {
        return ResponseUserRole.builder()
                .studentNumber(user.getStudentNumber())
                .role(user.getRole().toValue())
                .build();
    }
}
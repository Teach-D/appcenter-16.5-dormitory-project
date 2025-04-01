package com.example.appcenter_project.dto.response.user;

import com.example.appcenter_project.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseUserDto {

    private String name;
    private String studentNumber;
    private String dormType;
    private String college;
    private int penalty;
    private String role;


    public static ResponseUserDto entityToDto(User user) {
        return ResponseUserDto.builder()
                .name(user.getName())
                .studentNumber(user.getStudentNumber())
                .dormType(String.valueOf(user.getDormType()))
                .college(String.valueOf(user.getCollege()))
                .penalty(user.getPenalty())
                .role(String.valueOf(user.getRole()))
                .build();
    }
}

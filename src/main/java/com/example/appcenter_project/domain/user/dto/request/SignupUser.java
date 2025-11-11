package com.example.appcenter_project.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SignupUser {

    @NotBlank(message = "포털 로그인을 위한 학번 입력은 필수입니다.")
    private String studentNumber;

    @NotBlank(message = "포털 로그인을 위한 비밀번호 입력은 필수입니다.")
    private String password;
}

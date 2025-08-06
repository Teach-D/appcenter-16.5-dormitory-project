package com.example.appcenter_project.dto.request.tip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestTipDto {

    @NotBlank(message = "팁 게시글 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "팁 게시글 내용은 필수입니다.")
    private String content;

}

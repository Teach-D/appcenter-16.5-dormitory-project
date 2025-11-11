package com.example.appcenter_project.domain.tip.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RequestTipDto {

    @NotBlank(message = "팁 게시글 제목은 필수입니다.")
    private String title;

    @NotBlank(message = "팁 게시글 내용은 필수입니다.")
    private String content;

}

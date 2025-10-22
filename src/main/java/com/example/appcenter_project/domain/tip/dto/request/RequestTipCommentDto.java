package com.example.appcenter_project.domain.tip.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestTipCommentDto {

    private Long parentCommentId;

    @NotNull(message = "팁 ID는 필수입니다.")
    private Long tipId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String reply;
}

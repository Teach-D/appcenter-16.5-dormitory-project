package com.example.appcenter_project.domain.groupOrder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestGroupOrderCommentDto {

    private Long parentCommentId;

    @NotNull(message = "공동구매 ID는 필수입니다.")
    private Long groupOrderId;

    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String reply;
}

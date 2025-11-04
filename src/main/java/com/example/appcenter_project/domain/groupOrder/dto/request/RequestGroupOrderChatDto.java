package com.example.appcenter_project.domain.groupOrder.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestGroupOrderChatDto {

    @NotNull(message = "공동구매 채팅방 ID는 필수입니다.")
    private Long groupOrderChatRoomId;

    @NotBlank(message = "채팅 내용은 필수입니다.")
    private String content;
}

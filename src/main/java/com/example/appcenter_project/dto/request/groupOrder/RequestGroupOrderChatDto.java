package com.example.appcenter_project.dto.request.groupOrder;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RequestGroupOrderChatDto {

    private Long groupOrderChatRoomId;
    private String content;
}

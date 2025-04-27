package com.example.appcenter_project.dto.request.groupOrder;

import lombok.Getter;

@Getter
public class RequestGroupOrderChatDto {

    private Long userId;
    private Long groupOrderChatRomId;
    private String content;
}

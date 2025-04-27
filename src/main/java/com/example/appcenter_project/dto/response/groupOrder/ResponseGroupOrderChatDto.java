package com.example.appcenter_project.dto.response.groupOrder;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderChatDto {

    private Long groupOrderChatRoomId;
    private Long groupOrderChatId;
    private String content;
    private Integer unreadUserCount;
}

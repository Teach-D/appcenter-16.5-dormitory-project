package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
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
    private Long userId;

    public static ResponseGroupOrderChatDto entityToDto(GroupOrderChat groupOrderChat) {
        return ResponseGroupOrderChatDto.builder()
                .groupOrderChatRoomId(groupOrderChat.getGroupOrderChatRoom().getId())
                .groupOrderChatId(groupOrderChat.getId())
                .content(groupOrderChat.getContent())
                .unreadUserCount(
                        groupOrderChat.getUnreadUser() != null ? groupOrderChat.getUnreadUser().size() : 0
                )
                .userId(groupOrderChat.getUser().getId())
                .build();
    }
}

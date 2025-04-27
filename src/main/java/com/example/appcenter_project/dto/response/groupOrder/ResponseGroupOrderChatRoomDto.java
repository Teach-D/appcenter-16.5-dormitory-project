package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderChatRoomDto {

    private Long chatRoomId;
    private String chatRoomTitle;
    private Integer unreadCount;
    private String recentChatContent;
    private LocalDateTime recentChatTime;

    public static ResponseGroupOrderChatRoomDto entityToDto(UserGroupOrderChatRoom userGroupOrderChatRoom) {
        return ResponseGroupOrderChatRoomDto.builder()
                .chatRoomId(userGroupOrderChatRoom.getId())
                .chatRoomTitle(userGroupOrderChatRoom.getChatRoomTitle())
                .unreadCount(userGroupOrderChatRoom.getUnreadCount())
                .recentChatContent(userGroupOrderChatRoom.getRecentChatContent())
                .recentChatTime(userGroupOrderChatRoom.getUpdateTime())
                .build();
    }
}

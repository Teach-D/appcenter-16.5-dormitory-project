package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.enums.ChatRoomType;
import lombok.*;

import java.time.LocalDate;
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
    private ChatRoomType chatRoomType;
    private Integer currentPeople;
    private Integer maxPeople;
    private LocalDateTime deadline;

    public static ResponseGroupOrderChatRoomDto entityToDto(UserGroupOrderChatRoom userGroupOrderChatRoom) {
        return ResponseGroupOrderChatRoomDto.builder()
                .chatRoomId(userGroupOrderChatRoom.getId())
                .chatRoomTitle(userGroupOrderChatRoom.getChatRoomTitle())
                .unreadCount(userGroupOrderChatRoom.getUnreadCount())
                .recentChatContent(userGroupOrderChatRoom.getRecentChatContent())
                .recentChatTime(userGroupOrderChatRoom.getModifiedDate())
                .chatRoomType(ChatRoomType.GROUP_ORDER)
                .deadline(userGroupOrderChatRoom.getGroupOrderChatRoom().getGroupOrder().getDeadline())
                .build();
    }
}

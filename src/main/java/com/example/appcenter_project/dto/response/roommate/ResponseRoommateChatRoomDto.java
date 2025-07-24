package com.example.appcenter_project.dto.response.roommate;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseRoommateChatRoomDto {
    private Long chatRoomId;
    private String opponentNickname;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
}

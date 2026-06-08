package com.example.appcenter_project.domain.openChat.dto.response;

import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseOpenChatMessageDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private OpenChatMessageType type;
    private int unreadCount;
    private LocalDateTime createdAt;

    public static ResponseOpenChatMessageDto from(OpenChatMessage message, String senderNickname, int unreadCount) {
        return ResponseOpenChatMessageDto.builder()
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderNickname(senderNickname)
                .content(message.getContent())
                .type(message.getType())
                .unreadCount(unreadCount)
                .createdAt(message.getCreatedDate())
                .build();
    }
}

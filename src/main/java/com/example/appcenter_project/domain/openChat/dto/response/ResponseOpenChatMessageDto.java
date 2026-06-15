package com.example.appcenter_project.domain.openChat.dto.response;

import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ResponseOpenChatMessageDto {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private OpenChatMessageType type;
    private List<String> imageUrls;
    private int unreadCount;
    private LocalDateTime createdAt;

    public static ResponseOpenChatMessageDto from(OpenChatMessage message, String senderNickname, int unreadCount) {
        return from(message, senderNickname, unreadCount, List.of());
    }

    public static ResponseOpenChatMessageDto from(OpenChatMessage message, String senderNickname, int unreadCount, List<String> imageUrls) {
        return ResponseOpenChatMessageDto.builder()
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderNickname(senderNickname)
                .content(message.getContent())
                .type(message.getType())
                .imageUrls(imageUrls != null ? imageUrls : List.of())
                .unreadCount(unreadCount)
                .createdAt(message.getCreatedDate())
                .build();
    }
}

package com.example.appcenter_project.domain.openChat.dto.response;

import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseOpenChatRoomDto {

    private Long roomId;
    private String name;
    private String description;
    private OpenChatRoomScope scope;
    private int currentParticipants;
    private int maxParticipants;
    private boolean isJoined;
    private LocalDateTime lastMessageAt;
    private String lastMessagePreview;

    public static ResponseOpenChatRoomDto from(OpenChatRoom room, int currentParticipants, boolean joined) {
        return ResponseOpenChatRoomDto.builder()
                .roomId(room.getId())
                .name(room.getName())
                .description(room.getDescription())
                .scope(room.getScope())
                .currentParticipants(currentParticipants)
                .maxParticipants(room.getMaxParticipants())
                .isJoined(joined)
                .lastMessageAt(room.getLastMessageAt())
                .lastMessagePreview(null)
                .build();
    }
}

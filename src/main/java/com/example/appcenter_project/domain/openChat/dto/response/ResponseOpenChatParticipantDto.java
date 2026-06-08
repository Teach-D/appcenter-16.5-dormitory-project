package com.example.appcenter_project.domain.openChat.dto.response;

import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class ResponseOpenChatParticipantDto {

    private final Long userId;
    private final String nickname;
    private final String joinedAt;
    private final Boolean isHost;

    private ResponseOpenChatParticipantDto(Long userId, String nickname, String joinedAt, Boolean isHost) {
        this.userId = userId;
        this.nickname = nickname;
        this.joinedAt = joinedAt;
        this.isHost = isHost;
    }

    public static ResponseOpenChatParticipantDto of(OpenChatParticipant participant, String nickname, boolean isHost) {
        String joinedAtStr = participant.getJoinedAt() != null
                ? participant.getJoinedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        return new ResponseOpenChatParticipantDto(participant.getUserId(), nickname, joinedAtStr, isHost);
    }
}

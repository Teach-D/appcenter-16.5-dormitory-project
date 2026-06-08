package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ResponseOpenChatParticipantListDto {

    private final Long roomId;
    private final List<ResponseOpenChatParticipantDto> participants;
    private final Integer totalCount;

    private ResponseOpenChatParticipantListDto(Long roomId, List<ResponseOpenChatParticipantDto> participants) {
        this.roomId = roomId;
        this.participants = participants;
        this.totalCount = participants.size();
    }

    public static ResponseOpenChatParticipantListDto of(Long roomId, List<ResponseOpenChatParticipantDto> participants) {
        return new ResponseOpenChatParticipantListDto(roomId, participants);
    }
}

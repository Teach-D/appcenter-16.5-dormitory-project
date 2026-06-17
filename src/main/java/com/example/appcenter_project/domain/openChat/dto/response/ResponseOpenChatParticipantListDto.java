package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ResponseOpenChatParticipantListDto {

    private final Long roomId;
    private final List<ResponseOpenChatParticipantDto> participants;
    private final Integer totalCount;
    private final Integer hostCount;

    private ResponseOpenChatParticipantListDto(Long roomId, List<ResponseOpenChatParticipantDto> participants, int hostCount) {
        this.roomId = roomId;
        this.participants = participants;
        this.totalCount = participants.size();
        this.hostCount = hostCount;
    }

    public static ResponseOpenChatParticipantListDto of(Long roomId, List<ResponseOpenChatParticipantDto> participants) {
        int hostCount = (int) participants.stream()
                .filter(p -> Boolean.TRUE.equals(p.getIsHost()))
                .count();
        return new ResponseOpenChatParticipantListDto(roomId, participants, hostCount);
    }

    public static ResponseOpenChatParticipantListDto of(Long roomId, List<ResponseOpenChatParticipantDto> participants, int hostCount) {
        return new ResponseOpenChatParticipantListDto(roomId, participants, hostCount);
    }
}

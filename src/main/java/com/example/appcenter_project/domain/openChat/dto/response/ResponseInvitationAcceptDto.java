package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Getter;

@Getter
public class ResponseInvitationAcceptDto {

    private final Long roomId;
    private final String roomName;
    private final Integer currentParticipants;
    private final Integer maxParticipants;

    private ResponseInvitationAcceptDto(Long roomId, String roomName, Integer currentParticipants, Integer maxParticipants) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.currentParticipants = currentParticipants;
        this.maxParticipants = maxParticipants;
    }

    public static ResponseInvitationAcceptDto of(Long roomId, String roomName, int currentParticipants, int maxParticipants) {
        return new ResponseInvitationAcceptDto(roomId, roomName, currentParticipants, maxParticipants);
    }
}

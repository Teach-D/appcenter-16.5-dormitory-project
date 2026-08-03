package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Getter;

@Getter
public class ResponsePersonalRoomCreatedDto {

    private final Long roomId;

    private ResponsePersonalRoomCreatedDto(Long roomId) {
        this.roomId = roomId;
    }

    public static ResponsePersonalRoomCreatedDto of(Long roomId) {
        return new ResponsePersonalRoomCreatedDto(roomId);
    }
}

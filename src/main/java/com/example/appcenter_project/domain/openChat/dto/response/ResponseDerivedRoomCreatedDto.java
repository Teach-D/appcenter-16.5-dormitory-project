package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Getter;

@Getter
public class ResponseDerivedRoomCreatedDto {

    private final Long roomId;

    private ResponseDerivedRoomCreatedDto(Long roomId) {
        this.roomId = roomId;
    }

    public static ResponseDerivedRoomCreatedDto of(Long roomId) {
        return new ResponseDerivedRoomCreatedDto(roomId);
    }
}

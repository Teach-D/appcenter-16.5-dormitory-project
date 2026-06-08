package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Getter;

@Getter
public class ResponseInvitationCreatedDto {

    private final Long invitationId;

    private ResponseInvitationCreatedDto(Long invitationId) {
        this.invitationId = invitationId;
    }

    public static ResponseInvitationCreatedDto of(Long invitationId) {
        return new ResponseInvitationCreatedDto(invitationId);
    }
}

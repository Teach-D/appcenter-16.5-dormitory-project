package com.example.appcenter_project.domain.openChat.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestSendInvitationDto {

    @NotNull
    private Long inviteeUserId;
}

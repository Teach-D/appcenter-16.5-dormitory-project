package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseLeaveOpenChatRoomDto {

    private boolean roomDeleted;
}

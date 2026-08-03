package com.example.appcenter_project.domain.openChat.dto.response;

import com.example.appcenter_project.domain.openChat.enums.ChatNotificationMode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseNotificationModeDto {

    private ChatNotificationMode mode;

    public static ResponseNotificationModeDto of(ChatNotificationMode mode) {
        return new ResponseNotificationModeDto(mode);
    }
}
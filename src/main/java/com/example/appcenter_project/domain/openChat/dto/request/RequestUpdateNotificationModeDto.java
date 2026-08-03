package com.example.appcenter_project.domain.openChat.dto.request;

import com.example.appcenter_project.domain.openChat.enums.ChatNotificationMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestUpdateNotificationModeDto {

    @NotNull
    private ChatNotificationMode mode;

    public RequestUpdateNotificationModeDto(ChatNotificationMode mode) {
        this.mode = mode;
    }
}

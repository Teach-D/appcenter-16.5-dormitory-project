package com.example.appcenter_project.domain.notification.dto.request;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestNotificationReadDto {

    @NotNull
    private FcmRoutingType type;

    @NotBlank
    private String targetId;

    public RequestNotificationReadDto(FcmRoutingType type, String targetId) {
        this.type = type;
        this.targetId = targetId;
    }

    public Long getTargetIdAsLong() {
        try {
            long value = Long.parseLong(targetId);
            if (value <= 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException e) {
            throw new com.example.appcenter_project.global.exception.CustomException(
                    com.example.appcenter_project.global.exception.ErrorCode.VALIDATION_FAILED);
        }
    }
}

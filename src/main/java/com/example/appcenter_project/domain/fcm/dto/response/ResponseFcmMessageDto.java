package com.example.appcenter_project.domain.fcm.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFcmMessageDto {
    private String messageId;
    private String status;
}
package com.example.appcenter_project.dto.response.fcm;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseFcmMessageDto {
    private String messageId;
    private String status;
}
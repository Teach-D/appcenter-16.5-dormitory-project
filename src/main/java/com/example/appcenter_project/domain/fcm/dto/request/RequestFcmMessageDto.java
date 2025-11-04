package com.example.appcenter_project.domain.fcm.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestFcmMessageDto {
    private String title;
    private String body;
}
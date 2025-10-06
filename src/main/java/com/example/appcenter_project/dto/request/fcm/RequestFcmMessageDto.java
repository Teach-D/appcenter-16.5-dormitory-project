package com.example.appcenter_project.dto.request.fcm;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestFcmMessageDto {
    private String title;
    private String body;
}
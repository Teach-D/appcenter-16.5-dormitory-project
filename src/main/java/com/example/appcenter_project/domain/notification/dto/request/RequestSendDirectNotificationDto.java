package com.example.appcenter_project.domain.notification.dto.request;

import lombok.Getter;

@Getter
public class RequestSendDirectNotificationDto {

    private String studentNumber;
    private String title;
    private String content;
}

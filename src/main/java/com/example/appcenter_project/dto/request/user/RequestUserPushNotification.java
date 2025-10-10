package com.example.appcenter_project.dto.request.user;

import lombok.Getter;

@Getter
public class RequestUserPushNotification {

    private Long userId;
    private String title;
    private String body;
}

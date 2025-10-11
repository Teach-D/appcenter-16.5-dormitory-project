package com.example.appcenter_project.dto.request.user;

import lombok.Getter;

import java.util.List;

@Getter
public class RequestUserPushNotification {

    private List<Long> userIds;
    private String title;
    private String body;
}

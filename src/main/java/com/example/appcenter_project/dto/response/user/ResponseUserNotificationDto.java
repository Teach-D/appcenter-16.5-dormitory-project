package com.example.appcenter_project.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ResponseUserNotificationDto {

    private boolean roommateNotification;
    private boolean groupOrderNotification;
    private boolean dormitoryNotification;
    private boolean unidormNotification;
    private boolean supportersNotification;


}

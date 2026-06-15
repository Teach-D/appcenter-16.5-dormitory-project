package com.example.appcenter_project.domain.openChat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OpenChatNotificationScheduler {

    private final OpenChatNotificationService openChatNotificationService;

    @Scheduled(cron = "0 0 * * * *")
    public void sendHourlyNotifications() {
        openChatNotificationService.sendHourlyUnreadNotifications();
    }
}

package com.example.appcenter_project.domain.openChat.dto;

public record UnreadNotificationInfo(Long userId, Long roomId, long unreadCount) {
}

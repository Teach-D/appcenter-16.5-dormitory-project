package com.example.appcenter_project.domain.notification.service;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import com.example.appcenter_project.domain.notification.dto.response.ResponseNotificationReadDto;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.openChat.service.OpenChatMessageService;
import com.example.appcenter_project.shared.enums.ApiType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationReadService {

    private final UserNotificationRepository userNotificationQuerydslRepository;
    private final OpenChatMessageService openChatMessageService;

    @Transactional
    public ResponseNotificationReadDto markAsRead(Long userId, FcmRoutingType type, Long targetId) {
        if (type == FcmRoutingType.ANNOUNCEMENT || type == FcmRoutingType.NOTICE) {
            List<UserNotification> userNotifications =
                    userNotificationQuerydslRepository.findByUserIdAndBoardIdAndApiType(userId, targetId, ApiType.ANNOUNCEMENT);
            userNotifications.forEach(un -> un.changeReadStatus(true));
        } else {
            openChatMessageService.markChatRoomAsRead(targetId, userId);
        }
        return ResponseNotificationReadDto.success();
    }
}

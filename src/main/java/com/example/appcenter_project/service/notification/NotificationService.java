package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.dto.request.notification.RequestNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponseNotificationDto;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.example.appcenter_project.enums.user.NotificationType.*;
import static com.example.appcenter_project.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FcmMessageService fcmMessageService;
    private final UnidormNotificationService unidormNotificationService;

    public List<ResponseNotificationDto> findNotifications(Long userId) {
        List<ResponseNotificationDto> responseNotificationDtos = new ArrayList<>();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        for (UserNotification userNotification : user.getUserNotifications()) {
            responseNotificationDtos.add(ResponseNotificationDto.entityToDto(userNotification));

            // 공지사항 읽음 처리
            userNotification.updateIsRead(true);
        }

        Collections.reverse(responseNotificationDtos);
        return responseNotificationDtos;
    }

    public ResponseNotificationDto findNotification(Long userId, Long notificationId) {
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(userId, notificationId).orElseThrow();
        return  ResponseNotificationDto.entityToDto(userNotification);
    }


    public void saveNotification(RequestNotificationDto requestNotificationDto) {
        Notification notification = RequestNotificationDto.dtoToEntity(requestNotificationDto);
        notificationRepository.save(notification);

        NotificationType notificationType = from(requestNotificationDto.getNotificationType());

        if (notificationType == UNI_DORM) {
            unidormNotificationService.saveAndSendUnidormNotification(notification);
        }
        else if (notificationType == DORMITORY) {
            sendAllDormitoryStudent(notification);
        }
    }

    private void sendAllDormitoryStudent(Notification notification) {
        for (User user : userRepository.findByDormTypeNot(DormType.NONE)) {
            // 유저 알림 저장
            UserNotification userNotification = UserNotification.builder()
                    .notification(notification)
                    .user(user)
                    .build();
            userNotificationRepository.save(userNotification);

            fcmMessageService.sendDormitoryNotification(user, notification.getTitle(), notification.getBody());

        }
    }

    private void sendAllUsers(Notification notification) {
        for (User user : userRepository.findAll()) {
            // 유저 알림 저장
            UserNotification userNotification = UserNotification.builder()
                    .notification(notification)
                    .user(user)
                    .build();
            userNotificationRepository.save(userNotification);
        }

        fcmMessageService.sendNotificationToAllUsers(notification.getTitle(), notification.getBody());
    }

    public void updateNotification(Long notificationId, RequestNotificationDto requestNotificationDto) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.update(requestNotificationDto);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}

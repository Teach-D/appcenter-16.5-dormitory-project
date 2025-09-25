package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.dto.request.notification.RequestNotificationDto;
import com.example.appcenter_project.dto.response.notification.ResponseNotificationDto;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.FcmToken;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    public List<ResponseNotificationDto> findNotifications(Long userId) {
        List<ResponseNotificationDto> responseNotificationDtos = new ArrayList<>();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        for (UserNotification userNotification : user.getUserNotifications()) {
            responseNotificationDtos.add(ResponseNotificationDto.entityToDto(userNotification));
        }

        return responseNotificationDtos;
    }

    public ResponseNotificationDto findNotification(Long userId, Long notificationId) {
        UserNotification userNotification = userNotificationRepository.findByUserIdAndNotificationId(userId, notificationId).orElseThrow();
        ResponseNotificationDto responseNotificationDto = ResponseNotificationDto.entityToDto(userNotification);

        // 공지사항 읽음 처리
        userNotification.updateIsRead(true);

        return responseNotificationDto;
    }

    public void saveNotification(RequestNotificationDto requestNotificationDto) {
        Notification notification = RequestNotificationDto.dtoToEntity(requestNotificationDto);
        notificationRepository.save(notification);

        NotificationType notificationType = from(requestNotificationDto.getNotificationType());

        if (notificationType == UNI_DORM) {
            sendAllUsers(notification);
        } else if (notificationType == DORMITORY) {
            sendAllDormitoryStudent(notification);
        }
    }

    private void sendAllDormitoryStudent(Notification notification) {
        
        // todo 기숙사생 전용 알림 구현
    }

    private void sendAllUsers(Notification notification) {
        for (User user : userRepository.findAll()) {
            for (FcmToken fcmToken : user.getFcmTokenList()) {
                fcmMessageService.sendNotification(fcmToken.getToken(), notification.getTitle(), notification.getBody());
            }
        }
    }

    public void updateNotification(Long notificationId, RequestNotificationDto requestNotificationDto) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.update(requestNotificationDto);
    }

    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}

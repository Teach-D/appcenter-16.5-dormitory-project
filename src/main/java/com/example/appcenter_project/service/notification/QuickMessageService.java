package com.example.appcenter_project.service.notification;

import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.roommate.MyRoommate;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.roommate.MyRoommateRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class QuickMessageService {

    private final UserRepository userRepository;
    private final MyRoommateRepository myRoommateRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final FcmMessageService fcmMessageService;

    /**
     * 로그인한 사용자가 자신의 룸메이트에게 메시지를 전송
     */
    public void sendQuickMessageToMyRoommate(Long senderId, String messageBody) {
        // 1. 보낸 사람 확인
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 2. MyRoommate 테이블에서 룸메이트 조회
        MyRoommate myRoommate = myRoommateRepository.findByUserId(senderId)
                .orElseThrow(() -> new CustomException(MY_ROOMMATE_NOT_REGISTERED));

        User roommate = myRoommate.getRoommate();
        if (roommate == null) {
            throw new CustomException(MY_ROOMMATE_NOT_REGISTERED);
        }

        if (!roommate.getReceiveNotificationTypes().contains(NotificationType.ROOMMATE)) {
            return;
        }

        // 3. 알림 데이터 생성
        String title = sender.getName() + "님이 퀵메시지를 보냈어요!";
        String body = messageBody;

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .notificationType(NotificationType.ROOMMATE)
                .apiType(ApiType.ROOMMATE)
                .build();
        notificationRepository.save(notification);

        // 4. 유저별 알림(UserNotification) 저장
        UserNotification userNotification = UserNotification.builder()
                .notification(notification)
                .user(roommate)
                .build();
        userNotificationRepository.save(userNotification);

        // 5. FCM 전송
        try {
            fcmMessageService.sendNotification(roommate, title, body);
        } catch (Exception e) {
            throw new CustomException(FCM_SEND_FAILED);
        }
    }
}

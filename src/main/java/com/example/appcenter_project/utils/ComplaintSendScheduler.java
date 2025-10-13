package com.example.appcenter_project.utils;

import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ComplaintSendScheduler {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final FcmMessageService fcmMessageService;

    @Scheduled(cron = "0 0 10 * * MON-FRI")
//    @Scheduled(cron = "0 59 11 * * *")
    @Transactional
    public void executeEveryWeekdayAt10AM() {
        log.info("민원 알림 10시 스케줄 실행");

        Set<String> keys = redisTemplate.keys("complaint_queue:user_notification:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        // 모든 키의 값을 조회
        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        for (Object value : values) {
            if (value instanceof Number) {
                Long userNotificationId = ((Number) value).longValue();
                UserNotification userNotification = userNotificationRepository.findById(userNotificationId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_NOT_FOUND));
                
                // User를 별도로 조회하여 managed 상태로 만듦
                Long userId = userNotification.getUser().getId();
                User receiveUser = userRepository.findById(userId)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                
                Notification notification = userNotification.getNotification();
                log.info("send userNotificationId: {}, receiveUserId: {}, notification: {}", 
                        userNotificationId, userId, notification.getId());
                
                fcmMessageService.sendNotification(receiveUser, notification.getTitle(), notification.getBody());
                redisTemplate.delete("complaint_queue:user_notification:" + userNotificationId);
            }
        }
    }
}

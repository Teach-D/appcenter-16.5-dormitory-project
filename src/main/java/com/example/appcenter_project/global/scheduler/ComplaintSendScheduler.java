package com.example.appcenter_project.global.scheduler;

import com.example.appcenter_project.domain.notification.entity.Notification;
import com.example.appcenter_project.domain.notification.entity.UserNotification;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.cache.ComplaintLocalQueueService;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.notification.repository.UserNotificationRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
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
    private final ComplaintLocalQueueService localQueueService;

    @Scheduled(cron = "0 0 10 * * MON-FRI")
//    @Scheduled(cron = "0 59 11 * * *")
    @Transactional
    public void executeEveryWeekdayAt10AM() {
        log.info("민원 알림 10시 스케줄 실행");

        processRedisQueue();
        processLocalQueue();
    }

    private void processRedisQueue() {
        Set<String> keys = redisTemplate.keys("complaint_queue:user_notification:*");

        if (keys == null || keys.isEmpty()) {
            return;
        }

        List<Object> values = redisTemplate.opsForValue().multiGet(keys);
        for (Object value : values) {
            if (value instanceof Number) {
                Long userNotificationId = ((Number) value).longValue();
                sendNotification(userNotificationId);
                redisTemplate.delete("complaint_queue:user_notification:" + userNotificationId);
            }
        }
    }

    private void processLocalQueue() {
        if (localQueueService.isEmpty()) {
            return;
        }

        Set<Long> localIds = localQueueService.drainAll();
        for (Long userNotificationId : localIds) {
            sendNotification(userNotificationId);
        }
    }

    private void sendNotification(Long userNotificationId) {
        UserNotification userNotification = userNotificationRepository.findById(userNotificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOTIFICATION_NOT_FOUND));

        Long userId = userNotification.getUser().getId();
        User receiveUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Notification notification = userNotification.getNotification();
        log.info("send userNotificationId: {}, receiveUserId: {}, notification: {}",
                userNotificationId, userId, notification.getId());

        fcmMessageService.sendNotification(receiveUser, notification.getTitle(), notification.getBody());
    }
}

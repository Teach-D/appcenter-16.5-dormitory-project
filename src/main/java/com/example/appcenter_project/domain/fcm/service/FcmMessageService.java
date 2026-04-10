package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmDlqDto;
import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmMessageDto;
import com.example.appcenter_project.domain.fcm.dto.response.ResponseFcmStatsDto;
import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;
import com.example.appcenter_project.domain.user.enums.NotificationType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class FcmMessageService {

    private static final String FCM_SUCCESS_KEY_PREFIX = "fcm:success:";
    private static final String FCM_FAIL_KEY_PREFIX = "fcm:fail:";

    private final FcmTokenRepository fcmTokenRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FcmOutboxRepository fcmOutboxRepository;

    @Async("fcmExecutor")
    public void sendNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        enqueueOutbox(freshUser, title, body);
    }

    public ResponseFcmMessageDto sendNotificationToAllUsers(String title, String body) {
        List<String> tokens = fcmTokenRepository.findAll().stream()
                .map(FcmToken::getToken)
                .toList();

        if (tokens.isEmpty()) {
            log.warn("No FCM tokens found. 전체 사용자에게 보낼 토큰이 없습니다.");
            throw new CustomException(ErrorCode.FCM_TOKEN_NOT_FOUND);
        }

        List<FcmOutbox> outboxes = tokens.stream()
                .map(token -> FcmOutbox.create(token, title, body))
                .toList();
        fcmOutboxRepository.saveAll(outboxes);

        log.info("전체 FCM Outbox 적재 완료: {}개 토큰", tokens.size());
        return ResponseFcmMessageDto.builder()
                .messageId("ALL_USERS")
                .status("QUEUED")
                .build();
    }

    @Async("fcmExecutor")
    public void sendNotificationDormitoryPerson(String title, String body) {
        List<User> dormitoryUsers = userRepository.findDormitoryUsersWithFcmTokens(DormType.NONE, NotificationType.DORMITORY);
        for (User user : dormitoryUsers) {
            enqueueOutbox(user, title, body);
        }
    }

    @Async("fcmExecutor")
    public void sendGroupOrderNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.GROUP_ORDER)) {
            return;
        }
        enqueueOutbox(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendDormitoryNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.DORMITORY)) {
            return;
        }
        enqueueOutbox(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendUnidormNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.UNI_DORM)) {
            return;
        }
        enqueueOutbox(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendSupporterNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.SUPPORTERS)) {
            return;
        }
        enqueueOutbox(freshUser, title, body);
    }

    @Async("fcmExecutor")
    public void sendUnidormAnnouncementNotification(User user, String title, String body) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        if (!freshUser.getReceiveNotificationTypes().contains(NotificationType.UNI_DORM)) {
            return;
        }
        enqueueOutbox(freshUser, title, body);
    }

    @Transactional(readOnly = true)
    public ResponseFcmStatsDto getFcmStats() {
        LocalDate today = LocalDate.now();
        String successKey = FCM_SUCCESS_KEY_PREFIX + today;
        String failKey = FCM_FAIL_KEY_PREFIX + today;

        Object successVal = redisTemplate.opsForValue().get(successKey);
        Object failVal = redisTemplate.opsForValue().get(failKey);

        long successCount = successVal != null ? Long.parseLong(successVal.toString()) : 0L;
        long failCount = failVal != null ? Long.parseLong(failVal.toString()) : 0L;

        return ResponseFcmStatsDto.builder()
                .date(today.toString())
                .successCount(successCount)
                .failCount(failCount)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ResponseFcmDlqDto> getDlqList(Pageable pageable) {
        return fcmOutboxRepository.findByStatusIn(
                List.of(OutboxStatus.DEAD_PERMANENT, OutboxStatus.DEAD_EXHAUSTED), pageable
        ).map(ResponseFcmDlqDto::from);
    }

    public void retryDlq(Long outboxId) {
        FcmOutbox outbox = fcmOutboxRepository.findById(outboxId)
                .orElseThrow(() -> new CustomException(ErrorCode.FCM_OUTBOX_NOT_FOUND));
        if (outbox.getStatus() != OutboxStatus.DEAD_EXHAUSTED) {
            throw new CustomException(ErrorCode.FCM_OUTBOX_NOT_RETRYABLE);
        }
        outbox.resetToPending();
    }

    private void enqueueOutbox(User user, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findAllByUser(user);
        for (FcmToken token : tokens) {
            fcmOutboxRepository.save(FcmOutbox.create(token.getToken(), title, body));
        }
    }

}

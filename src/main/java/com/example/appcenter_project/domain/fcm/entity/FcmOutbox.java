package com.example.appcenter_project.domain.fcm.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.fcm.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "fcm_outbox", indexes = {
        @Index(name = "idx_fcm_outbox_status_next_retry", columnList = "status, next_retry_at")
})
public class FcmOutbox extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fcm_outbox_seq")
    @SequenceGenerator(name = "fcm_outbox_seq", sequenceName = "fcm_outbox_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, length = 512)
    private String token;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private int maxRetry;

    private LocalDateTime nextRetryAt;

    @Column(length = 100)
    private String lastErrorCode;

    private LocalDateTime expiredAt;

    private static final int TTL_HOURS = 24;

    public static FcmOutbox create(String token, String title, String body) {
        FcmOutbox outbox = new FcmOutbox();
        outbox.token = token;
        outbox.title = title;
        outbox.body = body;
        outbox.status = OutboxStatus.PENDING;
        outbox.retryCount = 0;
        outbox.maxRetry = 3;
        outbox.nextRetryAt = LocalDateTime.now();
        outbox.expiredAt = LocalDateTime.now().plusHours(TTL_HOURS);
        return outbox;
    }

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
    }

    public void markSent() {
        this.status = OutboxStatus.SENT;
    }

    public void markFailed(String errorCode, LocalDateTime nextRetryAt) {
        this.status = OutboxStatus.FAILED;
        this.retryCount++;
        this.lastErrorCode = errorCode;
        this.nextRetryAt = nextRetryAt;
    }

    public void markDeadPermanent(String errorCode) {
        this.status = OutboxStatus.DEAD_PERMANENT;
        this.lastErrorCode = errorCode;
    }

    public void markDeadExhausted(String errorCode) {
        this.status = OutboxStatus.DEAD_EXHAUSTED;
        this.lastErrorCode = errorCode;
    }

    public void resetToPending() {
        this.status = OutboxStatus.PENDING;
        this.retryCount = 0;
        this.nextRetryAt = LocalDateTime.now();
    }

    public void markExpired() {
        this.status = OutboxStatus.EXPIRED;
    }

    public boolean isExhausted() {
        return retryCount >= maxRetry;
    }
}

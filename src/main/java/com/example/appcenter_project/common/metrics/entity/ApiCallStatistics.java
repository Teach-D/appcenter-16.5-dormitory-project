package com.example.appcenter_project.common.metrics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * API 호출 통계를 영구 저장하는 엔티티
 *
 * 애플리케이션 재시작 시에도 누적 통계를 유지하기 위해 사용됩니다.
 */
@Entity
@Table(name = "api_call_statistics",
       uniqueConstraints = @UniqueConstraint(columnNames = {"api_name"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ApiCallStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * API 이름 (예: "survey.find", "announcement.find")
     */
    @Column(nullable = false, unique = true, length = 100)
    private String apiName;

    /**
     * 총 호출 횟수
     */
    @Column(nullable = false)
    @Builder.Default
    private Long totalCalls = 0L;

    /**
     * 마지막 호출 시간
     */
    @Column
    private LocalDateTime lastCallTime;

    /**
     * 생성 시간
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 시간
     */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 호출 횟수를 1 증가시킵니다.
     */
    public void incrementCalls() {
        this.totalCalls++;
        this.lastCallTime = LocalDateTime.now();
    }

    /**
     * 호출 횟수를 지정된 수만큼 증가시킵니다.
     */
    public void incrementCallsBy(long count) {
        this.totalCalls += count;
        this.lastCallTime = LocalDateTime.now();
    }
}
package com.example.appcenter_project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 지연된 알림 데이터를 저장하는 DTO
 * Redis에 저장되어 스케줄링된 알림의 정보를 담고 있습니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DelayedNotificationData {
    
    /**
     * 민원 ID
     */
    private Long complaintId;
    
    /**
     * 알림 제목
     */
    private String title;
    
    /**
     * 알림 내용
     */
    private String body;
    
    /**
     * 알림 타입 (NEW_COMPLAINT, STATUS_CHANGE, REPLY 등)
     */
    private String type;
    
    /**
     * 알림 생성 시간
     */
    private LocalDateTime createdAt;
    
    /**
     * 알림 우선순위 (1: 최고, 5: 최저)
     */
    private Integer priority;
    
    /**
     * 알림 발송 대상 관리자 ID 목록
     */
    private java.util.List<Long> targetAdminIds;
}


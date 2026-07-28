package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseReportedUserDto {
    private String studentNumber;
    private Long userId;          // 탈퇴/미가입이면 null
    private String name;          // 탈퇴/미가입이면 null
    private long approvedCount;   // 승인된 누적 신고 수

    public static ResponseReportedUserDto of(String studentNumber, Long userId, String name, long approvedCount) {
        return ResponseReportedUserDto.builder()
                .studentNumber(studentNumber).userId(userId).name(name).approvedCount(approvedCount).build();
    }
}
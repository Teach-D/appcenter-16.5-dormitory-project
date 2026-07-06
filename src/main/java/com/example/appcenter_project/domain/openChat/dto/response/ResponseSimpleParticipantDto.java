package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseSimpleParticipantDto {
    private Long userId;
    private String name;

    public static ResponseSimpleParticipantDto of(Long userId, String name) {
        return ResponseSimpleParticipantDto.builder()
                .userId(userId)
                .name(name)
                .build();
    }
}

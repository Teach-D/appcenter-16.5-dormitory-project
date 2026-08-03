package com.example.appcenter_project.domain.openChat.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseSimpleParticipantListDto {
    private Long roomId;
    private List<ResponseSimpleParticipantDto> participants;

    public static ResponseSimpleParticipantListDto of(Long roomId, List<ResponseSimpleParticipantDto> participants) {
        return ResponseSimpleParticipantListDto.builder()
                .roomId(roomId)
                .participants(participants)
                .build();
    }
}

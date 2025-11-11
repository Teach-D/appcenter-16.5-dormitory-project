package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ResponseReceivedRoommateMatchingDto {
    private Long matchingId;
    private Long senderId;
    private String senderName;
    private MatchingStatus status;

    @Builder
    public ResponseReceivedRoommateMatchingDto(Long matchingId, Long senderId, String senderName, MatchingStatus status) {
        this.matchingId = matchingId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.status = status;
    }
}

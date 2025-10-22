package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ResponseRoommateMatchingDto {
    private Long MatchingId;
    private Long reciverId;
    private MatchingStatus status;
}

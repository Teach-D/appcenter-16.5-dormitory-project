package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.enums.roommate.MatchingStatus;
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

package com.example.appcenter_project.domain.roommate.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseRoommateBoardDetailDto {
    private ResponseRoommatePostDto post;
    private boolean isOwner;
}

package com.example.appcenter_project.dto.response.roommate;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RoommateChatRoomDetailDto {
    private Long roomId;
    private Long partnerId;
    private String partnerName;
    private String partnerProfileImageUrl;
    private List<RoommateChatHistoryDto> chatList;
}

package com.example.appcenter_project.dto.response.roommate;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RoommateChatHistoryDto {
    private Long roommateChattingRoomId;
    private Long roommateChatId;
    private Long userId;
    private String content;
    private boolean read;
    private String createdDate;
    private String profileImageUrl;
}

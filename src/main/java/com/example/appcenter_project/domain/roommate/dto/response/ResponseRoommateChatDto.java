package com.example.appcenter_project.domain.roommate.dto.response;

import com.example.appcenter_project.domain.roommate.entity.RoommateChattingChat;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class ResponseRoommateChatDto {

    private Long roommateChattingRoomId;
    private Long roommateChatId;
    private Long userId;
    private String content;
    private boolean read;
    private String createdDate;

    public static ResponseRoommateChatDto entityToDto(RoommateChattingChat chat) {
        return ResponseRoommateChatDto.builder()
                .roommateChattingRoomId(chat.getRoommateChattingRoom().getId())
                .roommateChatId(chat.getId())
                .userId(chat.getMember().getId())
                .content(chat.getContent())
                .read(chat.isReadByReceiver())
                .createdDate(chat.getCreatedDate().toString())
                .build();
    }
}
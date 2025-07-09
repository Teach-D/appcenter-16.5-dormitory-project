package com.example.appcenter_project.dto.response.roommate;

import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
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

    public static ResponseRoommateChatDto entityToDto(RoommateChattingChat chat) {
        return ResponseRoommateChatDto.builder()
                .roommateChattingRoomId(chat.getRoommateChattingRoom().getId())
                .roommateChatId(chat.getId())
                .userId(chat.getMember().getId())
                .content(chat.getContent())
                .read(chat.isReadByReceiver())
                .build();
    }
}
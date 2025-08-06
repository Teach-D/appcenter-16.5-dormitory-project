package com.example.appcenter_project.dto.response.roommate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseRoommateChatRoomDto {
    private Long chatRoomId;
    private String opponentNickname;
    private String lastMessage;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime lastMessageTime;
    private Long partnerId;
    private String partnerName;
}

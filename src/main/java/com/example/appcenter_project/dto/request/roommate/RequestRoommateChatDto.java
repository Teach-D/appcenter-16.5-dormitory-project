package com.example.appcenter_project.dto.request.roommate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RequestRoommateChatDto {

    @NotNull
    private Long roommateChattingRoomId;

    @NotBlank
    private String content;
}


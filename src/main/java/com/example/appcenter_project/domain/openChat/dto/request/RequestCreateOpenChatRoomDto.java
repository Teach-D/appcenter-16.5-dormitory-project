package com.example.appcenter_project.domain.openChat.dto.request;

import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RequestCreateOpenChatRoomDto {

    @NotBlank
    @Size(max = 30)
    private String name;

    @Size(max = 100)
    private String description;

    @NotNull
    private OpenChatRoomScope scope;

    @NotNull
    @Min(2)
    @Max(100)
    private Integer maxParticipants;
}

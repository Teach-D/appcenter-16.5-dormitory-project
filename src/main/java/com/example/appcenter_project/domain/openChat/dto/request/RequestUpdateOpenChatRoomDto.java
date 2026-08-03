package com.example.appcenter_project.domain.openChat.dto.request;

import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestUpdateOpenChatRoomDto {

    @Size(min = 1, max = 30)
    private String name;

    @Size(max = 100)
    private String description;

    private OpenChatRoomScope scope;

    @Min(2) @Max(100)
    private Integer maxParticipants;

    @Size(max = 50)
    private String password;

    private Boolean isPublic;

    @Builder
    public RequestUpdateOpenChatRoomDto(String name, String description, OpenChatRoomScope scope,
                                        Integer maxParticipants, String password, Boolean isPublic) {
        this.name = name;
        this.description = description;
        this.scope = scope;
        this.maxParticipants = maxParticipants;
        this.password = password;
        this.isPublic = isPublic;
    }
}

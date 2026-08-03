package com.example.appcenter_project.domain.openChat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestCreatePersonalRoomDto {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotNull
    private Long targetUserId;

    @Size(max = 50)
    private String password;

    @Builder
    public RequestCreatePersonalRoomDto(String name, Long targetUserId, String password) {
        this.name = name;
        this.targetUserId = targetUserId;
        this.password = password;
    }
}

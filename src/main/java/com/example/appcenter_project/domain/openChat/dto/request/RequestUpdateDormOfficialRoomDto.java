package com.example.appcenter_project.domain.openChat.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestUpdateDormOfficialRoomDto {

    @Size(min = 1, max = 30)
    private String name;

    @Size(max = 100)
    private String description;

    @Builder
    public RequestUpdateDormOfficialRoomDto(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

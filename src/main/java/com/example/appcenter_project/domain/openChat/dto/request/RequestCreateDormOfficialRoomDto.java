package com.example.appcenter_project.domain.openChat.dto.request;

import com.example.appcenter_project.domain.user.enums.DormType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestCreateDormOfficialRoomDto {

    @NotBlank
    @Size(max = 30)
    private String name;

    @Size(max = 100)
    private String description;

    @NotNull
    private DormType dormType;

    @Builder
    public RequestCreateDormOfficialRoomDto(String name, String description, DormType dormType) {
        this.name = name;
        this.description = description;
        this.dormType = dormType;
    }
}

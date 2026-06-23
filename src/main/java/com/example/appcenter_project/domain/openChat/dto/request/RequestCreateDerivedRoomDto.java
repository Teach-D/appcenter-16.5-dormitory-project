package com.example.appcenter_project.domain.openChat.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestCreateDerivedRoomDto {

    @NotBlank
    @Size(min = 1, max = 30)
    private String name;

    @Size(max = 100)
    private String description;

    @NotNull
    @Min(2)
    @Max(100)
    private Integer maxParticipants;

    @NotNull
    private Boolean isPublic;

    @Size(max = 50)
    private String password;
}

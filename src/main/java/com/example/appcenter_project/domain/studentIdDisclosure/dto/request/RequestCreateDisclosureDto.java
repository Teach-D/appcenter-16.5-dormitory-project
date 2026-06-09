package com.example.appcenter_project.domain.studentIdDisclosure.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestCreateDisclosureDto {

    @NotNull
    private Long roomId;

    @NotNull
    private Long targetId;
}

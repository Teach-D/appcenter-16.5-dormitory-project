package com.example.appcenter_project.domain.studentIdDisclosure.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDisclosureStatusDto {

    private String status;
    private Long requestId;
    private String targetStudentNumber;
}

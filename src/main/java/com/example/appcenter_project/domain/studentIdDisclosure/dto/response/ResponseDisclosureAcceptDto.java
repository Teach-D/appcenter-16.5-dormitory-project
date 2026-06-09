package com.example.appcenter_project.domain.studentIdDisclosure.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDisclosureAcceptDto {

    private Long requestId;
    private String requesterStudentNumber;
}

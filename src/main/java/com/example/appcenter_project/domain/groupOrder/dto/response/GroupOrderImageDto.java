package com.example.appcenter_project.domain.groupOrder.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GroupOrderImageDto {

    private String filename;
    private String contentType;

}

package com.example.appcenter_project.dto.response.groupOrder;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GroupOrderImageDto {

    private String filename;
    private String contentType;

}

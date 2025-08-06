package com.example.appcenter_project.dto.response.tip;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TipImageDto {

    private String filename;
    private String contentType;
}

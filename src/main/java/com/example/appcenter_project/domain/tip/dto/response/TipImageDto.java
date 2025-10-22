package com.example.appcenter_project.domain.tip.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TipImageDto {

    private String filename;
    private String contentType;
}

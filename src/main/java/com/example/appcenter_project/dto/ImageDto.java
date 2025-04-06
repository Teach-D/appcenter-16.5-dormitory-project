package com.example.appcenter_project.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
public class ImageDto {

    private Resource resource;
    private String contentType;
}

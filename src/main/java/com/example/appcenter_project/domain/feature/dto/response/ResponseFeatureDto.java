package com.example.appcenter_project.domain.feature.dto.response;

import com.example.appcenter_project.domain.feature.entity.Feature;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ResponseFeatureDto {

    private String key;
    private boolean flag;

    public static ResponseFeatureDto of(Feature feature) {
        return ResponseFeatureDto.builder()
                .key(feature.getKey())
                .flag(feature.isFlag())
                .build();
    }
}

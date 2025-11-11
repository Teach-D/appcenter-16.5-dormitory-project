package com.example.appcenter_project.common.image.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.Resource;

@Getter
@Builder
public class ImageDto {

    @NotNull(message = "이미지 리소스가 필요합니다.")
    private Resource resource;

    @NotBlank(message = "컨텐츠 타입을 입력해주세요.")
    private String contentType;
}
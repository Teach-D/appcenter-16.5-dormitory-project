package com.example.appcenter_project.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageLinkDto {

    @NotBlank(message = "이미지 URL이 필요합니다.")
    private String imageUrl;

    @NotBlank(message = "파일명이 필요합니다.")
    private String fileName;

    @NotBlank(message = "컨텐츠 타입을 입력해주세요.")
    private String contentType;

    private Long fileSize; // 파일 크기 (bytes)
    private LocalDateTime uploadDate; // 업로드 날짜
}

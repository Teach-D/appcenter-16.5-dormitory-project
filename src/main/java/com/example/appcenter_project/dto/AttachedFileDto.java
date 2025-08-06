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
public class AttachedFileDto {

    @NotBlank(message = "첨부파일 URL이 필요합니다.")
    private String fileUrl;

    @NotBlank(message = "첨부파일명이 필요합니다.")
    private String fileName;

    private Long fileSize; // 파일 크기 (bytes)
    private LocalDateTime uploadDate; // 업로드 날짜
}

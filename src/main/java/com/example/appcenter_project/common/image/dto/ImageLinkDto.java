package com.example.appcenter_project.common.image.dto;

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

    @NotBlank(message = "이미지 이름이 필요합니다.")
    private String imageName;

    @NotBlank(message = "이미지 경로가 필요합니다.")
    private String imageUrl;

    @NotBlank(message = "컨텐츠 타입을 입력해주세요.")
    private String contentType;

    private Long fileSize; // 파일 크기 (bytes)
    private LocalDateTime uploadDate; // 업로드 날짜

    public static ImageLinkDto of(String imageName, String staticImageUrl, String contentType, Long length) {
        return ImageLinkDto.builder()
                .imageName(imageName)
                .imageUrl(staticImageUrl)  // 정적 리소스로 직접 접근 가능한 URL
                .contentType(contentType)
                .fileSize(length)
                .build();
    }

    public static ImageLinkDto ofNull() {
        return ImageLinkDto.builder()
                .imageName(null)
                .imageUrl(null)  // 정적 리소스로 직접 접근 가능한 URL
                .contentType(null)
                .fileSize(null)
                .build();
    }
}

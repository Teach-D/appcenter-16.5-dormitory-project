package com.example.appcenter_project.dto;

import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.image.ImageType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageListCacheDto {

    private Long id;
    private String filePath;
    private ImageType imageType;
    private Boolean isDefault = false;
    private Long boardId;

    public static ImageListCacheDto from(Image image) {
        return ImageListCacheDto.builder()
                .id(image.getId())
                .filePath(image.getFilePath())
                .imageType(image.getImageType())
                .isDefault(image.getIsDefault())
                .boardId(image.getBoardId())
                .build();
    }
}

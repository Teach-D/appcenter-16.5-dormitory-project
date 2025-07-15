package com.example.appcenter_project.dto.cache;

import com.example.appcenter_project.entity.Image;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageCacheDto {
    private Long id;
    private String filePath;
    private String imageType;
    private Boolean isDefault;
    private Long boardId;

    public static ImageCacheDto fromEntity(Image image) {
        ImageCacheDto dto = new ImageCacheDto();
        dto.setId(image.getId());
        dto.setFilePath(image.getFilePath());
        dto.setImageType(image.getImageType() != null ? image.getImageType().name() : null);
        dto.setIsDefault(image.getIsDefault());
        dto.setBoardId(image.getBoardId());
        return dto;
    }
}
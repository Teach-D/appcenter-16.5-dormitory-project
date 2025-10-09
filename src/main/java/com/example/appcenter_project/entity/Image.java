package com.example.appcenter_project.entity;

import com.example.appcenter_project.enums.image.ImageType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageName;
    
    @Column(nullable = false)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 200)
    private ImageType imageType;

    @Column(nullable = false)
    private Boolean isDefault = false;

    private Long entityId = 0L;

    @Builder
    public Image(String filePath, String fileName, ImageType imageType, Boolean isDefault, Long entityId) {
        this.imagePath = filePath;
        this.imageName = fileName;
        this.imageType = imageType;
        this.isDefault = isDefault;
        this.entityId = entityId;
    }

    public static Image of(String filePath, String fileName, ImageType imageType, Long entityId) {
        return Image.builder()
                .filePath(filePath)
                .fileName(fileName)
                .isDefault(false)
                .imageType(imageType)
                .entityId(entityId)
                .build();
    }

    public void updateImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setIsDefault(boolean b) {
        this.isDefault = b;
    }
}

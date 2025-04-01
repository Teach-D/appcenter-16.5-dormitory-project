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

    private String filePath;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    private Boolean isDefault = false;

    @Builder
    public Image(String filePath, ImageType imageType, Boolean isDefault) {
        this.filePath = filePath;
        this.imageType = imageType;
        this.isDefault = isDefault;
    }

    public void updateFilePath(String filePath) {
        this.filePath = filePath;
    }
}

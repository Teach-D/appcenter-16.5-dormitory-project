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
    private String filePath;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Long boardId;

    @Builder
    public Image(String filePath, ImageType imageType, Boolean isDefault, Long boardId) {
        this.filePath = filePath;
        this.imageType = imageType;
        this.isDefault = isDefault;
        this.boardId = boardId;
    }

    public void updateFilePath(String filePath) {
        this.filePath = filePath;
    }
}

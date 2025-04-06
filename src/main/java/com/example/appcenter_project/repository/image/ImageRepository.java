package com.example.appcenter_project.repository.image;

import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.image.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByFilePath(String filePath);
    Optional<Image> findByImageTypeAndIsDefault(ImageType imageType, Boolean isDefault);
}

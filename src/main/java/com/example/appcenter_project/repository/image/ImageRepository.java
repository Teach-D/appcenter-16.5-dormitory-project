package com.example.appcenter_project.repository.image;

import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.image.ImageType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByFilePath(String filePath);
    Optional<Image> findAllByImageTypeAndIsDefault(ImageType imageType, Boolean isDefault);
    boolean existsByImageTypeAndIsDefault(ImageType imageType, boolean isDefault);

    // Tip 이미지 관련 메서드 추가
    Optional<Image> findByBoardIdAndImageType(Long boardId, ImageType imageType);
    List<Image> findAllByBoardIdAndImageType(Long boardId, ImageType imageType);
}

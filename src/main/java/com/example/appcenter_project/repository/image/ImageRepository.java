package com.example.appcenter_project.repository.image;

import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.enums.image.ImageType;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImagePath(String filePath);
    Optional<Image> findAllByImageTypeAndIsDefault(ImageType imageType, Boolean isDefault);
    boolean existsByImageTypeAndIsDefault(ImageType imageType, boolean isDefault);

    // Tip 이미지 관련 메서드 추가
    Optional<Image> findByEntityIdAndImageType(Long boardId, ImageType imageType);
    List<Image> findAllByEntityIdAndImageType(Long boardId, ImageType imageType);

    @Query("SELECT i FROM Image i WHERE i.imageType = 'TIP' AND i.entityId IN :boardIds")
    List<Image> findTipImagesByEntityIds(@Param("boardIds") List<Long> boardIds);

    // GroupOrder 이미지 관련 메서드 추가
    @Query("SELECT i FROM Image i WHERE i.imageType = 'GROUP_ORDER' AND i.entityId IN :boardIds")
    List<Image> findGroupOrderImagesByEntityIds(@Param("boardIds") List<Long> boardIds);

    List<Image> findByImageTypeAndEntityId(ImageType imageType, Long entityId);

    Optional<Image> findByImageName(String imageName);
}

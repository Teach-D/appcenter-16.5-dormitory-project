package com.example.appcenter_project.domain.tip.service;

import com.example.appcenter_project.domain.tip.dto.response.ResponseTipDto;
import com.example.appcenter_project.common.image.entity.Image;
import com.example.appcenter_project.domain.tip.entity.TipLike;
import com.example.appcenter_project.domain.tip.entity.Tip;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.repository.ImageRepository;
import com.example.appcenter_project.domain.tip.repository.TipLikeRepository;
import com.example.appcenter_project.domain.tip.repository.TipRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipQueryService {

    private final TipRepository tipRepository;
    private final ImageRepository imageRepository;
    private final TipService tipService;
    private final TipLikeRepository tipLikeRepository;
    private final ImageService imageService;
    /**
     * Tip + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseTipDto> findTipsByUser(Long userId, HttpServletRequest request) {
        List<Tip> tips = tipRepository.findByUserId(userId);

        if (tips.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Image>> TipImageMap = findTipImageMap(toTipIds(tips));

        return tips.stream()
                .map(tip -> {
                    Image image = Optional.ofNullable(TipImageMap.get(tip.getId()))
                            .filter(images -> !images.isEmpty())
                            .map(images -> images.get(0))
                            .orElse(null);

                    String imageUrl = null;
                    if (image != null) {
                        imageUrl = imageService.getImageUrl(ImageType.TIP, image, request);
                    }

                    return ResponseTipDto.entityToDto(tip, imageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * TipLike + Tip + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseTipDto> findLikedByUser(Long userId, HttpServletRequest request) {
        List<TipLike> tipLikes = tipLikeRepository.findByUserIdWithTip(userId);
        List<Tip> tips = tipLikes.stream().map(TipLike::getTip).toList();

        if (tipLikes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Image>> TipImageMap = findTipImageMap(toTipIds(tips));

        return tips.stream()
                .map(tip -> {
                    Image image = Optional.ofNullable(TipImageMap.get(tip.getId()))
                            .filter(images -> !images.isEmpty())
                            .map(images -> images.get(0))
                            .orElse(null);

                    String imageUrl = null;
                    if (image != null) {
                        imageUrl = imageService.getImageUrl(ImageType.TIP, image, request);
                    }

                    return ResponseTipDto.entityToDto(tip, imageUrl);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, List<Image>> findTipImageMap(List<Long> TipIds) {
        List<Image> findTipImages = imageRepository.findTipImagesByEntityIds(TipIds);
        return findTipImages.stream()
                .collect(Collectors.groupingBy(Image::getEntityId));
    }

    private List<Long> toTipIds(List<Tip> Tips) {
        return Tips.stream().map(Tip::getId).collect(Collectors.toList());
    }


}

package com.example.appcenter_project.service.tip;

import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.tip.Tip;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.tip.TipRepository;
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

    /**
     * Tip + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseTipDto> findTipDtosWithImages(Long userId, HttpServletRequest request) {
        List<Tip> Tips = tipRepository.findByUserId(userId);

        if (Tips.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Image>> TipImageMap = findTipImageMap(toTipIds(Tips));

        return Tips.stream()
                .map(Tip -> {
                    Image image = Optional.ofNullable(TipImageMap.get(Tip.getId()))
                            .filter(images -> !images.isEmpty())
                            .map(images -> images.get(0))
                            .orElse(null);

                    String fileName = null;
                    if (image != null) {
                        fileName = tipService.getTipImage(image, request).getFileName();
                    }

                    return ResponseTipDto.entityToDto(Tip, fileName);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, List<Image>> findTipImageMap(List<Long> TipIds) {
        List<Image> findTipImages = imageRepository.findTipImagesByBoardIds(TipIds);
        return findTipImages.stream()
                .collect(Collectors.groupingBy(Image::getBoardId));
    }

    private List<Long> toTipIds(List<Tip> Tips) {
        return Tips.stream().map(Tip::getId).collect(Collectors.toList());
    }
}

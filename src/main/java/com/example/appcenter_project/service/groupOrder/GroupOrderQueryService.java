package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.image.ImageRepository;
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
public class GroupOrderQueryService {

    private final GroupOrderRepository groupOrderRepository;
    private final ImageRepository imageRepository;
    private final GroupOrderService groupOrderService;

    /**
     * GroupOrder + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseGroupOrderDto> findGroupOrderDtosWithImages(Long userId, HttpServletRequest request) {
        List<GroupOrder> groupOrders = groupOrderRepository.findByUserId(userId);

        if (groupOrders.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<Image>> groupOrderImageMap = findGroupOrderImageMap(toGroupOrderIds(groupOrders));

        return groupOrders.stream()
                .map(groupOrder -> {
                    Image image = Optional.ofNullable(groupOrderImageMap.get(groupOrder.getId()))
                            .filter(images -> !images.isEmpty())
                            .map(images -> images.get(0))
                            .orElse(null);

                    String fileName = null;
                    if (image != null) {
                        fileName = groupOrderService.getGroupOrderImage(image, request).getFileName();
                    }

                    return ResponseGroupOrderDto.entityToDto(groupOrder, fileName);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, List<Image>> findGroupOrderImageMap(List<Long> groupOrderIds) {
        List<Image> findGroupOrderImages = imageRepository.findGroupOrderImagesByBoardIds(groupOrderIds);
        return findGroupOrderImages.stream()
                .collect(Collectors.groupingBy(Image::getBoardId));
    }

    private List<Long> toGroupOrderIds(List<GroupOrder> groupOrders) {
        return groupOrders.stream().map(GroupOrder::getId).collect(Collectors.toList());
    }
}
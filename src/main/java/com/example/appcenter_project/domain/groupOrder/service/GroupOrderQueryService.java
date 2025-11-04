package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderDto;
import com.example.appcenter_project.common.image.entity.Image;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.common.image.repository.ImageRepository;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderLikeRepository;
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
public class GroupOrderQueryService {

    private final GroupOrderRepository groupOrderRepository;
    private final ImageRepository imageRepository;
    private final GroupOrderService groupOrderService;
    private final GroupOrderLikeRepository groupOrderLikeRepository;
    private final ImageService imageService;
    /**
     * GroupOrder + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseGroupOrderDto> findGroupOrdersByUser(Long userId, HttpServletRequest request) {
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

                    String imageUrl = null;
                    if (image != null) {
                        imageUrl = imageService.getImageUrl(ImageType.GROUP_ORDER, image, request);
                    }

                    return ResponseGroupOrderDto.of(groupOrder, imageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * GroupOrderLike + GroupOrder + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseGroupOrderDto> findLikedByUser(Long userId, HttpServletRequest request) {
        List<GroupOrderLike> groupOrderLikes = groupOrderLikeRepository.findByUserIdWithGroupOrder(userId);
        List<GroupOrder> groupOrders = groupOrderLikes.stream().map(GroupOrderLike::getGroupOrder).toList();

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

                    String imageUrl = null;
                    if (image != null) {
                        imageUrl = imageService.getImageUrl(ImageType.GROUP_ORDER, image, request);
                    }

                    return ResponseGroupOrderDto.of(groupOrder, imageUrl);
                })
                .collect(Collectors.toList());
    }

    private Map<Long, List<Image>> findGroupOrderImageMap(List<Long> groupOrderIds) {
        List<Image> findGroupOrderImages = imageRepository.findGroupOrderImagesByEntityIds(groupOrderIds);
        return findGroupOrderImages.stream()
                .collect(Collectors.groupingBy(Image::getEntityId));
    }

    private List<Long> toGroupOrderIds(List<GroupOrder> groupOrders) {
        return groupOrders.stream().map(GroupOrder::getId).collect(Collectors.toList());
    }


}
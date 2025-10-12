package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.service.image.ImageService;
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

                    String imageUrl = null;
                    if (image != null) {
                        imageUrl = imageService.getImageUrl(ImageType.GROUP_ORDER, image, request);
                    }

                    return ResponseGroupOrderDto.entityToDto(groupOrder, imageUrl);
                })
                .collect(Collectors.toList());
    }

    /**
     * GroupOrderLike + GroupOrder + Image 최적화 조회
     * N+1 문제 해결을 위한 전용 서비스
     */
    public List<ResponseGroupOrderDto> findGroupOrderLikeDtosWithImages(Long userId, HttpServletRequest request) {
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

                    return ResponseGroupOrderDto.entityToDto(groupOrder, imageUrl);
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
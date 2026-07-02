package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderDto;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.place.service.PlaceResolveResult;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupOrderCreateService {

    private final GroupOrderRepository groupOrderRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final GroupOrderNotificationService groupOrderNotificationService;

    @Transactional
    public void createAndPersist(Long userId, RequestGroupOrderDto dto, List<MultipartFile> images,
                                  PlaceResolveResult resolveResult, String rawPlaceName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GroupOrder groupOrder = RequestGroupOrderDto.of(dto, user);
        if (resolveResult != null) {
            String normalizedRaw = rawPlaceName != null && !rawPlaceName.isBlank() ? rawPlaceName : null;
            groupOrder.assignPlace(resolveResult.getPlace(), normalizedRaw);
        }

        groupOrderRepository.save(groupOrder);
        imageService.saveImages(ImageType.GROUP_ORDER, groupOrder.getId(), images);
        groupOrderNotificationService.sendNotifications(groupOrder);
    }
}

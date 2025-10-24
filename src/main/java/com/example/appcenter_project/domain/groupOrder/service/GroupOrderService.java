package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderPopularSearch;
import com.example.appcenter_project.domain.groupOrder.entity.*;
import com.example.appcenter_project.domain.groupOrder.repository.*;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderDto;
import com.example.appcenter_project.common.image.entity.Image;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderLike;
import com.example.appcenter_project.global.scheduler.MealTimeChecker;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderSort;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.common.image.repository.ImageRepository;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderLikeRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.notification.service.GroupOrderNotificationOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderService {

    private final GroupOrderRepository groupOrderRepository;
    private final UserRepository userRepository;
    private final GroupOrderLikeRepository groupOrderLikeRepository;
    private final GroupOrderCommentRepository groupOrderCommentRepository;
    private final ImageRepository imageRepository;
    private final GroupOrderPopularSearchKeywordRepository groupOrderPopularSearchKeywordRepository;
    private final ImageService imageService;
    private final AsyncViewCountService asyncViewCountService;
    private final MealTimeChecker mealTimeChecker;
    private final GroupOrderNotificationService groupOrderNotificationService;

    private static final int TOP_SEARCH_KEYWORD_COUNT = 10;

    // ========== Public Methods ========== //

    public void saveGroupOrder(Long userId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        GroupOrder groupOrder = createGroupOrder(requestGroupOrderDto, user);
        imageService.saveImages(ImageType.GROUP_ORDER, groupOrder.getId(), images);

        groupOrderNotificationService.sendNotification(groupOrder);
    }

    public void addRating(Long groupOrderId, Float ratingScore) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        groupOrder.getUser().addRating(ratingScore);
    }

    public ResponseGroupOrderDetailDto findGroupOrder(CustomUserDetails currentUser, Long groupOrderId, HttpServletRequest request) {
        plusViewCount(groupOrderId);

        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        updateExpiredGroupOrder(groupOrder);

        ResponseGroupOrderDetailDto groupOrderDetailDto = createDto(request, groupOrder);
        checkGroupOrderOwnByCurrentUser(currentUser, groupOrder, groupOrderDetailDto);
        checkCurrentUserLiked(currentUser, groupOrder, groupOrderDetailDto);
        addCommentToDto(groupOrder.getId(), request, groupOrderDetailDto);

        return groupOrderDetailDto;
    }


    public List<String> findGroupOrderSearchLog(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return user.getSearchLogs();
    }

    public List<ResponseGroupOrderPopularSearch> findGroupOrderPopularSearch() {
        List<GroupOrderPopularSearchKeyword> topKeywords = groupOrderPopularSearchKeywordRepository
                .findTopKeywords(TOP_SEARCH_KEYWORD_COUNT);

        return createDtoWithRank(topKeywords);
    }

    public List<ImageLinkDto> findGroupOrderImages(Long groupOrderId, HttpServletRequest request) {
        return imageService.findImages(ImageType.GROUP_ORDER, groupOrderId, request);
    }

    public List<ResponseGroupOrderDto> findGroupOrders(CustomUserDetails currentUser, GroupOrderSort sort, GroupOrderType type, String search, HttpServletRequest request) {
        addUserSearchLog(currentUser, search);

        List<ResponseGroupOrderDto> responseGroupOrderDtoList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (GroupOrder groupOrder : groupOrderRepository.findGroupOrdersComplex(sort, type, search)) {
            updateExpiredGroupOrder(groupOrder, now);
            String imagePath = findRepresentativeImage(request, groupOrder);
            responseGroupOrderDtoList.add(ResponseGroupOrderDto.of(groupOrder, imagePath));
        }

        return responseGroupOrderDtoList;
    }

    public Integer likeGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (groupOrder.isLikedBy(user)) {
            throw new CustomException(ALREADY_GROUP_ORDER_LIKE_USER);
        }

        createGroupOrderLike(user, groupOrder);

        return groupOrder.increaseLike();
    }

    public Integer unlikeGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (!groupOrder.isLikedBy(user)) {
            throw new CustomException(GROUP_ORDER_LIKE_NOT_FOUND);
        }

        deleteGroupOrderLike(user, groupOrder);

        return groupOrder.decreaseLike();
    }

    public void completeGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        groupOrder.updateRecruitmentComplete(true);
    }

    public void unCompleteGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        groupOrder.updateRecruitmentComplete(false);
    }

    public void updateGroupOrder(Long userId, Long groupOrderId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));

        if (isChangeNewOpenChatLink(requestGroupOrderDto, groupOrder)) {
            groupOrderNotificationService.sendOpenChatLinkNotification(groupOrder);
        }

        groupOrder.update(requestGroupOrderDto);

        imageService.updateGroupOrderImages(ImageType.GROUP_ORDER, groupOrderId, images);
    }

    public void deleteGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));
        imageService.deleteImages(ImageType.GROUP_ORDER, groupOrderId);
        groupOrderRepository.delete(groupOrder);
    }

    public void deleteGroupOrderImage(Long userId, String imageName) {
        Image image = imageRepository.findByImageName(imageName)
                .orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
        GroupOrder groupOrder = groupOrderRepository.findById(image.getEntityId())
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        if (isGroupOrderOwnByUser(userId, groupOrder)) {
            throw new CustomException(IMAGE_UPDATE_NOT_ALLOWED);
        }

        imageService.deleteImage(image);
    }

    // ========== Private Methods ========== //


    private GroupOrder createGroupOrder(RequestGroupOrderDto requestGroupOrderDto, User user) {
        GroupOrder groupOrder = RequestGroupOrderDto.of(requestGroupOrderDto, user);
        groupOrderRepository.save(groupOrder);
        return groupOrder;
    }

    private void plusViewCount(Long groupOrderId) {
        if (mealTimeChecker.isMealTime()) {
            asyncViewCountService.incrementViewCount(groupOrderId);
        } else {
            GroupOrder groupOrder = groupOrderRepository.findByIdWithLock(groupOrderId)
                    .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
            groupOrder.plusViewCount();
        }
    }

    private static void updateExpiredGroupOrder(GroupOrder groupOrder) {
        LocalDateTime now = LocalDateTime.now();
        updateExpiredGroupOrder(groupOrder, now);
    }

    private ResponseGroupOrderDetailDto createDto(HttpServletRequest request, GroupOrder groupOrder) {
        String writerImageUrl = getRepresentativeUserImageUrl(request, groupOrder.getUser());
        return ResponseGroupOrderDetailDto.of(groupOrder, writerImageUrl);
    }

    private String getRepresentativeUserImageUrl(HttpServletRequest request, User findUser) {
        List<ImageLinkDto> imageLinkDtos = imageService.findImages(ImageType.USER, findUser.getId(), request);

        if (imageLinkDtos.isEmpty()) {
            return null;
        }

        return imageLinkDtos.get(0).getImageUrl();
    }

    private void checkGroupOrderOwnByCurrentUser(CustomUserDetails currentUser, GroupOrder groupOrder, ResponseGroupOrderDetailDto dto) {
        if (isUserLogin(currentUser)) {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
            if (Objects.equals(groupOrder.getUser().getId(), user.getId())) {
                dto.updateIsMyPost(true);
            }
        }
    }

    private static boolean isUserLogin(CustomUserDetails currentUser) {
        return currentUser != null;
    }

    private void checkCurrentUserLiked(CustomUserDetails currentUser, GroupOrder groupOrder, ResponseGroupOrderDetailDto dto) {
        if (isUserLogin(currentUser)) {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
            if (isCurrentUserLiked(groupOrder, user)) {
                dto.updateIsCheckLikeCurrentUser(true);
                return;
            }

            dto.updateIsCheckLikeCurrentUser(false);
        }
    }

    private boolean isCurrentUserLiked(GroupOrder groupOrder, User user) {
        return groupOrderLikeRepository.existsByUserIdAndGroupOrderId(user.getId(), groupOrder.getId());
    }

    private void addCommentToDto(Long groupOrderId, HttpServletRequest request, ResponseGroupOrderDetailDto dto) {
        List<ResponseGroupOrderCommentDto> commentDtos = buildCommentHierarchy(groupOrderId, request, dto);
        dto.updateGroupOrderCommentDtoList(commentDtos);
    }

    private List<ResponseGroupOrderCommentDto> buildCommentHierarchy(Long groupOrderId, HttpServletRequest request, ResponseGroupOrderDetailDto dto) {
        List<GroupOrderComment> parentComments = groupOrderCommentRepository
                .findByGroupOrderIdAndParentGroupOrderCommentIsNull(groupOrderId);

        if (parentComments.isEmpty()) {
            return new ArrayList<>();
        }

        return parentComments.stream()
                .map(parentComment -> buildCommentDto(parentComment, request))
                .toList();
    }

    private ResponseGroupOrderCommentDto buildCommentDto(GroupOrderComment parentComment, HttpServletRequest request) {
        ResponseGroupOrderCommentDto parentDto = convertCommentToDto(parentComment, request);
        addChildCommentToParentComment(request, parentComment, parentDto);
        return parentDto;
    }

    private void addChildCommentToParentComment(HttpServletRequest request, GroupOrderComment parentComment, ResponseGroupOrderCommentDto parentDto) {
        List<ResponseGroupOrderCommentDto> childDtos = parentComment.getChildGroupOrderComments().stream()
                .map(childComment -> convertCommentToDto(childComment, request))
                .toList();
        parentDto.updateChildGroupOrderCommentList(childDtos);
    }

    private ResponseGroupOrderCommentDto convertCommentToDto(GroupOrderComment comment, HttpServletRequest request) {
        ResponseGroupOrderCommentDto dto = ResponseGroupOrderCommentDto.from(comment);

        if (comment.isDeleted()) {
            setDeletedCommentInfo(dto);
        } else {
            setNotDeletedCommentInfo(comment, request, dto);
        }

        return dto;
    }

    private void setNotDeletedCommentInfo(GroupOrderComment comment, HttpServletRequest request, ResponseGroupOrderCommentDto dto) {
        dto.updateReply(comment.getReply());
        dto.updateUsername(comment.getUser().getName());

        String writerImageUrl = imageService.findStaticImageUrl(ImageType.USER, comment.getUser().getId(), request);
        dto.updateCommentAuthorImagePath(writerImageUrl);
    }

    private void setDeletedCommentInfo(ResponseGroupOrderCommentDto dto) {
        dto.updateReply("삭제된 메시지입니다.");
        dto.updateUsername("알 수 없는 사용자");
        dto.updateCommentAuthorImagePath(null);
    }

    private static List<ResponseGroupOrderPopularSearch> createDtoWithRank(List<GroupOrderPopularSearchKeyword> topKeywords) {
        return IntStream.range(0, topKeywords.size())
                .mapToObj(i -> ResponseGroupOrderPopularSearch.of(i + 1, topKeywords.get(i).getKeyword()))
                .toList();
    }

    private void addUserSearchLog(CustomUserDetails currentUser, String search) {
        if (currentUser != null && search != null) {
            User user = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
            user.addSearchLog(search);
        }
    }

    private static void updateExpiredGroupOrder(GroupOrder groupOrder, LocalDateTime now) {
        if (!groupOrder.isRecruitmentComplete() && now.isAfter(groupOrder.getDeadline())) {
            groupOrder.updateRecruitmentComplete(true);
        }
    }

    private String findRepresentativeImage(HttpServletRequest request, GroupOrder groupOrder) {
        List<ImageLinkDto> images = imageService.findImages(ImageType.GROUP_ORDER, groupOrder.getId(), request);

        String imagePath = null;
        if (!images.isEmpty()) {
            imagePath = images.get(0).getImageUrl();
        }
        return imagePath;
    }

    private void createGroupOrderLike(User user, GroupOrder groupOrder) {
        GroupOrderLike groupOrderLike = GroupOrderLike.builder()
                .user(user)
                .groupOrder(groupOrder)
                .build();

        groupOrderLikeRepository.save(groupOrderLike);
    }

    private void deleteGroupOrderLike(User user, GroupOrder groupOrder) {
        GroupOrderLike groupOrderLike = groupOrderLikeRepository.findByUserAndGroupOrder(user, groupOrder)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_LIKE_NOT_FOUND));

        groupOrderLikeRepository.delete(groupOrderLike);
    }

    private static boolean isChangeNewOpenChatLink(RequestGroupOrderDto requestGroupOrderDto, GroupOrder groupOrder) {
        String newLink = requestGroupOrderDto.getLink();
        String oldLink = groupOrder.getLink();

        return newLink != null && !newLink.isEmpty() && !newLink.equals(oldLink);
    }

    private static boolean isGroupOrderOwnByUser(Long userId, GroupOrder groupOrder) {
        return !Objects.equals(groupOrder.getUser().getId(), userId);
    }
}
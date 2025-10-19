package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.*;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.*;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.*;
import com.example.appcenter_project.enums.ApiType;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.enums.user.NotificationType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.repository.groupOrder.*;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import com.example.appcenter_project.service.image.ImageService;
import com.example.appcenter_project.service.notification.GroupOrderNotificationOrderService;
import com.example.appcenter_project.utils.MealTimeChecker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderService {

    private final GroupOrderRepository groupOrderRepository;
    private final UserRepository userRepository;
    private final GroupOrderLikeRepository groupOrderLikeRepository;
    private final GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    private final UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;
    private final GroupOrderCommentRepository groupOrderCommentRepository;
    private final ImageRepository imageRepository;
    private final GroupOrderMapper groupOrderMapper;
    private final GroupOrderPopularSearchKeywordRepository groupOrderPopularSearchKeywordRepository;
    private final ImageService imageService;
    private final AsyncViewCountService asyncViewCountService;
    private final MealTimeChecker mealTimeChecker;
    private final FcmMessageService fcmMessageService;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final GroupOrderNotificationOrderService groupOrderNotificationOrderService;


    public void saveGroupOrder(Long userId, RequestGroupOrderDto requestGroupOrderDto) {
        // GroupOrder 저장
        User user = userRepository.findById(userId).orElseThrow();
        GroupOrder groupOrder = RequestGroupOrderDto.dtoToEntity(requestGroupOrderDto, user);

        user.getGroupOrderList().add(groupOrder);

        groupOrderRepository.save(groupOrder);

        // GroupOrderChatRoom 저장
        GroupOrderChatRoom groupOrderChatRoom = new GroupOrderChatRoom(groupOrder.getTitle());
        UserGroupOrderChatRoom userGroupOrderChatRoom = UserGroupOrderChatRoom.builder()
                .groupOrderChatRoom(groupOrderChatRoom)
                .user(user)
                .build();
        // User - UserGroupOrderChatRoom 1대 N 매핑
        user.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);

        // GroupOrder - GroupOrderChatRoom 1대 1 양방향 매핑
        groupOrder.updateGroupOrderChatRoom(groupOrderChatRoom);
        groupOrderChatRoom.updateGroupOrder(groupOrder);

        groupOrderChatRoomRepository.save(groupOrderChatRoom);
        userGroupOrderChatRoomRepository.save(userGroupOrderChatRoom);
    }

    public ResponseGroupOrderDetailDto findGroupOrderById(CustomUserDetails jwtUser, Long groupOrderId, HttpServletRequest request) {
        if (mealTimeChecker.isMealTime()) {
            asyncViewCountService.incrementViewCount(groupOrderId);
        } else {
            GroupOrder groupOrder = groupOrderRepository.findByIdWithLock(groupOrderId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
            groupOrder.plusViewCount();
        }
        return getResponseGroupOrderDetailDto(jwtUser, groupOrderId, request);
    }

    private ResponseGroupOrderDetailDto getResponseGroupOrderDetailDto(CustomUserDetails jwtUser, Long groupOrderId, HttpServletRequest request) {
        GroupOrder groupOrder = groupOrderRepository.findByIdWithLock(groupOrderId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        // 마감시간 지난 공동구매 isRecruitmentComplete true로 변경
        LocalDateTime now = LocalDateTime.now();

        if(!groupOrder.isRecruitmentComplete() && now.isAfter(groupOrder.getDeadline())) {
            groupOrder.updateRecruitmentComplete(true);
        }

        List<ResponseGroupOrderCommentDto> flatComments = new ArrayList<>();

        for (GroupOrderComment groupOrderComment : groupOrder.getGroupOrderCommentList()) {
            flatComments.add(ResponseGroupOrderCommentDto.entityToDto(groupOrderComment));
        }

/*

        ResponseGroupOrderDetailDto flatDto = groupOrderMapper.findGroupOrderById(groupOrderId);
        if (flatDto == null) {
            throw new CustomException(GROUP_ORDER_NOT_FOUND);
        }

        List<ResponseGroupOrderCommentDto> flatComments = flatDto.getGroupOrderCommentDtoList();

*/

        Map<Long, ResponseGroupOrderCommentDto> parentMap = new LinkedHashMap<>();
        List<ResponseGroupOrderCommentDto> topLevelComments = new ArrayList<>();

        for (ResponseGroupOrderCommentDto comment : flatComments) {
            // 삭제된 댓글이 아닐 때만 이미지 경로 업데이트
            if (Boolean.TRUE.equals(comment.getIsDeleted())) {
                comment.updateCommentAuthorImagePath(null);
            } else {
                comment.updateCommentAuthorImagePath(imageService.findStaticImageUrl(ImageType.USER, comment.getUserId(), request));
            }

            // 계층 구조 구성
            if (comment.getParentId() == null) {
                comment.updateChildGroupOrderCommentList(new ArrayList<>());
                parentMap.put(comment.getGroupOrderCommentId(), comment);
                topLevelComments.add(comment);
            } else {
                ResponseGroupOrderCommentDto parent = parentMap.get(comment.getParentId());
                if (parent != null) {
                    if (parent.getChildGroupOrderCommentList() == null) {
                        parent.updateChildGroupOrderCommentList(new ArrayList<>());
                    }
                    parent.getChildGroupOrderCommentList().add(comment);
                }
            }
        }

        ResponseGroupOrderDetailDto flatDto = ResponseGroupOrderDetailDto.entityToDto(groupOrder, topLevelComments);

        // 해당 게시글의 작성자인지 검증
        if (jwtUser != null) {
            User currentUser = userRepository.findById(jwtUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
            if (groupOrder.getUser().getId() == currentUser.getId()) {
                flatDto.updateIsMyPost(true);
            }
        }

        // 해당 글을 좋아요 눌렀는지 검증
        if (jwtUser != null) {
            User currentUser = userRepository.findById(jwtUser.getId()).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
            if (groupOrderLikeRepository.existsByUserIdAndGroupOrderId(currentUser.getId(), groupOrderId)) {
                flatDto.updateIsCheckLikeCurrentUser(true);
            } else {
                flatDto.updateIsCheckLikeCurrentUser(false);
            }
        }

        User findUser = groupOrder.getUser();

        String userImageUrl = getRepresentativeUserImageUrl(request, findUser);
        flatDto.updateAuthorImagePath(userImageUrl);
/*
        // 게시글 작성자의 평점 조회
        Float averageRating = groupOrder.getUser().getAverageRating();
        flatDto.updateAuthorRating(averageRating);

*/
        return flatDto;
    }

    private String getRepresentativeUserImageUrl(HttpServletRequest request, User findUser) {
        List<ImageLinkDto> imageLinkDtos = imageService.findImages(ImageType.USER, findUser.getId(), request);

        if (imageLinkDtos.isEmpty()) {
            return null;
        }

        return imageLinkDtos.get(0).getImageUrl();
    }


    // 이미지와 함께 공동구매 생성
    public void saveGroupOrder(Long userId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        GroupOrder groupOrder = RequestGroupOrderDto.dtoToEntity(requestGroupOrderDto, user);

        user.getGroupOrderList().add(groupOrder);

        groupOrderRepository.save(groupOrder);

        imageService.saveImages(ImageType.GROUP_ORDER, groupOrder.getId(), images);

        groupOrderNotificationOrderService.saveAndSendGroupOrderNotification(groupOrder);
    }

    public List<ImageLinkDto> findGroupOrderImageUrls(Long groupOrderId, HttpServletRequest request) {
        return imageService.findImages(ImageType.GROUP_ORDER, groupOrderId, request);
    }

    public List<ResponseGroupOrderDto> findGroupOrders(CustomUserDetails jwtUser, GroupOrderSort sort, GroupOrderType type, String search, HttpServletRequest request) {
        if (jwtUser != null && search != null) {
                User user = userRepository.findById(jwtUser.getId())
                        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
                user.addSearchLog(search);
        }

        // todo 전체 조회 로직 필요


        // 마감시간 지난 공동구매 isRecruitmentComplete true로 변경
        List<ResponseGroupOrderDto> responseGroupOrderDtoList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (GroupOrder groupOrder : groupOrderRepository.findGroupOrdersComplex(sort, type, search)) {
            if(!groupOrder.isRecruitmentComplete() && now.isAfter(groupOrder.getDeadline())) {
                groupOrder.updateRecruitmentComplete(true);
            }

            List<ImageLinkDto> images = imageService.findImages(ImageType.GROUP_ORDER, groupOrder.getId(), request);

            String imagePath = null;
            if (!images.isEmpty()) {
                imagePath = images.get(0).getImageUrl();
            }

            responseGroupOrderDtoList.add(ResponseGroupOrderDto.entityToDto(groupOrder, imagePath));
        }

        if (sort == GroupOrderSort.LATEST) {
            Collections.reverse(responseGroupOrderDtoList);
        }

        return responseGroupOrderDtoList;
    }


    public void updateGroupOrder(Long userId, Long groupOrderId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));

/*        if (groupOrderRepository.existsByTitle(requestGroupOrderDto.getTitle())) {
            throw new CustomException(GROUP_ORDER_TITLE_DUPLICATE);
        }*/

        groupOrder.update(requestGroupOrderDto);

        imageService.updateGroupOrderImages(ImageType.GROUP_ORDER, groupOrderId, images);

        if (!(requestGroupOrderDto.getOpenChatLink() == null || requestGroupOrderDto.getOpenChatLink() == "")) {
            Notification likeNotification = Notification.builder()
                    .title("좋아요한 공동구매 게시글의 오픈채팅방이 만들어졌어요!")
                    .body(groupOrder.getTitle())
                    .notificationType(NotificationType.GROUP_ORDER)
                    .apiType(ApiType.GROUP_ORDER)
                    .build();

            notificationRepository.save(likeNotification);

            for (GroupOrderLike groupOrderLike : groupOrder.getGroupOrderLikeList()) {
                User user = groupOrderLike.getUser();

                UserNotification userNotification = UserNotification.of(user, likeNotification);
                userNotificationRepository.save(userNotification);

                fcmMessageService.sendNotification(user, likeNotification.getTitle(), likeNotification.getBody());
            }

            Notification commentNotification = Notification.builder()
                    .title("댓글을 단 공동구매 게시글의 오픈채팅방이 만들어졌어요!")
                    .body(groupOrder.getTitle())
                    .notificationType(NotificationType.GROUP_ORDER)
                    .apiType(ApiType.GROUP_ORDER)
                    .build();

            notificationRepository.save(commentNotification);

            for (GroupOrderComment groupOrderComment : groupOrder.getGroupOrderCommentList()) {
                User user = groupOrderComment.getUser();

                UserNotification userNotification = UserNotification.of(user, commentNotification);
                userNotificationRepository.save(userNotification);

                fcmMessageService.sendNotification(user, commentNotification.getTitle(), commentNotification.getBody());
            }
        }
    }

    public void deleteGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));
        imageService.deleteImages(ImageType.GROUP_ORDER, groupOrderId);
        groupOrderRepository.delete(groupOrder);
    }

    public Integer likePlusGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 좋아요를 누른 유저가 또 좋아요를 할려는 경우 예외처리
        if (groupOrderLikeRepository.existsByUserAndGroupOrder(user, groupOrder)) {
            throw new CustomException(ALREADY_GROUP_ORDER_LIKE_USER);
        }

        GroupOrderLike groupOrderLike = GroupOrderLike.builder()
                .user(user)
                .groupOrder(groupOrder)
                .build();

        groupOrderLikeRepository.save(groupOrderLike);

        // user에 좋아요 정보 추가
        user.addGroupOrderLike(groupOrderLike);

        groupOrder.getGroupOrderLikeList().add(groupOrderLike);

        return groupOrder.plusLike();
    }

    public Integer likeMinusGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 좋아요를 누르지 않은 유저가 좋아요 취소를 시도한 경우 예외처리
        if (!groupOrderLikeRepository.existsByUserAndGroupOrder(user, groupOrder)) {
            throw new CustomException(GROUP_ORDER_LIKE_NOT_FOUND);
        }

        GroupOrderLike groupOrderLike = groupOrderLikeRepository.findByUserAndGroupOrder(user, groupOrder)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_LIKE_NOT_FOUND));

        // user에서 좋아요 정보 제거
        user.removeGroupOrderLike(groupOrderLike);

        groupOrder.getGroupOrderLikeList().remove(groupOrderLike);

        groupOrderLikeRepository.delete(groupOrderLike);

        return groupOrder.minusLike();
    }


    public List<String> findGroupOrderSearchLog(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        return user.getSearchLogs();
    }

    public void addRating(CustomUserDetails user, Long groupOrderId, Float ratingScore) {
        if (user == null) {
            throw new CustomException(USER_NOT_FOUND);
        }

        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        groupOrder.getUser().addRating(ratingScore);
    }

    public void completeGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        groupOrder.updateRecruitmentComplete(true);
    }

    public void unCompleteGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        groupOrder.updateRecruitmentComplete(false);
    }

    public List<ResponseGroupOrderPopularSearch> findGroupOrderPopularSearch() {
        int index = 1;
        List<ResponseGroupOrderPopularSearch> responseGroupOrderPopularSearchList = new ArrayList<>();
        List<GroupOrderPopularSearchKeyword> top10Popular = groupOrderPopularSearchKeywordRepository.findTop10ByOrderBySearchCountDesc();
        for (GroupOrderPopularSearchKeyword groupOrderPopularSearchKeyword : top10Popular) {
            ResponseGroupOrderPopularSearch responseGroupOrderPopularSearch = new ResponseGroupOrderPopularSearch(index, groupOrderPopularSearchKeyword.getKeyword());
            responseGroupOrderPopularSearchList.add(responseGroupOrderPopularSearch);
            index += 1;
        }

        return responseGroupOrderPopularSearchList;
    }

    public void deleteGroupOrderImage(Long userId, String imageName) {
        Image image = imageRepository.findByImageName(imageName).orElseThrow(() -> new CustomException(IMAGE_NOT_FOUND));
        GroupOrder groupOrder = groupOrderRepository.findById(image.getEntityId()).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        if (groupOrder.getUser().getId() != userId) {
            throw new CustomException(IMAGE_UPDATE_NOT_ALLOWED);
        }

        imageService.deleteImage(image);
    }
}

package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.*;
import com.example.appcenter_project.entity.groupOrder.*;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.notification.Notification;
import com.example.appcenter_project.entity.notification.UserNotification;
import com.example.appcenter_project.entity.user.*;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.repository.groupOrder.*;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.repository.notification.NotificationRepository;
import com.example.appcenter_project.repository.notification.UserNotificationRepository;
import com.example.appcenter_project.repository.user.UserGroupOrderCategoryRepository;
import com.example.appcenter_project.repository.user.UserGroupOrderKeywordRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.fcm.FcmMessageService;
import com.example.appcenter_project.service.image.ImageService;
import com.example.appcenter_project.utils.MealTimeChecker;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.appcenter_project.enums.user.NotificationType.*;
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
    private final UserGroupOrderKeywordRepository userGroupOrderKeywordRepository;
    private final FcmMessageService fcmMessageService;
    private final UserGroupOrderCategoryRepository userGroupOrderCategoryRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;

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
            // 삭제된 댓글 처리
            if (Boolean.TRUE.equals(comment.getIsDeleted())) {
                comment.updateReply("삭제된 메시지입니다.");
            }



            comment.updateCommentAuthorImagePath(imageService.findStaticImageUrl(ImageType.USER, comment.getUserId(), request));

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

        // 키워드, 카테고리 중복 알림 방지 유저 목록
        Set<User> receivedUsers = new HashSet<>();

        // 키워드를 등록한 유저에게 푸시 알림 전송(제목 또는 내용에 키워드가 포함되어 있는 경우)
        keywordNotificationUser(groupOrder, receivedUsers);

        // 카테고리를 등록한 유저에게 푸시 알림 전송
        categoryNotificationUser(groupOrder, receivedUsers);
    }

    private void keywordNotificationUser(GroupOrder groupOrder, Set<User> receivedUsers) {
        List<UserGroupOrderKeyword> allKeyword = userGroupOrderKeywordRepository.findAll();
        List<Long> sendUser = new ArrayList<>();


        for (UserGroupOrderKeyword userGroupOrderKeyword : allKeyword) {
            if (groupOrder.getTitle().contains(userGroupOrderKeyword.getKeyword()) || groupOrder.getDescription().contains(userGroupOrderKeyword.getKeyword())) {
                if (!sendUser.contains(userGroupOrderKeyword.getId())) {
                    sendUser.add(userGroupOrderKeyword.getId());

                    // 알림 저장
                    User user = userGroupOrderKeyword.getUser();

                    String title = "[" +  userGroupOrderKeyword.getKeyword() + "]" + " 공동구매 게시글이 등록되었습니다.";
                    String body = groupOrder.getTitle();


                    Notification notification = Notification.builder()
                            .boardId(groupOrder.getId())
                            .title(title)
                            .body(body)
                            .notificationType(GROUP_ORDER)
                            .build();

                    notificationRepository.save(notification);

                    UserNotification userNotification = UserNotification.builder()
                            .user(user)
                            .notification(notification)
                            .build();

                    userNotificationRepository.save(userNotification);

                    // 알림 수신 선택이 되어 있는 유저한테만 알림 전송
                    if (user.getReceiveNotificationTypes().contains(GROUP_ORDER)) {
                        fcmMessageService.sendNotification(user, title, body);
                    }

                    // 키워드 알림을 받은 유저로 등록, 카테고리 알림은 받으면 안됨
                    receivedUsers.add(user);
                }
            }

        }

    }


    private void categoryNotificationUser(GroupOrder groupOrder, Set<User> receivedUsers) {
        GroupOrderType groupOrderType = groupOrder.getGroupOrderType();

        List<UserGroupOrderCategory> allCategory = userGroupOrderCategoryRepository.findAll();
        for (UserGroupOrderCategory category : allCategory) {

            // 알림 저장
            User user = category.getUser();

            // 이미 키워드 알림을 받은 유저는 카테고리 알림에서는 제외
            if (receivedUsers.contains(user)) {
                return;
            }

            String title = "[" + groupOrderType.toValue() + "]" + " 공동구매 게시글이 등록되었습니다.";
            String body = groupOrder.getTitle();

            Notification notification = Notification.builder()
                    .boardId(groupOrder.getId())
                    .title(title)
                    .body(body)
                    .notificationType(GROUP_ORDER)
                    .build();

            notificationRepository.save(notification);

            UserNotification userNotification = UserNotification.builder()
                    .user(user)
                    .notification(notification)
                    .build();

            userNotificationRepository.save(userNotification);

            // 알림 전송
            if (category.getCategory() == groupOrderType) {
                // 알림 수신 선택이 되어 있는 유저한테만 알림 전송
                if (user.getReceiveNotificationTypes().contains(GROUP_ORDER)) {
                    fcmMessageService.sendNotification(user, title, body);
                }
            }
        }
    }

    public List<ImageLinkDto> findGroupOrderImageUrls(Long groupOrderId, HttpServletRequest request) {
        return imageService.findImages(ImageType.GROUP_ORDER, groupOrderId, request);
    }



    // 정적 GroupOrder 이미지 URL 생성 헬퍼 메소드
    private String getStaticGroupOrderImageUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/images/group-order/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for group-order image path: {}", filePath);
            return null;
        }
    }

    // 유틸리티: 베이스 URL 생성 (ImageService와 동일)
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        // 기본 포트가 아닌 경우에만 포트 추가
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }

        baseUrl.append(contextPath);
        return baseUrl.toString();
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

        imageService.updateImages(ImageType.GROUP_ORDER, groupOrderId, images);
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
}

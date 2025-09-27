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
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

            comment.updateCommentAuthorImagePath(imageService.findUserImageUrlByUserId(comment.getUserId(), request).getFileName());

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

        String userImageUrl = imageService.findUserImageUrlByUserId(findUser.getId(), request).getFileName();
        flatDto.updateAuthorImagePath(userImageUrl);

        // 게시글 조회 수 증가
        groupOrder.plusViewCount();

/*
        // 게시글 작성자의 평점 조회
        Float averageRating = groupOrder.getUser().getAverageRating();
        flatDto.updateAuthorRating(averageRating);

*/
        return flatDto;
    }


    // 이미지와 함께 공동구매 생성
    public void saveGroupOrder(Long userId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        GroupOrder groupOrder = RequestGroupOrderDto.dtoToEntity(requestGroupOrderDto, user);

        user.getGroupOrderList().add(groupOrder);

        groupOrderRepository.save(groupOrder);

        saveImages(groupOrder, images);

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

        // 키워드를 등록한 유저에게 푸시 알림 전송(제목 또는 내용에 키워드가 포함되어 있는 경우)
        keywordNotificationUser(groupOrder);

        // 카테고리를 등록한 유저에게 푸시 알림 전송
        categoryNotificationUser(groupOrder);
    }

    private void categoryNotificationUser(GroupOrder groupOrder) {
        GroupOrderType groupOrderType = groupOrder.getGroupOrderType();

        List<UserGroupOrderCategory> allCategory = userGroupOrderCategoryRepository.findAll();
        for (UserGroupOrderCategory category : allCategory) {

            // 알림 저장
            User user = category.getUser();

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

    private void keywordNotificationUser(GroupOrder groupOrder) {
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
                }
            }

        }

    }

    private void saveImages(GroupOrder groupOrder, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // User와 동일한 방식으로 경로 설정
            String basePath = System.getProperty("user.dir");
            String imagePath = basePath + "/images/group-order/";

            File directory = new File(imagePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
                if (!created) {
                    log.error("Failed to create group-order directory: {}", imagePath);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }

            for (MultipartFile file : files) {
                // User 방식과 동일한 파일명 생성 패턴
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String imageFileName = "grouporder_" + groupOrder.getId() + "_" + uuid + fileExtension;
                File destinationFile = new File(imagePath + imageFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("GroupOrder image saved successfully: {}", destinationFile.getAbsolutePath());

                    Image image = Image.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .isDefault(false)
                            .imageType(ImageType.GROUP_ORDER)
                            .boardId(groupOrder.getId())
                            .build();

                    imageRepository.save(image);
                    groupOrder.getImageList().add(image);

                } catch (IOException e) {
                    log.error("Failed to save group-order image for groupOrder {}: ", groupOrder.getId(), e);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }
        }
    }

    public List<ImageLinkDto> findGroupOrderImageUrls(Long groupOrderId, HttpServletRequest request) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        List<Image> imageList = groupOrder.getImageList();
        List<ImageLinkDto> imageLinkDtos = new ArrayList<>();

        for (Image image : imageList) {
            ImageLinkDto groupOrderImage = getGroupOrderImage(image, request);
            imageLinkDtos.add(groupOrderImage);
        }

        return imageLinkDtos;
    }

    public ImageLinkDto getGroupOrderImage(Image image, HttpServletRequest request) {
        File file = new File(image.getFilePath());
        log.info("Checking group-order image file: {}", image.getFilePath());
        log.info("File exists: {}", file.exists());

        if (!file.exists()) {
            log.error("GroupOrder image file not found: {}", image.getFilePath());
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        // 이미지 URL 생성
        String baseUrl = getBaseUrl(request);
        String imageUrl = baseUrl + "/api/images/group-order/" + image.getId();

        // 정적 리소스 URL 생성
        String staticImageUrl = getStaticGroupOrderImageUrl(image.getFilePath(), baseUrl);
        String changeUrl = staticImageUrl.replace("http", "https");

        // 안전한 컨텐츠 타입 확인
        String contentType = getSafeContentType(file);

        ImageLinkDto imageLinkDto = ImageLinkDto.builder()
                .imageUrl(imageUrl)
                .fileName(changeUrl)
                .contentType(contentType)
                .fileSize(file.length())
                .build();

        return imageLinkDto;
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

    public List<GroupOrderImageDto> findGroupOrderImages(Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        List<Image> imageList = groupOrder.getImageList();
        List<GroupOrderImageDto> groupOrderImageDtoList = new ArrayList<>();

        for (Image image : imageList) {
            File file = new File(image.getFilePath());
            if (!file.exists()) {
                log.error("GroupOrder image file not found: {}", image.getFilePath());
                throw new CustomException(IMAGE_NOT_FOUND);
            }

            // User 방식과 동일한 안전한 컨텐츠 타입 확인
            String contentType = getSafeContentType(file);

            // 실제 파일명 추출 (경로에서 파일명만)
            String filename = file.getName();

            GroupOrderImageDto groupOrderImageDto = GroupOrderImageDto.builder()
                    .filename(filename)
                    .contentType(contentType)
                    .build();

            groupOrderImageDtoList.add(groupOrderImageDto);
        }

        return groupOrderImageDtoList;
    }

    public Resource loadImageAsResource(String filename) {
        // User 방식과 동일한 경로 사용
        String imagePath = System.getProperty("user.dir") + "/images/group-order/";
        File file = new File(imagePath + filename);

        if (!file.exists()) {
            log.error("GroupOrder image file not found: {}", file.getAbsolutePath());
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }

    // User 방식과 동일한 안전한 컨텐츠 타입 확인 메소드
    private String getSafeContentType(File file) {
        try {
            String fileName = file.getName().toLowerCase();

            // 확장자 기반으로 먼저 판단 (더 안정적)
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
                return "image/jpeg";
            } else if (fileName.endsWith(".png")) {
                return "image/png";
            } else if (fileName.endsWith(".gif")) {
                return "image/gif";
            } else if (fileName.endsWith(".webp")) {
                return "image/webp";
            } else if (fileName.endsWith(".svg")) {
                return "image/svg+xml";
            }

            // Files.probeContentType이 실패할 수 있으므로 try-catch
            try {
                String detectedType = Files.probeContentType(file.toPath());
                if (detectedType != null && detectedType.startsWith("image/")) {
                    return detectedType;
                }
            } catch (Exception e) {
                log.warn("Could not probe content type for file: {}", file.getName());
            }

            // 기본값
            return "image/jpeg";

        } catch (Exception e) {
            log.error("Error determining content type for file: {}", file.getName(), e);
            return "image/jpeg"; // 안전한 기본값
        }
    }

    // User 방식과 동일한 파일 확장자 추출 메소드
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".jpg"; // 기본 확장자
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".jpg"; // 확장자가 없으면 기본값
        }

        return fileName.substring(lastDotIndex).toLowerCase();
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

        for (GroupOrder groupOrdersComplex : groupOrderRepository.findGroupOrdersComplex(sort, type, search)) {
            if(!groupOrdersComplex.isRecruitmentComplete() && now.isAfter(groupOrdersComplex.getDeadline())) {
                groupOrdersComplex.updateRecruitmentComplete(true);
            }

            responseGroupOrderDtoList.add(ResponseGroupOrderDto.entityToDto(groupOrdersComplex, request));
        }

        return responseGroupOrderDtoList;
    }

    // 이미지 경로를 User 방식의 URL로 변환하는 헬퍼 메소드
    private void convertImagePathToUrl(ResponseGroupOrderDto dto, HttpServletRequest request) {
        if (dto.getFilePath() != null && !dto.getFilePath().isEmpty()) {
            try {
                // 절대 경로에서 파일명만 추출
                String fileName = Paths.get(dto.getFilePath()).getFileName().toString();

                // User 방식과 동일한 URL 패턴으로 변환
                String baseUrl = getBaseUrl(request);
                String imageUrl = baseUrl.replace("http:", "https:") + "/images/group-order/" + fileName;

                // DTO의 filePath를 변환된 URL로 업데이트
                // Reflection을 사용하여 private 필드에 접근
                java.lang.reflect.Field filePathField = ResponseGroupOrderDto.class.getSuperclass().getDeclaredField("filePath");
                filePathField.setAccessible(true);
                filePathField.set(dto, imageUrl);

                log.debug("Converted GroupOrder image path: {} -> {}", dto.getFilePath(), imageUrl);
            } catch (Exception e) {
                log.warn("Failed to update image path for GroupOrder {}: {}", dto.getBoardId(), e.getMessage());
            }
        }
    }

    public void updateGroupOrder(Long userId, Long groupOrderId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));

        if (groupOrderRepository.existsByTitle(requestGroupOrderDto.getTitle())) {
            throw new CustomException(GROUP_ORDER_TITLE_DUPLICATE);
        }

        groupOrder.update(requestGroupOrderDto);

        // 이미지가 제공된 경우에만 기존 이미지를 삭제하고 새로운 이미지를 저장
        if (images != null && !images.isEmpty()) {
            // 기존 이미지들이 있다면 파일 및 DB에서 삭제
            List<Image> existingImages = groupOrder.getImageList();
            for (Image existingImage : existingImages) {
                File oldFile = new File(existingImage.getFilePath());
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    if (!deleted) {
                        log.warn("Failed to delete old GroupOrder image file: {}", existingImage.getFilePath());
                    }
                }
                // 기존 이미지 엔티티 삭제
                imageRepository.delete(existingImage);
            }
            groupOrder.getImageList().clear();

            // 새로운 이미지들 저장
            saveImages(groupOrder, images);
        }

        // 키워드를 등록한 유저에게 푸시 알림 전송(제목 또는 내용에 키워드가 포함되어 있는 경우)
        keywordNotificationUser(groupOrder);
    }

    public void deleteGroupOrder(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));

        List<Image> existingImages = groupOrder.getImageList();
        for (Image existingImage : existingImages) {
            File oldFile = new File(existingImage.getFilePath());
            if (oldFile.exists()) {
                boolean deleted = oldFile.delete();
                if (!deleted) {
                    log.warn("Failed to delete old GroupOrder image file: {}", existingImage.getFilePath());
                }
            }
            // 기존 이미지 엔티티 삭제
            imageRepository.delete(existingImage);
        }
        groupOrder.getImageList().clear();
        groupOrderRepository.delete(groupOrder);
    }

    private Specification<GroupOrder> buildSpecification(GroupOrderType type, Optional<String> search) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != GroupOrderType.ALL) {
                predicates.add(criteriaBuilder.equal(root.get("groupOrderType"), type));
            }

            search.filter(s -> !s.isEmpty())
                    .ifPresent(s -> predicates.add(criteriaBuilder.like(root.get("title"), "%" + s + "%")));
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Sort getSortOption(GroupOrderSort sort) {
        return switch (sort) {
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
            case POPULARITY -> Sort.by(Sort.Direction.DESC, "groupOrderLike");
            default -> Sort.by(Sort.Direction.ASC, "deadline");
        };
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


    // 하나의 GroupOrder 게시판에 있는 모든 GroupOrderComment 조회
    private List<ResponseGroupOrderCommentDto> findGroupOrderComment(GroupOrder groupOrder) {
        List<ResponseGroupOrderCommentDto> responseGroupOrderCommentDtoList = new ArrayList<>();
        List<GroupOrderComment> groupOrderCommentList = groupOrderCommentRepository.findByGroupOrder_IdAndParentGroupOrderCommentIsNull(groupOrder.getId());
        for (GroupOrderComment groupOrderComment : groupOrderCommentList) {
            List<ResponseGroupOrderCommentDto> childResponseComments = new ArrayList<>();
            List<GroupOrderComment> childGroupOrderComments = groupOrderComment.getChildGroupOrderComments();
            for (GroupOrderComment childGroupOrderComment : childGroupOrderComments) {
                ResponseGroupOrderCommentDto build = ResponseGroupOrderCommentDto.builder()
                        .groupOrderCommentId(childGroupOrderComment.getId())
                        .userId(childGroupOrderComment.getUser().getId())
                        .reply(childGroupOrderComment.isDeleted() ? "삭제된 메시지입니다." : childGroupOrderComment.getReply())
                        .build();

                childResponseComments.add(build);
            }
            ResponseGroupOrderCommentDto responseGroupOrderCommentDto = ResponseGroupOrderCommentDto.builder()
                    .groupOrderCommentId(groupOrderComment.getId())
                    .userId(groupOrderComment.getUser().getId())
                    .reply(groupOrderComment.isDeleted() ? "삭제된 메시지입니다." : groupOrderComment.getReply())
                    .childGroupOrderCommentList(childResponseComments)
                    .build();
            responseGroupOrderCommentDtoList.add(responseGroupOrderCommentDto);

        }
        return responseGroupOrderCommentDtoList;
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

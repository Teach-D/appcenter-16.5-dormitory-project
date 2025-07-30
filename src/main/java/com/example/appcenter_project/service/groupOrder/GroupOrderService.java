package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.GroupOrderImageDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.mapper.GroupOrderMapper;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderCommentRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.groupOrder.UserGroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.image.ImageRepository;
import com.example.appcenter_project.repository.like.GroupOrderLikeRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private final DeliveryCacheService deliveryCacheService;

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

    /**
     * groupOrderId로 GroupOrder 조회
     * 해당 GroupOrder가 redis에 캐싱되어 있으면 redis에서 조회
     * 해당 GroupOrder가 redis에 캐싱되어 있지 않으면 db에서 조회
     */
    public ResponseGroupOrderDetailDto findGroupOrderById(Long groupOrderId) {
        ResponseGroupOrderDetailDto dto;

        // 캐시에서 먼저 조회
        if (deliveryCacheService.existsGroupOrderInCache(groupOrderId)) {
            dto = deliveryCacheService.getAllCacheDelivery(groupOrderId);
            log.info("redis 조회");
            if (dto != null) {
                return buildHierarchicalComments(dto, true);
            }
        }

        log.info("db 조회");
        // DB에서 조회
        dto = groupOrderMapper.findGroupOrderById(groupOrderId);
        if (dto == null) {
            throw new CustomException(GROUP_ORDER_NOT_FOUND);
        }

        groupOrderMapper.plusViewCount(groupOrderId);

        return buildHierarchicalComments(dto, false);
    }

    /**
     * GroupOrder의 comment를 트리 형태로 계층화
     */
    private ResponseGroupOrderDetailDto buildHierarchicalComments(ResponseGroupOrderDetailDto dto, boolean isFromCache) {
        List<ResponseGroupOrderCommentDto> comments = dto.getGroupOrderCommentDtoList();

        // 캐시에서 온 데이터는 정렬 필요
        if (isFromCache) {
            comments = comments.stream()
                    .sorted(Comparator.comparing(ResponseGroupOrderCommentDto::getGroupOrderCommentId))
                    .toList();
        }

        Map<Long, ResponseGroupOrderCommentDto> parentMap = new LinkedHashMap<>();
        List<ResponseGroupOrderCommentDto> topLevelComments = new ArrayList<>();

        for (ResponseGroupOrderCommentDto comment : comments) {
            // 삭제된 댓글 처리
            if (Boolean.TRUE.equals(comment.getIsDeleted())) {
                if (isFromCache) {
                    comment.setReply("삭제된 메시지입니다.");
                } else {
                    comment.updateReply("삭제된 메시지입니다.");
                }
            }

            // 계층 구조 구성
            if (comment.getParentId() == null) {
                // 최상위 댓글
                if (isFromCache) {
                    comment.setChildGroupOrderCommentList(new ArrayList<>());
                } else {
                    comment.updateChildGroupOrderCommentList(new ArrayList<>());
                }
                parentMap.put(comment.getGroupOrderCommentId(), comment);
                topLevelComments.add(comment);
            } else {
                // 대댓글
                ResponseGroupOrderCommentDto parent = parentMap.get(comment.getParentId());
                if (parent != null) {
                    if (parent.getChildGroupOrderCommentList() == null) {
                        if (isFromCache) {
                            parent.setChildGroupOrderCommentList(new ArrayList<>());
                        } else {
                            parent.updateChildGroupOrderCommentList(new ArrayList<>());
                        }
                    }
                    parent.getChildGroupOrderCommentList().add(comment);
                }
            }
        }

        // 최상위 댓글만 설정
        if (isFromCache) {
            dto.setGroupOrderCommentDtoList(topLevelComments);
        } else {
            dto.updateGroupOrderCommentDtoList(topLevelComments);
        }

        return dto;
    }


    /**
     * GroupOrder 중에 Delivery만 조회
     */
    @Transactional(readOnly = true)
    public List<GroupOrder> findGroupOrdersDelivery() {
        return groupOrderMapper.findAllGroupOrdersWithDetails();
        // 1단계: 기본 정보와 이미지 로딩
        //return findAllGroupOrders();
    }

    public List<GroupOrder> findAllGroupOrders() {
        log.info("단계별 조회 시작");

        // 1단계: 기본 정보 + 이미지 + 유저 + 채팅방 조회
        List<GroupOrder> groupOrders = groupOrderRepository.findAllWithBasicInfo();
        log.info("1단계 완료: {} 개의 GroupOrder 조회", groupOrders.size());

        if (groupOrders.isEmpty()) {
            return groupOrders;
        }

        // ID 리스트 추출
        List<Long> groupOrderIds = groupOrders.stream()
                .map(GroupOrder::getId)
                .collect(Collectors.toList());

        // 빠른 조회를 위한 Map 생성
        Map<Long, GroupOrder> groupOrderMap = groupOrders.stream()
                .collect(Collectors.toMap(GroupOrder::getId, Function.identity()));

        // 2단계: 좋아요 정보 조회 및 병합
        List<GroupOrder> withLikes = groupOrderRepository.findAllWithLikes(groupOrderIds);
        log.info("2단계 완료: {} 개의 좋아요 정보 조회", withLikes.size());

        withLikes.forEach(go -> {
            GroupOrder original = groupOrderMap.get(go.getId());
            if (original != null) {
                // 기존 리스트를 클리어하고 새로운 데이터로 교체
                original.getGroupOrderLikeList().clear();
                original.getGroupOrderLikeList().addAll(go.getGroupOrderLikeList());
            }
        });

        // 3단계: 댓글 정보 조회 및 병합
        List<GroupOrder> withComments = groupOrderRepository.findAllWithComments(groupOrderIds);
        log.info("3단계 완료: {} 개의 댓글 정보 조회", withComments.size());

        withComments.forEach(go -> {
            GroupOrder original = groupOrderMap.get(go.getId());
            if (original != null) {
                // 기존 리스트를 클리어하고 새로운 데이터로 교체
                original.getGroupOrderCommentList().clear();
                original.getGroupOrderCommentList().addAll(go.getGroupOrderCommentList());
            }
        });

        // Redis 직렬화를 위한 컬렉션 초기화
        groupOrders.forEach(this::initializeCollectionsForRedis);

        log.info("단계별 조회 완료: 총 {} 개 GroupOrder 반환", groupOrders.size());
        return groupOrders;
    }

    /**
     * Redis 직렬화를 위한 컬렉션 초기화
     */
    private void initializeCollectionsForRedis(GroupOrder groupOrder) {
        try {
            // 이미지 리스트 초기화
            groupOrder.getImageList().size();

            // 좋아요 리스트 초기화
            groupOrder.getGroupOrderLikeList().size();
            groupOrder.getGroupOrderLikeList().forEach(like -> {
                if (like.getUser() != null) {
                    like.getUser().getId(); // User 정보 초기화
                }
            });

            // 댓글 리스트 초기화
            groupOrder.getGroupOrderCommentList().size();
            groupOrder.getGroupOrderCommentList().forEach(comment -> {
                if (comment.getUser() != null) {
                    comment.getUser().getId(); // User 정보 초기화
                }
                if (comment.getChildGroupOrderComments() != null) {
                    comment.getChildGroupOrderComments().size(); // 중첩 댓글 초기화
                }
            });

            // 유저 정보 초기화
            if (groupOrder.getUser() != null) {
                groupOrder.getUser().getId();
            }

            // 채팅방 정보 초기화
            if (groupOrder.getGroupOrderChatRoom() != null) {
                groupOrder.getGroupOrderChatRoom().getId();
            }

        } catch (Exception e) {
            log.warn("컬렉션 초기화 중 에러 발생: {}", e.getMessage());
        }
    }

    // 이미지와 함께 공동구매 생성
    public void saveGroupOrder(Long userId, RequestGroupOrderDto requestGroupOrderDto, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        if (groupOrderRepository.existsByTitle(requestGroupOrderDto.getTitle())) {
            throw new CustomException(GROUP_ORDER_TITLE_DUPLICATE);
        }

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
    }

    private void saveImages(GroupOrder groupOrder, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/images/group-order/";
            File directory = new File(projectPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            for (MultipartFile file : files) {
                String uuid = UUID.randomUUID().toString();
                String imageFileName = uuid + "_" + file.getOriginalFilename();
                File destinationFile = new File(projectPath + imageFileName);

                try {
                    file.transferTo(destinationFile);

                    Image image = Image.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .isDefault(false)
                            .imageType(ImageType.GROUP_ORDER)
                            .boardId(groupOrder.getId())
                            .build();

                    imageRepository.save(image);
                    groupOrder.getImageList().add(image);

                } catch (IOException e) {
                    throw new RuntimeException("Failed to save image", e);
                }
            }
        }
    }

    public List<GroupOrderImageDto> findGroupOrderImages(Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        List<Image> imageList = groupOrder.getImageList();
        List<GroupOrderImageDto> groupOrderImageDtoList = new ArrayList<>();

        for (Image image : imageList) {
            File file = new File(image.getFilePath());
            if (!file.exists()) {
                throw new RuntimeException("Image file not found at path: " + image.getFilePath());
            }

            String contentType;
            try {
                contentType = Files.probeContentType(file.toPath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            } catch (IOException e) {
                throw new CustomException(IMAGE_NOT_FOUND);
            }

            String filename = file.getName();
            String url = "/api/images/view?filename=" + filename;

            GroupOrderImageDto tipImageDto = GroupOrderImageDto.builder()
                    .filename(filename)
                    .contentType(contentType)
                    .build();

            groupOrderImageDtoList.add(tipImageDto);
        }

        return groupOrderImageDtoList;
    }

    public Resource loadImageAsResource(String filename) {
        String projectPath = System.getProperty("user.dir") + "/src/main/resources/static/images/group-order/";
        File file = new File(projectPath + filename);

        if (!file.exists()) {
            throw new CustomException(IMAGE_NOT_FOUND);
        }

        return new FileSystemResource(file);
    }

    public String getImageContentType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());
            return (contentType != null) ? contentType : "application/octet-stream";
        } catch (IOException e) {
            throw new RuntimeException("Could not determine file type.", e);
        }
    }

    public List<ResponseGroupOrderDto> findGroupOrders(Long userId, GroupOrderSort sort, GroupOrderType type, Optional<String> search) {
        search.filter(s -> !s.isBlank())
                .ifPresent(keyword -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
                    user.addSearchKeyword(keyword);
                });

        String searchKeyword = search.filter(s -> !s.isBlank()).orElse(null);
        String sortParam = sort.name();
        String typeString = String.valueOf(GroupOrderType.from(String.valueOf(type)));
        return groupOrderMapper.findGroupOrders(typeString, searchKeyword, sortParam);
    }

    /**
     * groupOrderId, RequestGroupOrderDto로 GroupOrder 수정
     * 해당 GroupOrder가 redis에 캐싱되어 있으면 redis에서 수정
     * 해당 GroupOrder가 redis에 캐싱되어 있는 것과 상관없이 db에서 수정
     */
    public ResponseGroupOrderDetailDto updateGroupOrder(Long userId, Long groupOrderId, RequestGroupOrderDto requestGroupOrderDto) {
        ResponseGroupOrderDetailDto dto;

        if (deliveryCacheService.existsGroupOrderInCache(groupOrderId)) {
            ResponseGroupOrderDetailDto responseGroupOrderDetailDto = deliveryCacheService.updateCacheDelivery(groupOrderId, requestGroupOrderDto);
            log.info("redis 수정");
            if (responseGroupOrderDetailDto != null) {
                dto = buildHierarchicalComments(responseGroupOrderDetailDto, true);
            }
        }

        log.info("db 수정");
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));

        if (groupOrderRepository.existsByTitle(requestGroupOrderDto.getTitle())) {
            throw new CustomException(GROUP_ORDER_TITLE_DUPLICATE);
        }

        groupOrder.update(requestGroupOrderDto);

        List<ResponseGroupOrderCommentDto> groupOrderCommentDtoList = findGroupOrderComment(groupOrder);

        List<Long> groupOrderLikeUserList = new ArrayList<>();

        List<GroupOrderLike> groupOrderLikeList = groupOrder.getGroupOrderLikeList();
        for (GroupOrderLike groupOrderLike : groupOrderLikeList) {
            Long groupOrderLikeUserId = groupOrderLike.getUser().getId();
            groupOrderLikeUserList.add(groupOrderLikeUserId);
        }

        dto = ResponseGroupOrderDetailDto.detailEntityToDto(groupOrder, groupOrderCommentDtoList, groupOrderLikeUserList);
        return dto;
    }

    /**
     * groupOrderId, GroupOrder 삭제
     * 해당 GroupOrder가 redis에 캐싱되어 있으면 redis에서 삭제
     * 해당 GroupOrder가 redis에 캐싱되어 있는 것과 상관없이 db에서 삭제
     */
    public void deleteGroupOrder(Long userId, Long groupOrderId) {
        if (deliveryCacheService.existsGroupOrderInCache(groupOrderId)) {
            log.info("redis 삭제");
            deliveryCacheService.evictDelivery(groupOrderId);
        }

        log.info("db 삭제");
        GroupOrder groupOrder = groupOrderRepository.findByIdAndUserId(groupOrderId, userId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_OWNED_BY_USER));

        GroupOrderChatRoom groupOrderChatRoom = groupOrder.getGroupOrderChatRoom();
        groupOrderChatRoom.updateGroupOrder(null);
        groupOrderRepository.deleteById(groupOrderId);
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
        return user.getSearchLog();
    }

}

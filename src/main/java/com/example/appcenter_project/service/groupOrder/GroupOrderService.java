package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.groupOrder.GroupOrderSort;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.groupOrder.UserGroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.like.LikeRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderService {

    private final GroupOrderRepository groupOrderRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    private final UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;

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

        // GroupOrder - GroupOrderChatRoom 1대 1 매핑
        groupOrder.updateGroupOrderChatRoom(groupOrderChatRoom);

        groupOrderChatRoomRepository.save(groupOrderChatRoom);
        userGroupOrderChatRoomRepository.save(userGroupOrderChatRoom);
    }

    public ResponseGroupOrderDto findGroupOrderById(Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId).orElseThrow();
        return ResponseGroupOrderDto.entityToDto(groupOrder);
    }

    public List<ResponseGroupOrderDto> findGroupOrders(GroupOrderSort sort, GroupOrderType type, Optional<String> search) {
        Specification<GroupOrder> spec = buildSpecification(type, search);
        Sort sortOption = getSortOption(sort);

        List<GroupOrder> groupOrders = groupOrderRepository.findAll(spec, sortOption);
        return groupOrders.stream()
                .map(ResponseGroupOrderDto::entityToDto)
                .collect(Collectors.toList());
    }

    public ResponseGroupOrderDto updateGroupOrder(Long groupOrderId, RequestGroupOrderDto requestGroupOrderDto) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId).orElseThrow();
        groupOrder.update(requestGroupOrderDto);

        return ResponseGroupOrderDto.entityToDto(groupOrder);
    }

    public void deleteGroupOrder(Long groupOrderId) {
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
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        GroupOrderLike groupOrderLike = GroupOrderLike.builder()
                .user(user)
                .groupOrder(groupOrder)
                .build();

        likeRepository.save(groupOrderLike);

        // user에 좋아요 정보 추가
        user.addLike(groupOrderLike);

        return groupOrder.plusLike();
    }
}
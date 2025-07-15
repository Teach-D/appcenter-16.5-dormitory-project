package com.example.appcenter_project.dto.cache;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOrderCacheDto1 {
    private Long id;
    private String title;
    private String groupOrderType;
    private Integer price;
    private String link;
    private Integer currentPeople;
    private Integer maxPeople;
    private LocalDateTime deadline;
    private Integer groupOrderLike;
    private String description;
    private Long groupOrderChatRoomId;
    private Long userId;
    private List<ImageCacheDto> imageList;
    private List<GroupOrderLikeCacheDto> groupOrderLikeList;
    private List<GroupOrderCommentCacheDto> groupOrderCommentList;

    /*public static GroupOrderCacheDto1 fromEntity(GroupOrder groupOrder) {
        GroupOrderCacheDto1 dto = new GroupOrderCacheDto1();
        dto.setId(groupOrder.getId());
        dto.setTitle(groupOrder.getTitle());
        dto.setGroupOrderType(groupOrder.getGroupOrderType().name());
        dto.setPrice(groupOrder.getPrice());
        dto.setLink(groupOrder.getLink());
        dto.setCurrentPeople(groupOrder.getCurrentPeople());
        dto.setMaxPeople(groupOrder.getMaxPeople());
        dto.setDeadline(groupOrder.getDeadline());
        dto.setGroupOrderLike(groupOrder.getGroupOrderLike());
        dto.setDescription(groupOrder.getDescription());

        // 연관 엔티티 ID 설정
        dto.setGroupOrderChatRoomId(
                groupOrder.getGroupOrderChatRoom() != null ?
                        groupOrder.getGroupOrderChatRoom().getId() : null
        );
        dto.setUserId(
                groupOrder.getUser() != null ?
                        groupOrder.getUser().getId() : null
        );

        // 이미지 리스트 변환
        if (groupOrder.getImageList() != null) {
            dto.setImageList(
                    groupOrder.getImageList().stream()
                            .map(ImageCacheDto::fromEntity)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setImageList(new ArrayList<>());
        }

        // 좋아요 리스트 변환
        if (groupOrder.getGroupOrderLikeList() != null) {
            dto.setGroupOrderLikeList(
                    groupOrder.getGroupOrderLikeList().stream()
                            .map(GroupOrderLikeCacheDto::fromEntity)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setGroupOrderLikeList(new ArrayList<>());
        }

        // 댓글 리스트 변환
        if (groupOrder.getGroupOrderCommentList() != null) {
            dto.setGroupOrderCommentList(
                    groupOrder.getGroupOrderCommentList().stream()
                            .map(GroupOrderCommentCacheDto::fromEntity)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setGroupOrderCommentList(new ArrayList<>());
        }

        return dto;
    }*/
}
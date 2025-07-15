package com.example.appcenter_project.dto;

import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOrderCacheDto {
    private Long id;
    private String title;
    private GroupOrderType groupOrderType;
    private Integer price;
    private String link;
    private int currentPeople;
    private Integer maxPeople;
    private LocalDateTime deadline;
    private int groupOrderLike;
    private String description;
    private Long groupOrderChatRoomId;
    private Long userId;
    private List<ImageListCacheDto> imageList = new ArrayList<>();
    private List<GroupOrderLikeListCacheDto> groupOrderLikeList = new ArrayList<>();
    private List<GroupOrderCommentListCacheDto> groupOrderCommentList = new ArrayList<>();

    // GroupOrder -> DTO 변환 메서드
    public static GroupOrderCacheDto fromEntity(
            GroupOrder groupOrder, List<ImageListCacheDto> imageList, List<GroupOrderLikeListCacheDto> groupOrderLikeList, List<GroupOrderCommentListCacheDto> groupOrderCommentLis
    ) {
        return GroupOrderCacheDto.builder()
                .id(groupOrder.getId())
                .title(groupOrder.getTitle())
                .groupOrderType(groupOrder.getGroupOrderType())
                .price(groupOrder.getPrice())
                .link(groupOrder.getLink())
                .currentPeople(groupOrder.getCurrentPeople())
                .maxPeople(groupOrder.getMaxPeople())
                .deadline(groupOrder.getDeadline())
                .groupOrderLike(groupOrder.getGroupOrderLike())
                .description(groupOrder.getDescription())
                .groupOrderChatRoomId(groupOrder.getGroupOrderChatRoom().getId())
                .userId(groupOrder.getUser().getId())
                .imageList(imageList)
                .groupOrderLikeList(groupOrderLikeList)
                .groupOrderCommentList(groupOrderCommentLis)
                .build();
    }
}
package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderDetailDto {

    private Long groupOrderId;
    private String title;
    private String deadline;
    private String createTime;
    private String category;
    private int price;
    private int currentPeople;
    private int maxPeople;
    private int groupOrderLike;
    private String description;
    private String link;
    private List<Long> groupOrderLikeUserList = new ArrayList<>();

    @Builder.Default
    private List<ResponseGroupOrderCommentDto> groupOrderCommentDtoList = new ArrayList<>();

    public static ResponseGroupOrderDetailDto entityToDto(GroupOrder groupOrder) {
        return ResponseGroupOrderDetailDto.builder()
                .groupOrderId(groupOrder.getId())
                .title(groupOrder.getTitle())
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .price(groupOrder.getPrice())
                .currentPeople(groupOrder.getCurrentPeople())
                .maxPeople(groupOrder.getMaxPeople())
                .groupOrderLike(groupOrder.getGroupOrderLike())
                .description(groupOrder.getDescription())
                .link(groupOrder.getLink())
                .build();
    }

    public static ResponseGroupOrderDetailDto detailEntityToDto(GroupOrder groupOrder, List<ResponseGroupOrderCommentDto> responseGroupOrderCommentDto, List<Long> groupOrderLikeUserList) {
        return ResponseGroupOrderDetailDto.builder()
                .groupOrderId(groupOrder.getId())
                .title(groupOrder.getTitle())
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .createTime(String.valueOf(groupOrder.getCreatedDate()))
                .category(String.valueOf(groupOrder.getGroupOrderType()))
                .price(groupOrder.getPrice())
                .currentPeople(groupOrder.getCurrentPeople())
                .maxPeople(groupOrder.getMaxPeople())
                .groupOrderLike(groupOrder.getGroupOrderLike())
                .description(groupOrder.getDescription())
                .link(groupOrder.getLink())
                .groupOrderCommentDtoList(responseGroupOrderCommentDto)
                .groupOrderLikeUserList(groupOrderLikeUserList)
                .build();
    }
}
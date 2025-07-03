package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderDetailDto {

    private Long id;
    private String title;
    private String deadline;
    private String createDate;
    private GroupOrderType groupOrderType;
    private int price;
    private int currentPeople;
    private int maxPeople;
    private int groupOrderLike;
    private String description;
    private String link;

    @Builder.Default
    private List<Long> groupOrderLikeUserList = new ArrayList<>();

    @Builder.Default
    private List<ResponseGroupOrderCommentDto> groupOrderCommentDtoList = new ArrayList<>();

    public static ResponseGroupOrderDetailDto detailEntityToDto(GroupOrder groupOrder, List<ResponseGroupOrderCommentDto> responseGroupOrderCommentDto, List<Long> groupOrderLikeUserList) {
        return ResponseGroupOrderDetailDto.builder()
                .id(groupOrder.getId())
                .title(groupOrder.getTitle())
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .createDate(String.valueOf(groupOrder.getCreatedDate()))
                .groupOrderType(groupOrder.getGroupOrderType())
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

    public void updateGroupOrderCommentDtoList(List<ResponseGroupOrderCommentDto> groupedList) {
        this.groupOrderCommentDtoList = groupedList;
    }
}
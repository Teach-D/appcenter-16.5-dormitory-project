package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderDto {

    private Long groupOrderId;
    private String title;
    private String deadline;
    private int price;
    private int currentPeople;
    private int maxPeople;
    private int groupOrderLike;
    private String description;
    private String link;

    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder) {
        return ResponseGroupOrderDto.builder()
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
}
package com.example.appcenter_project.dto.request.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RequestGroupOrderDto {

    private String title;
    private String groupOrderType;
    private int price;
    private String link;
    private int maxPeople;
    private LocalDateTime deadline;
    private String description;

    public static GroupOrder dtoToEntity(RequestGroupOrderDto dto) {
        return GroupOrder.builder()
                .title(dto.getTitle())
                .groupOrderType(dto.getGroupOrderType())
                .price(dto.getPrice())
                .link(dto.getLink())
                .currentPeople(0)
                .maxPeople(dto.getMaxPeople())
                .deadline(dto.getDeadline())
                .groupOrderLike(0)
                .description(dto.getDescription())
                .build();
    }
}



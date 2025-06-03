package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import lombok.*;

import java.time.LocalDateTime;

@Getter
public class ResponseGroupOrderDto extends ResponseBoardDto {

    private String deadline;
    private int price;
    private int currentPeople;
    private int maxPeople;

    @Builder
    public ResponseGroupOrderDto(Long boardId, String title, String deadline, int price, int currentPeople, int maxPeople, LocalDateTime createTime, String type) {
        super(boardId, title, type, createTime);
        this.deadline = deadline;
        this.price = price;
        this.currentPeople = currentPeople;
        this.maxPeople = maxPeople;
    }

    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder) {
        return ResponseGroupOrderDto.builder()
                .boardId(groupOrder.getId())
                .title(groupOrder.getTitle())
                .type("GROUP_ORDER")
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .price(groupOrder.getPrice())
                .currentPeople(groupOrder.getCurrentPeople())
                .maxPeople(groupOrder.getMaxPeople())
                .createTime(groupOrder.getCreatedDate())
                .build();

    }
}
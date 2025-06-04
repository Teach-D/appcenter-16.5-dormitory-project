package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import lombok.*;

import java.nio.file.Paths;
import java.time.LocalDateTime;

@Getter
public class ResponseGroupOrderDto extends ResponseBoardDto {

    private String deadline;
    private int price;
    private int currentPeople;
    private int maxPeople;

    @Builder
    public ResponseGroupOrderDto(Long boardId, String title, String deadline, int price, int currentPeople, int maxPeople, LocalDateTime createTime, String type, String fileName) {
        super(boardId, title, type, createTime,fileName);
        this.deadline = deadline;
        this.price = price;
        this.currentPeople = currentPeople;
        this.maxPeople = maxPeople;
    }

    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder) {
        String fullPath = groupOrder.getImageList().get(0).getFilePath();
        String fileName = extractFileName(fullPath);

        return ResponseGroupOrderDto.builder()
                .boardId(groupOrder.getId())
                .title(groupOrder.getTitle())
                .type("GROUP_ORDER")
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .price(groupOrder.getPrice())
                .currentPeople(groupOrder.getCurrentPeople())
                .maxPeople(groupOrder.getMaxPeople())
                .createTime(groupOrder.getCreatedDate())
                .fileName(fileName)
                .build();
    }

    private static String extractFileName(String fullPath) {
        String markerWin = "group-order\\";
        String markerUnix = "group-order/";

        int index = fullPath.lastIndexOf(markerWin);
        if (index != -1) {
            return fullPath.substring(index + markerWin.length());
        }

        index = fullPath.lastIndexOf(markerUnix);
        if (index != -1) {
            return fullPath.substring(index + markerUnix.length());
        }

        return Paths.get(fullPath).getFileName().toString(); // fallback
    }
}
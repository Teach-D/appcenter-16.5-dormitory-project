package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;

import java.nio.file.Paths;
import java.time.LocalDateTime;

@Slf4j
@Getter
public class ResponseGroupOrderDto extends ResponseBoardDto {
    private String deadline;
    private int price;
    private int currentPeople;
    private int maxPeople;
    private GroupOrderType groupOrderType;
    private boolean isRecruitmentComplete;

    public ResponseGroupOrderDto(Long id, String title, String type, int price, String link,
                                 int currentPeople, int maxPeople, String deadline,
                                 int groupOrderLike, String description, LocalDateTime createTime, String fileName, String groupOrderType, boolean isRecruitmentComplete) {
        super(id, title, type, createTime, fileName);
        this.price = price;
        this.currentPeople = currentPeople;
        this.maxPeople = maxPeople;
        this.deadline = deadline;
        this.groupOrderType = GroupOrderType.valueOf(groupOrderType);
        this.isRecruitmentComplete  = isRecruitmentComplete;
    }

    @Builder
    public ResponseGroupOrderDto(Long id, String title, String type, LocalDateTime createTime, String fileName,
                                 String deadline, int price, int currentPeople, int maxPeople, String groupOrderType, boolean isRecruitmentComplete) {
        super(id, title, type, createTime, fileName);
        this.deadline = deadline;
        this.price = price;
        this.currentPeople = currentPeople;
        this.maxPeople = maxPeople;
        this.groupOrderType = GroupOrderType.valueOf(groupOrderType);
        this.isRecruitmentComplete  = isRecruitmentComplete;
    }

    // Keep your existing entityToDto method unchanged
    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder) {
        String fullPath = groupOrder.getImageList().get(0).getFilePath();
        String fileName = extractFileName(fullPath);

        return ResponseGroupOrderDto.builder()
                .id(groupOrder.getId())
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

        return Paths.get(fullPath).getFileName().toString();
    }
}
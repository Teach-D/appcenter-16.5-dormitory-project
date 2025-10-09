package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import com.example.appcenter_project.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.example.appcenter_project.exception.ErrorCode.IMAGE_NOT_FOUND;

@Slf4j
@Getter
public class ResponseGroupOrderDto extends ResponseBoardDto {
    private String deadline;
    private int price;
    private GroupOrderType groupOrderType;
    private boolean isRecruitmentComplete;
    private int viewCount;

    public ResponseGroupOrderDto(Long id, String title, String type, int price, String link,
                                 int currentPeople, int maxPeople, String deadline, int viewCount,
                                 int groupOrderLike, String description, LocalDateTime createTime, String imagePath, String groupOrderType, boolean isRecruitmentComplete) {
        super(id, title, type, createTime, imagePath);
        this.price = price;
        this.deadline = deadline;
        // from() 메서드를 사용하여 안전하게 변환
        this.groupOrderType = GroupOrderType.from(groupOrderType);
        this.viewCount = viewCount;
        this.isRecruitmentComplete = isRecruitmentComplete;
    }

    @Builder
    public ResponseGroupOrderDto(Long id, String title, String type, LocalDateTime createTime, String imagePath, int viewCount,
                                 String deadline, int price, String groupOrderType, boolean isRecruitmentComplete) {
        super(id, title, type, createTime, imagePath);
        this.deadline = deadline;
        this.price = price;
        this.viewCount = viewCount;
        // from() 메서드를 사용하여 안전하게 변환
        this.groupOrderType = GroupOrderType.from(groupOrderType);
        this.isRecruitmentComplete = isRecruitmentComplete;
    }

    public static ResponseGroupOrderDto entityToDto(GroupOrder groupOrder, String imagePath) {

        // groupOrderType이 null인 경우 기본값 설정
        String groupOrderTypeStr = groupOrder.getGroupOrderType() != null
                ? groupOrder.getGroupOrderType().name()
                : GroupOrderType.ETC.name();

        return ResponseGroupOrderDto.builder()
                .id(groupOrder.getId())
                .title(groupOrder.getTitle())
                .type("GROUP_ORDER")
                .deadline(String.valueOf(groupOrder.getDeadline()))
                .price(groupOrder.getPrice())
                .createTime(groupOrder.getCreatedDate())
                .imagePath(imagePath)
                .viewCount(groupOrder.getGroupOrderViewCount())
                .groupOrderType(groupOrderTypeStr)
                .isRecruitmentComplete(groupOrder.isRecruitmentComplete())
                .build();
    }
}
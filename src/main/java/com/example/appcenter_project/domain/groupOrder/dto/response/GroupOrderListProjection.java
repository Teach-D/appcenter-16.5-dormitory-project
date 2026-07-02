package com.example.appcenter_project.domain.groupOrder.dto.response;

import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GroupOrderListProjection {

    private Long id;
    private String title;
    private GroupOrderType groupOrderType;
    private Integer price;
    private LocalDateTime deadline;
    private boolean recruitmentComplete;
    private int viewCount;
    private LocalDateTime createdDate;

    @QueryProjection
    public GroupOrderListProjection(Long id, String title, GroupOrderType groupOrderType,
                                    Integer price, LocalDateTime deadline, boolean recruitmentComplete,
                                    int viewCount, LocalDateTime createdDate) {
        this.id = id;
        this.title = title;
        this.groupOrderType = groupOrderType;
        this.price = price;
        this.deadline = deadline;
        this.recruitmentComplete = recruitmentComplete;
        this.viewCount = viewCount;
        this.createdDate = createdDate;
    }
}

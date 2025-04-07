package com.example.appcenter_project.dto.response.like;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseLikeDto {

    private String title;
    private Integer price;
    private Integer currentPeople;
    private Integer maxPeople;
    private Long boardId;
    private LocalDateTime deadline;
}

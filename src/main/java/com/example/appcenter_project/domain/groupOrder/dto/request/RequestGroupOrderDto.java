package com.example.appcenter_project.domain.groupOrder.dto.request;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.groupOrder.enums.GroupOrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "공동구매 정보 입력")
@Getter
public class RequestGroupOrderDto {

    private String title;

    @Schema(description = "공동구매 유형",
            allowableValues = {"배달", "식자재", "생활용품",
                    "기타"})
    private GroupOrderType groupOrderType;

    private Integer price;

    private String link;

    private String openChatLink;

    @Future(message = "마감일은 미래의 날짜여야 합니다.")
    private LocalDateTime deadline;

    private String description;

    public static GroupOrder dtoToEntity(RequestGroupOrderDto dto, User user) {
        return GroupOrder.builder()
                .title(dto.getTitle())
                .groupOrderType(dto.getGroupOrderType())
                .price(dto.getPrice())
                .link(dto.getLink())
                .openChatLink(dto.getOpenChatLink())
                .currentPeople(0)
                .deadline(dto.getDeadline())
                .groupOrderLike(0)
                .description(dto.getDescription())
                .user(user)
                .build();
    }
}




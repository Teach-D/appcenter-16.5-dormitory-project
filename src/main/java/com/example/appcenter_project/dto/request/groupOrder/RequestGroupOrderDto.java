package com.example.appcenter_project.dto.request.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.groupOrder.GroupOrderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "공동구매 정보 입력")
@Getter
public class RequestGroupOrderDto {

    @NotBlank(message = "제목은 필수 입력 값입니다.")
    private String title;

    @Schema(description = "공동구매 유형",
            allowableValues = {"전체", "배달", "식료품", "생활용품",
                    "기타"})
    @NotNull(message = "공동구매 유형은 필수 입력 값입니다.")
    private GroupOrderType groupOrderType;

    @NotNull(message = "가격은 필수 입력 값입니다.")
    @Min(value = 1, message = "가격은 1원 이상이어야 합니다.")
    private Integer price;

    @NotBlank(message = "공동구매 링크는 필수 입력 값입니다.")
    private String link;

    @NotNull(message = "최대 인원 수는 필수 입력 값입니다.")
    @Min(value = 1, message = "최대 인원 수는 1명 이상이어야 합니다.")
    private Integer maxPeople;

    @NotNull(message = "마감일은 필수 입력 값입니다.")
    @Future(message = "마감일은 미래의 날짜여야 합니다.")
    private LocalDateTime deadline;

    @NotBlank(message = "공동구매 설명은 필수 입력 값입니다.")
    private String description;

    public static GroupOrder dtoToEntity(RequestGroupOrderDto dto, User user) {
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
                .user(user)
                .build();
    }
}




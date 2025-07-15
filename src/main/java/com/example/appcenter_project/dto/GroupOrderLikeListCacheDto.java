package com.example.appcenter_project.dto;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.like.GroupOrderLike;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupOrderLikeListCacheDto {

    private Long id;
    private Long userId;

    public static GroupOrderLikeListCacheDto fromEntity(GroupOrderLike groupOrderLike) {
        return GroupOrderLikeListCacheDto.builder()
                .id(groupOrderLike.getId())
                .userId(groupOrderLike.getUser().getId())
                .build();
    }
}

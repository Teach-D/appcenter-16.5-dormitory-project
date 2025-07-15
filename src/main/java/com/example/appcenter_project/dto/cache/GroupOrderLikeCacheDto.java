package com.example.appcenter_project.dto.cache;

import com.example.appcenter_project.entity.like.GroupOrderLike;
import lombok.*;

// 2. GroupOrderLikeCacheDto
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupOrderLikeCacheDto {
    private Long id;
    private Long userId;

    public static GroupOrderLikeCacheDto fromEntity(GroupOrderLike groupOrderLike) {
        GroupOrderLikeCacheDto dto = new GroupOrderLikeCacheDto();
        dto.setId(groupOrderLike.getId());
        dto.setUserId(groupOrderLike.getUser() != null ? groupOrderLike.getUser().getId() : null);
        return dto;
    }
}
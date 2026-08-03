package com.example.appcenter_project.domain.block.dto.response;

import com.example.appcenter_project.domain.block.entity.UserBlock;
import com.example.appcenter_project.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseBlockedUserDto {

    private Long blockedUserId;
    private String blockedUserName;
    private LocalDateTime blockedAt;

    public static ResponseBlockedUserDto of(UserBlock block, User user) {
        return ResponseBlockedUserDto.builder()
                .blockedUserId(user.getId())
                .blockedUserName(user.getName())
                .blockedAt(block.getCreatedDate())
                .build();
    }
}

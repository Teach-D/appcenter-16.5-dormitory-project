package com.example.appcenter_project.dto;


import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.user.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class GroupOrderCommentListCacheDto {

    private Long id;
    private String reply; // 댓글 내용
    private boolean isDeleted;
    private Long groupOrderId;
    private Long userId;
    private GroupOrderComment parentGroupOrderComment;
    private List<GroupOrderComment> childGroupOrderComments;

    public static GroupOrderCommentListCacheDto fromEntity(GroupOrderComment groupOrderComment) {
        return GroupOrderCommentListCacheDto.builder()
                .id(groupOrderComment.getId())
                .reply(groupOrderComment.getReply())
                .isDeleted(groupOrderComment.isDeleted())
                .userId(groupOrderComment.getUser().getId())
                .parentGroupOrderComment(groupOrderComment.getParentGroupOrderComment())
                .childGroupOrderComments(groupOrderComment.getChildGroupOrderComments())
                .build();
    }

}

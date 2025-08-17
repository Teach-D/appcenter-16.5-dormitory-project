package com.example.appcenter_project.dto.response.groupOrder;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import com.example.appcenter_project.entity.user.User;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseGroupOrderCommentDto {

    private Long groupOrderCommentId;
    private Long userId;
    private String reply;
    private Long parentId;
    private Boolean isDeleted;
    private String commentAuthorImagePath;

    @Builder.Default
    private List<ResponseGroupOrderCommentDto> childGroupOrderCommentList = new ArrayList<>();

    public static ResponseGroupOrderCommentDto entityToDto(GroupOrderComment groupOrderComment, User user) {
        return ResponseGroupOrderCommentDto.builder()
                .reply(groupOrderComment.getReply())
                .groupOrderCommentId(groupOrderComment.getId())
                .userId(user.getId())
                .build();
    }

    public void updateReply(String reply) {
        this.reply = reply;
    }

    public void updateChildGroupOrderCommentList(List childGroupOrderCommentList) {
        this.childGroupOrderCommentList = childGroupOrderCommentList;
    }

    public void updateCommentAuthorImagePath(String commentAuthorImagePath) {
        this.commentAuthorImagePath = commentAuthorImagePath;
    }
}

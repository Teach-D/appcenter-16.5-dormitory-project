package com.example.appcenter_project.domain.groupOrder.dto.response;

import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderComment;
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
    private String createDate;
    private String username;

    @Builder.Default
    private List<ResponseGroupOrderCommentDto> childGroupOrderCommentList = new ArrayList<>();

    public static ResponseGroupOrderCommentDto from(GroupOrderComment groupOrderComment) {
        boolean isDeleted = groupOrderComment.isDeleted();
        return ResponseGroupOrderCommentDto.builder()
                .parentId(groupOrderComment.getParentGroupOrderComment() == null ? null : groupOrderComment.getParentGroupOrderComment().getId())
                .reply(isDeleted ? "삭제된 댓글입니다" : groupOrderComment.getReply())
                .groupOrderCommentId(groupOrderComment.getId())
                .userId(groupOrderComment.getUser().getId())
                .createDate(String.valueOf(groupOrderComment.getCreatedDate()))
                .username(isDeleted ? "알 수 없는 사용자" : groupOrderComment.getUser().getName())
                .isDeleted(isDeleted)
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

    public void updateUsername(String username) {
        this.username = username;
    }
}

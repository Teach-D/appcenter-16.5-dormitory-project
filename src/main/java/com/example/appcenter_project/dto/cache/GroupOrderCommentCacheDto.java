package com.example.appcenter_project.dto.cache;

import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupOrderCommentCacheDto {
    private Long id;
    private String reply;
    private Boolean deleted;
    private Long userId;
    private Long parentCommentId; // 부모 댓글 ID만 저장
    private ParentCommentCacheDto parentGroupOrderComment;
    private List<GroupOrderCommentCacheDto> childComments;

    /*public static GroupOrderCommentCacheDto fromEntity(GroupOrderComment comment) {
        GroupOrderCommentCacheDto dto = new GroupOrderCommentCacheDto();
        dto.setId(comment.getId());
        dto.setReply(comment.getReply());
        dto.setDeleted(comment.isDeleted());
        dto.setUserId(comment.getUser() != null ? comment.getUser().getId() : null);
        dto.setParentCommentId(
                comment.getParentGroupOrderComment() != null ?
                        comment.getParentGroupOrderComment().getId() : null
        );

        // 자식 댓글들 변환
        if (comment.getChildGroupOrderComments() != null) {
            dto.setChildComments(
                    comment.getChildGroupOrderComments().stream()
                            .map(GroupOrderCommentCacheDto::fromEntity)
                            .collect(Collectors.toList())
            );
        } else {
            dto.setChildComments(new ArrayList<>());
        }

*//*        if (comment.getParentGroupOrderComment() != null) {
            GroupOrderComment parentGroupOrderComment1 = comment.getParentGroupOrderComment();
            dto.setParentGroupOrderComment(GroupOrderCommentCacheDto.fromEntity(parentGroupOrderComment1));
        }*//*

        return dto;
    }*/
}
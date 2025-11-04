package com.example.appcenter_project.domain.tip.dto.response;

import com.example.appcenter_project.domain.tip.entity.Tip;
import com.example.appcenter_project.domain.tip.entity.TipComment;
import com.example.appcenter_project.domain.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseTipCommentDto {

    private Long tipCommentId;
    private Long userId;
    private String reply;
    private Long parentId;
    private Boolean isDeleted;
    private LocalDateTime createDate;
    private String name;
    private String writerImageFile;

    @Builder.Default
    private List<ResponseTipCommentDto> childTipCommentList = new ArrayList<>();

    public static ResponseTipCommentDto entityToDto(TipComment tipComment, User user) {
        return ResponseTipCommentDto.builder()
                .tipCommentId(tipComment.getId())
                .userId(user.getId())
                .reply(tipComment.getReply())
                .parentId(tipComment.getParentTipComment() != null ? tipComment.getParentTipComment().getId() : null)
                .isDeleted(tipComment.isDeleted())
                .createDate(tipComment.getCreatedDate())
                .name(user.getName())
                .build();
    }

    public static ResponseTipCommentDto from(TipComment comment) {
        return ResponseTipCommentDto.builder()
                .tipCommentId(comment.getId())
                .userId(comment.getUser().getId())
                .parentId(extractParentCommentId(comment))
                .isDeleted(comment.isDeleted())
                .createDate(comment.getCreatedDate())
                .build();
    }

    private static Long extractParentCommentId(TipComment comment) {
        return comment.getParentTipComment() != null ? comment.getParentTipComment().getId() : null;
    }

    public void setDeletedCommentInfo() {
        updateReply("삭제된 메시지입니다.");
        updateName("알 수 없는 사용자");
        updateWriterImageFile(null);
    }

    public void updateReply(String reply) {
        this.reply = reply;
    }

    public void updateChildTipCommentList(List childTipCommentList) {
        this.childTipCommentList = childTipCommentList;
    }

    public void updateWriterImageFile(String  writerImageFile) {
        this.writerImageFile = writerImageFile;
    }

    public void updateName(String name) {
        this.name = name;
    }
}
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

    public static ResponseTipCommentDto detailEntityToDto(Tip tip, List<ResponseTipCommentDto> responseTipCommentDtoList) {
        return ResponseTipCommentDto.builder()
                .tipCommentId(tip.getId())
                .userId(tip.getUser().getId())
                .reply(tip.getContent())
                .childTipCommentList(responseTipCommentDtoList)
                .build();
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

package com.example.appcenter_project.dto.response.tip;

import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.tip.Tip;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ResponseTipDto extends ResponseBoardDto {

    private String content;
    private Integer tipLikeCount;
    private Integer tipCommentCount;

    @Builder
    public ResponseTipDto(Long boardId, String title, String content, Integer tipLikeCount, Integer tipCommentCount, LocalDateTime createTime, String type) {
        super(boardId, title, type, createTime);
        this.content = content;
        this.tipLikeCount = tipLikeCount;
        this.tipCommentCount = tipCommentCount;
    }

    public static ResponseTipDto entityToDto(Tip tip) {
        return ResponseTipDto.builder()
                .boardId(tip.getId())
                .title(tip.getTitle())
                .type("TIP")
                .content(tip.getContent())
                .tipLikeCount(tip.getTipLike())
                .tipCommentCount(
                        tip.getTipCommentList() != null ? tip.getTipCommentList().size() : 0
                )
                .createTime(tip.getCreatedDate())
                .build();
    }
}

package com.example.appcenter_project.dto.response.tip;

import com.example.appcenter_project.entity.tip.Tip;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseTipDto {

    private String title;
    private String content;
    private Integer tipLikeCount;
    private Integer tipCommentCount;
    private String createTime;

    public static ResponseTipDto entityToDto(Tip tip) {
        return ResponseTipDto.builder()
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipLikeCount(tip.getTipLike())
                .tipCommentCount(
                        tip.getTipCommentList() != null ? tip.getTipCommentList().size() : 0
                )
                .createTime(String.valueOf(tip.getCreatedDate()))
                .build();
    }
}

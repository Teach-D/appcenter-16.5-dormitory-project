package com.example.appcenter_project.dto.response.tip;

import com.example.appcenter_project.entity.tip.Tip;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ResponseTipDetailDto {

    private Long tipId;
    private String title;
    private String content;
    private Integer tipLikeCount;

    @Builder.Default
    private List<Long> tipLikeUserList = new ArrayList<>();

    private String createTime;

    @Builder.Default
    private List<ResponseTipCommentDto> tipCommentDtoList = new ArrayList<>();

    public static ResponseTipDetailDto entityToDto(Tip tip, List<ResponseTipCommentDto> responseTipCommentDtoList, List<Long> tipLikeUserList) {
        return ResponseTipDetailDto.builder()
                .tipId(tip.getId())
                .createTime(String.valueOf(tip.getCreatedDate()))
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipLikeCount(tip.getTipLike())
                .tipLikeUserList(tipLikeUserList)
                .tipCommentDtoList(responseTipCommentDtoList)
                .build();
    }


}

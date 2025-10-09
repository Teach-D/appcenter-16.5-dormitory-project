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

    private Long id;
    private String title;
    private String content;
    private Integer tipLikeCount;
    private String name;
    private boolean isCheckLikeCurrentUser = false;
    private String writerImageFile;

    @Builder.Default
    private List<Long> tipLikeUserList = new ArrayList<>();

    private String createDate;

    @Builder.Default
    private List<ResponseTipCommentDto> tipCommentDtoList = new ArrayList<>();

    public static ResponseTipDetailDto entityToDto(Tip tip, List<ResponseTipCommentDto> responseTipCommentDtoList, List<Long> tipLikeUserList) {
        return ResponseTipDetailDto.builder()
                .id(tip.getId())
                .createDate(String.valueOf(tip.getCreatedDate()))
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipLikeCount(tip.getTipLike())
                .name(tip.getUser().getName())
                .tipLikeUserList(tipLikeUserList)
                .tipCommentDtoList(responseTipCommentDtoList)
                .build();
    }


    public void updateTipCommentDtoList(List<ResponseTipCommentDto> topLevelComments) {
        this.tipCommentDtoList = topLevelComments;
    }

    public void updateIsCheckLikeCurrentUser(boolean check) {
        this.isCheckLikeCurrentUser = check;
    }

    public void updateWriterImageUrl(String fileName) {
        this.writerImageFile = fileName;
    }

    public void updateWriterName(String name) {
        this.name = name;
    }
}

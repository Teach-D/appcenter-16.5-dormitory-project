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
    private Integer tipLike;

    @Builder.Default
    private List<ResponseTipCommentDto> tipCommentDtoList = new ArrayList<>();

    public static ResponseTipDto entityToDto(Tip tip) {
        return ResponseTipDto.builder()
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipLike(tip.getTipLike())
                .build();
    }

    public static ResponseTipDto entityToDtoList(Tip tip) {
        return ResponseTipDto.builder()
                .title(tip.getTitle())
                .content(tip.getContent())
                .tipLike(tip.getTipLike())
                .build();
    }

}

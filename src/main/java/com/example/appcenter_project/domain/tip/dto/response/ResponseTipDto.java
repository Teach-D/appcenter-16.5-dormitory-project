package com.example.appcenter_project.domain.tip.dto.response;

import com.example.appcenter_project.domain.user.dto.response.ResponseBoardDto;
import com.example.appcenter_project.domain.tip.entity.Tip;
import lombok.*;

import java.nio.file.Paths;
import java.time.LocalDateTime;

@Getter
public class  ResponseTipDto extends ResponseBoardDto {

    private String content;
    private Integer tipLikeCount;
    private Integer tipCommentCount;

    public ResponseTipDto(Long id, String title, String type, LocalDateTime createDate, String filePath,
                          String content, Integer tipLikeCount, Integer tipCommentCount) {
        super(id, title, type, createDate, filePath);
        this.content = content;
        this.tipLikeCount = tipLikeCount;
        this.tipCommentCount = tipCommentCount;
    }

    @Builder
    public ResponseTipDto(Long boardId, String title, String content, Integer tipLikeCount, Integer tipCommentCount,
                          LocalDateTime createTime, String type, String fileName) {
        super(boardId, title, type, createTime, fileName);
        this.content = content;
        this.tipLikeCount = tipLikeCount;
        this.tipCommentCount = tipCommentCount;
    }

    public static ResponseTipDto from(Tip tip) {
        return ResponseTipDto.builder()
                .boardId(tip.getId())
                .title(tip.getTitle())
                .type("TIP")
                .content(tip.getContent())
                .tipLikeCount(tip.getTipLike())
                .tipCommentCount(tip.getTipCommentCount())
                .createTime(tip.getCreatedDate())
                .build();
    }

    private static String extractFileName(String fullPath) {
        String markerWin = "tip\\";
        String markerUnix = "tip/";

        int index = fullPath.lastIndexOf(markerWin);
        if (index != -1) {
            return fullPath.substring(index + markerWin.length());
        }

        index = fullPath.lastIndexOf(markerUnix);
        if (index != -1) {
            return fullPath.substring(index + markerUnix.length());
        }

        return Paths.get(fullPath).getFileName().toString(); // fallback
    }

    public static ResponseTipDto from(Tip tip, String fileName) {
        return ResponseTipDto.builder()
                .boardId(tip.getId())
                .title(tip.getTitle())
                .type("TIP")
                .content(tip.getContent())
                .tipLikeCount(tip.getTipLike())
                .tipCommentCount(tip.getTipCommentCount())
                .createTime(tip.getCreatedDate())
                .fileName(fileName)
                .build();
    }
}
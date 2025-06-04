package com.example.appcenter_project.dto.response.tip;

import com.example.appcenter_project.dto.response.user.ResponseBoardDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.tip.Tip;
import lombok.*;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ResponseTipDto extends ResponseBoardDto {

    private String content;
    private Integer tipLikeCount;
    private Integer tipCommentCount;

    @Builder
    public ResponseTipDto(Long boardId, String title, String content, Integer tipLikeCount, Integer tipCommentCount,
                          LocalDateTime createTime, String type, String fileName) {
        super(boardId, title, type, createTime, fileName);
        this.content = content;
        this.tipLikeCount = tipLikeCount;
        this.tipCommentCount = tipCommentCount;
    }

    public static ResponseTipDto entityToDto(Tip tip) {
        Image image = tip.getImageList().get(0);
        String fullPath = image.getFilePath();
        String fileName = extractFileName(fullPath);

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
                .fileName(fileName)
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
}

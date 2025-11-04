package com.example.appcenter_project.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public abstract class ResponseBoardDto {

    private Long boardId;
    private String title;
    private String type;
    private LocalDateTime createDate;
    private String filePath;
}
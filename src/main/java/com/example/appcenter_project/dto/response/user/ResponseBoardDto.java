package com.example.appcenter_project.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public abstract class ResponseBoardDto {

    private Long boardId;
    private String title;
    private String type;
    private LocalDateTime createTime;
    private String fileName;
}
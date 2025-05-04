package com.example.appcenter_project.dto.request.tip;

import lombok.Getter;

@Getter
public class RequestTipCommentDto {

    private Long parentCommentId;
    private Long tipId;
    private String reply;
}

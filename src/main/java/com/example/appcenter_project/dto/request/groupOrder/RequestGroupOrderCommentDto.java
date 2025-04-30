package com.example.appcenter_project.dto.request.groupOrder;

import lombok.Getter;

@Getter
public class RequestGroupOrderCommentDto {

    private Long parentCommentId;
    private Long groupOrderId;
    private String reply;
}

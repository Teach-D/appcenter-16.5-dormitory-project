package com.example.appcenter_project.dto.response.complaint;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseComplaintReplyDto {
    private String replyTitle;
    private String replyContent;
    private String responderName;
    private String attachmentUrl;
    private String createdDate;
}

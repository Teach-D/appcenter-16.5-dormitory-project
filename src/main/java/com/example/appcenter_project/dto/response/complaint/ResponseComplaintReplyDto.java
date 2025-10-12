package com.example.appcenter_project.dto.response.complaint;

import com.example.appcenter_project.dto.AttachedFileDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseComplaintReplyDto {
    private String replyTitle;
    private String replyContent;
    private String responderName;
    private List<AttachedFileDto> attachmentUrl;
    private String createdDate;
}

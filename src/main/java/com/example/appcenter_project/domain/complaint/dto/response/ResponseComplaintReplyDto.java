package com.example.appcenter_project.domain.complaint.dto.response;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseComplaintReplyDto {
    private String replyTitle;
    private String replyContent;
    private String responderName;
    private List<ImageLinkDto> attachmentUrl;
    private String createdDate;
}

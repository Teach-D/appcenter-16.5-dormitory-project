package com.example.appcenter_project.domain.complaint.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestComplaintReplyDto {
    private String replyTitle;     // 답변 제목
    private String replyContent;   // 답변 내용
    private String responderName;  // 담당자 이름
}

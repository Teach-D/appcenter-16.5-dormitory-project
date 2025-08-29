package com.example.appcenter_project.dto.request.complaint;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestComplaintSearchDto {
    private String keyword;     // 제목/내용 검색
    private String type;        // 이슈(ComplaintType)
    private String status;      // 처리상황(ComplaintStatus)
    private String dormType;    // 기숙사
    private String responder;   // 담당자 이름
    private String caseNumber;  // 사생번호
}

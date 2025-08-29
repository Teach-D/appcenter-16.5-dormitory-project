package com.example.appcenter_project.dto.response.complaint;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseComplaintDto {

    private Long id;            // 민원 ID
    private String title;       // 제목
    private String content;     // 내용
    private String type;        // 유형 (기물, 시설, ...)
    private String dormType;    // 기숙사 (1기숙사, 2기숙사, 3기숙사)
    private String caseNumber;  // 사생번호
    private String contact;     // 연락처
    private String status;      // 상태 (대기중, 처리중 등)
    private String createdDate; // 등록일시 (문자열 변환)
}

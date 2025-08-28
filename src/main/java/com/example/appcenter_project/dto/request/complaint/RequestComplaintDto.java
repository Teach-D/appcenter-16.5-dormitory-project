package com.example.appcenter_project.dto.request.complaint;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestComplaintDto {

    private String type;        // 유형 (기물, 시설, 청소/위생, 소음, 기타)
    private String dormType;    // 기숙사 (1기숙사, 2기숙사, 3기숙사)
    private String caseNumber;  // 사생번호
    private String contact;     // 연락처
    private String title;       // 민원 제목
    private String content;     // 민원 내용
}

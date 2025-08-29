package com.example.appcenter_project.dto.response.complaint;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseComplaintListDto {
    private String date;     // 08.27 형식
    private String type;     // 기물 / 시설 / 유형1 / 유형2
    private String title;    // 제목
    private String status;   // 대기중 / 담당자 배정 / 처리중 / 처리완료
}

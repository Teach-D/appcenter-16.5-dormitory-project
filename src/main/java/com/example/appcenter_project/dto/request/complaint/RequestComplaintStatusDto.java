package com.example.appcenter_project.dto.request.complaint;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestComplaintStatusDto {
    private String status; // 변경할 상태 (대기중, 담당자 배정, 처리중, 처리완료)
}

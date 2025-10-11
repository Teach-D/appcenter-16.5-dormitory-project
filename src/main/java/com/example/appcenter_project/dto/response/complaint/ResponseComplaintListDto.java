package com.example.appcenter_project.dto.response.complaint;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ResponseComplaintListDto {

    private Long id;
    private String date;     // 08.27 형식
    private String type;     // 기물 / 시설 / 유형1 / 유형2
    private String title;    // 제목
    private String status;   // 대기중 / 담당자 배정 / 처리중 / 처리완료
    private String officer; // 담당자 이름
    private String building;
    private String floor;
    private String roomNumber;
    private String bedNumber;
    private String dormType; // 기숙사
    private LocalDate createDate;
}

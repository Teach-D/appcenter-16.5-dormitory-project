package com.example.appcenter_project.dto.response.complaint;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResponseComplaintDetailDto {
    private Long id;
    private String title;
    private String content;
    private String type;        // 기물 등
    private String dormType;    // 1기숙사 등
    private String status;      // 대기중 등
    private String building;
    private String floor;
    private String roomNumber;
    private String bedNumber;
    private String specificLocation; // 구체적 장소
    private String incidentDate; // 사건 발생 날짜
    private String incidentTime; // 사건 발생 시간
    private String createdDate; // ISO 또는 포맷 문자열
    private String officer;
    private ResponseComplaintReplyDto reply; // null 가능
    private List<String> images;
    private String studentNumber;
}

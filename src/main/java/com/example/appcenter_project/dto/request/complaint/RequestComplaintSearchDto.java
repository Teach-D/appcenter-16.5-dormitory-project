package com.example.appcenter_project.dto.request.complaint;

import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestComplaintSearchDto {
    private DormType dormType;
    private String officer;
    private ComplaintStatus status;
    private String keyword;
    private String building;
    private String floor;
    private String bedNumber;
    private String roomNumber;

    @Schema(description = "민원 유형 (NOISE: 소음, SMOKING: 흡연, DRINKING: 음주, ROOMMATE_CHANGE: 호실변경신청, POINT_INQUIRY: 벌점 및 상점 문의, HALLWAY_OBSTRUCTION: 복도물건 적치 신고)")
    private ComplaintType type;}

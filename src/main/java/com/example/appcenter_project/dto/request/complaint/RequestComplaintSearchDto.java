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

    @Schema(description = "민원 유형 (NOISE: 소음, SMOKING: 흡연, DRINKING: 음주, ROOMMATE_CHANGE: 호실 변경, COMMON_SPACE: 공용공간)")
    private ComplaintType type;}

package com.example.appcenter_project.dto.request.complaint;

import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestComplaintSearchDto {
    private DormType dormType;
    private String officer;
    private String caseNumber;
    private ComplaintStatus status;
    private String keyword;
    private ComplaintType type;
}

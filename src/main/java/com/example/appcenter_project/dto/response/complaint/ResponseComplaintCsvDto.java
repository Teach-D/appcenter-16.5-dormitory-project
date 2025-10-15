package com.example.appcenter_project.dto.response.complaint;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseComplaintCsvDto {
    private Long id;
    private String date;
    private String type;
    private String title;
    private String content;
    private String status;
    private String officer;
    private String dormType;
    private String building;
    private String floor;
    private String roomNumber;
    private String bedNumber;
    private String userName;
    private String userEmail;
    private String userPhone;
    private String replyTitle;
    private String replyContent;
    private String responderName;
    private String replyDate;
    private boolean isPrivacyAgreed;
}


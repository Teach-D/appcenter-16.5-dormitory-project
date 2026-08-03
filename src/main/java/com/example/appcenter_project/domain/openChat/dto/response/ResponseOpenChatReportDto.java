package com.example.appcenter_project.domain.openChat.dto.response;

import com.example.appcenter_project.domain.openChat.entity.OpenChatMessageReport;
import com.example.appcenter_project.domain.openChat.enums.ReportReason;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ResponseOpenChatReportDto {

    private Long reportId;
    private ReportStatus status;
    private ReportReason reason;
    private Long roomId;
    private Long messageId;
    private String reportedContent;
    private Long reporterId;
    private String reporterStudentNumber;
    private String reporterName;
    private Long targetUserId;
    private String targetStudentNumber;
    private String targetName;
    private LocalDateTime createdDate;

    public static ResponseOpenChatReportDto from(OpenChatMessageReport report,
                                                 String reporterName, String targetName) {
        return ResponseOpenChatReportDto.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .reason(report.getReason())
                .roomId(report.getRoomId())
                .messageId(report.getMessageId())
                .reportedContent(report.getReportedContent())
                .reporterId(report.getReporterId())
                .reporterStudentNumber(report.getReporterStudentNumber())
                .reporterName(reporterName)
                .targetUserId(report.getTargetUserId())
                .targetStudentNumber(report.getTargetStudentNumber())
                .targetName(targetName)
                .createdDate(report.getCreatedDate())
                .build();
    }
}
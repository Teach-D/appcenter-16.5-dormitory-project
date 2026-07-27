package com.example.appcenter_project.domain.openChat.entity;

import com.example.appcenter_project.common.BaseTimeEntity;
import com.example.appcenter_project.domain.openChat.enums.ReportReason;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "open_chat_message_report", indexes = {
        @Index(name = "idx_ocmr_status", columnList = "status"),
        @Index(name = "idx_ocmr_target_student_number", columnList = "targetStudentNumber")
})
public class OpenChatMessageReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private Long roomId;

    @Column(nullable = false)
    private Long reporterId;

    @Column(nullable = false)
    private String reporterStudentNumber;

    @Column(nullable = false)
    private Long targetUserId;

    @Column(nullable = false)
    private String targetStudentNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reportedContent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    public static OpenChatMessageReport create(Long messageId, Long roomId,
                                               Long reporterId, String reporterStudentNumber,
                                               Long targetUserId, String targetStudentNumber,
                                               ReportReason reason, String reportedContent) {
        OpenChatMessageReport report = new OpenChatMessageReport();
        report.messageId = messageId;
        report.roomId = roomId;
        report.reporterId = reporterId;
        report.reporterStudentNumber = reporterStudentNumber;
        report.targetUserId = targetUserId;
        report.targetStudentNumber = targetStudentNumber;
        report.reason = reason;
        report.reportedContent = reportedContent;
        report.status = ReportStatus.PENDING;
        return report;
    }

    public boolean isPending() {
        return this.status == ReportStatus.PENDING;
    }

    public void approve() {
        this.status = ReportStatus.APPROVED;
    }

    public void cancel() {
        this.status = ReportStatus.CANCELLED;
    }
}
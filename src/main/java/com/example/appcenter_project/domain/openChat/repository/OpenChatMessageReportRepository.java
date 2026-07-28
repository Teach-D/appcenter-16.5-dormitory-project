package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatMessageReport;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpenChatMessageReportRepository extends JpaRepository<OpenChatMessageReport, Long> {

    Page<OpenChatMessageReport> findByStatus(ReportStatus status, Pageable pageable);

    long countByTargetStudentNumberAndStatus(String targetStudentNumber, ReportStatus status);
    long countByTargetStudentNumber(String targetStudentNumber);
}
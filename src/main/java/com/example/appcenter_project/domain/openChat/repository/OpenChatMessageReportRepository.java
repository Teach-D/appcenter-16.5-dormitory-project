package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.dto.TargetReportCount;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessageReport;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OpenChatMessageReportRepository extends JpaRepository<OpenChatMessageReport, Long> {

    Page<OpenChatMessageReport> findByStatus(ReportStatus status, Pageable pageable);

    long countByTargetStudentNumberAndStatus(String targetStudentNumber, ReportStatus status);
    long countByTargetStudentNumber(String targetStudentNumber);

    @Query("select new com.example.appcenter_project.domain.openChat.dto.TargetReportCount(" +
            "r.targetStudentNumber, count(r)) " +
            "from OpenChatMessageReport r where r.status = :status group by r.targetStudentNumber")
    List<TargetReportCount> findApprovedCountsGroupByTargetStudentNumber(@Param("status") ReportStatus status);
}
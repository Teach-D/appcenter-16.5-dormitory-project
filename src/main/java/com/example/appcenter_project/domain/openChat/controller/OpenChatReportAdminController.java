package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatReportDto;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import com.example.appcenter_project.domain.openChat.service.OpenChatMessageReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/open-chat-reports")
public class OpenChatReportAdminController implements OpenChatReportAdminApiSpecification {

    private final OpenChatMessageReportService reportService;

    @GetMapping
    public ResponseEntity<Page<ResponseOpenChatReportDto>> findReports(
            @RequestParam(defaultValue = "PENDING") ReportStatus status,
            @PageableDefault(size = 20, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reportService.findReports(status, pageable));
    }

    @PatchMapping("/{reportId}/approve")
    public ResponseEntity<Void> approveReport(@PathVariable Long reportId) {
        reportService.approveReport(reportId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{reportId}/cancel")
    public ResponseEntity<Void> cancelReport(@PathVariable Long reportId) {
        reportService.cancelReport(reportId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countReports(
            @RequestParam String studentNumber,
            @RequestParam(defaultValue = "APPROVED") ReportStatus status) {
        return ResponseEntity.ok(reportService.countReports(studentNumber, status));
    }
}
package com.example.appcenter_project.domain.report.controller;

import com.example.appcenter_project.domain.report.dto.request.RequestReportDto;
import com.example.appcenter_project.domain.report.dto.response.ResponseReportDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController implements ReportApiSpecification {

    private final ReportService reportService;

    @GetMapping
    public List<ResponseReportDto> getReports() {
        return reportService.getAllReports();
    }

    @GetMapping("/{reportId}")
    public ResponseReportDto getReport(@PathVariable Long reportId) {
        return reportService.getReport(reportId);
    }

    @PostMapping
    public void createReport(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestReportDto requestReportDto) {
        reportService.saveReport(requestReportDto, user.getId());
    }

    @DeleteMapping("/{reportId}")
    public void delete(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
    }
}

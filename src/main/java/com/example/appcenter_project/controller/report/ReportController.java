package com.example.appcenter_project.controller.report;

import com.example.appcenter_project.dto.request.report.RequestReportDto;
import com.example.appcenter_project.dto.response.report.ResponseReportDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.report.ReportService;
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
    public void createReport(@Valid @RequestBody RequestReportDto requestReportDto, @AuthenticationPrincipal CustomUserDetails user) {
        reportService.saveReport(requestReportDto, user.getId());
    }

    @DeleteMapping("/{reportId}")
    public void delete(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
    }
}

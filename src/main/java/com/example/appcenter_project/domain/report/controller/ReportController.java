package com.example.appcenter_project.domain.report.controller;

import com.example.appcenter_project.domain.report.dto.request.RequestReportDto;
import com.example.appcenter_project.domain.report.dto.response.ResponseReportDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController implements ReportApiSpecification {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<Void> saveReport(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestReportDto requestReportDto) {
        reportService.saveReport(requestReportDto, user.getId());
        return ResponseEntity.status(OK).build();
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ResponseReportDto> findReport(@PathVariable Long reportId) {
        return ResponseEntity.status(OK).body(reportService.findReport(reportId));
    }

    @GetMapping
    public ResponseEntity<List<ResponseReportDto>> findAllReports() {
        return ResponseEntity.status(OK).body(reportService.findAllReports());
    }

    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}

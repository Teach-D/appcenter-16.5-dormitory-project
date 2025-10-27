package com.example.appcenter_project.domain.report.service;

import com.example.appcenter_project.domain.report.dto.request.RequestReportDto;
import com.example.appcenter_project.domain.report.dto.response.ResponseReportDto;
import com.example.appcenter_project.domain.report.entity.Report;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.report.repository.ReportRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public void saveReport(RequestReportDto requestDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Report report = Report.of(
                requestDto.getCategory(), requestDto.getTitle(), requestDto.getContent(), user
        );
        reportRepository.save(report);
    }

    public ResponseReportDto findReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new CustomException(REPORT_NOT_REGISTERED));
        return ResponseReportDto.from(report);
    }

    public List<ResponseReportDto> findAllReports() {
        List<Report> reports = reportRepository.findAll();

        return reports.stream()
                .map(ResponseReportDto::from)
                .toList();
    }

    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }
}

package com.example.appcenter_project.service.report;

import com.example.appcenter_project.dto.request.report.RequestReportDto;
import com.example.appcenter_project.dto.response.report.ResponseReportDto;
import com.example.appcenter_project.entity.report.Report;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.report.ReportRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    public void saveReport(RequestReportDto requestReportDto, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Report report = RequestReportDto.dtoToEntity(requestReportDto);
        report.updateUser(user);
        reportRepository.save(report);
    }

    public ResponseReportDto getReport(Long reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new CustomException(REPORT_NOT_REGISTERED));
        return ResponseReportDto.entityToDto(report);
    }

    public List<ResponseReportDto> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        Collections.reverse(reports);
        List<ResponseReportDto> responseReportDtos = new ArrayList<>();

        for (Report report : reports) {
            ResponseReportDto responseReportDto = ResponseReportDto.entityToDto(report);
            responseReportDtos.add(responseReportDto);
        }

        return responseReportDtos;
    }

    public void deleteReport(Long reportId) {
        reportRepository.deleteById(reportId);
    }
}

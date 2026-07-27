package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestReportOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatReportDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessageReport;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageReportRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OpenChatMessageReportService {

    private final OpenChatMessageReportRepository reportRepository;
    private final OpenChatMessageRepository openChatMessageRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final UserRepository userRepository;

    public void reportMessage(Long reporterId, Long messageId, RequestReportOpenChatMessageDto request) {
        OpenChatMessage message = openChatMessageRepository.findById(messageId)
                .orElseThrow(() -> new CustomException(OPEN_CHAT_MESSAGE_NOT_FOUND));


        if (message.getType() == OpenChatMessageType.SYSTEM) {
            throw new CustomException(OPEN_CHAT_REPORT_TARGET_INVALID);
        }
        if (!openChatParticipantRepository.existsByRoomIdAndUserId(message.getRoomId(), reporterId)) {
            throw new CustomException(OPEN_CHAT_NOT_PARTICIPANT);
        }
        if (message.getSenderId().equals(reporterId)) {
            throw new CustomException(OPEN_CHAT_REPORT_SELF);
        }

        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        User target = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        OpenChatMessageReport report = OpenChatMessageReport.create(
                message.getId(), message.getRoomId(),
                reporter.getId(), reporter.getStudentNumber(),
                target.getId(), target.getStudentNumber(),
                request.getReason(), message.getContent());
        reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public Page<ResponseOpenChatReportDto> findReports(ReportStatus status, Pageable pageable) {
        Page<OpenChatMessageReport> reports = reportRepository.findByStatus(status, pageable);

        Set<Long> userIds = reports.getContent().stream()
                .flatMap(r -> Stream.of(r.getReporterId(), r.getTargetUserId()))
                .collect(Collectors.toSet());

        // 이름 배치 조회. 탈퇴 계정은 맵에 없어 null 처리됨
        Map<Long, String> nameById = new HashMap<>();
        userRepository.findAllById(userIds).forEach(u -> nameById.put(u.getId(), u.getName()));

        return reports.map(r -> ResponseOpenChatReportDto.from(
                r, nameById.get(r.getReporterId()), nameById.get(r.getTargetUserId())));
    }

    public void approveReport(Long reportId) {
        OpenChatMessageReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(OPEN_CHAT_REPORT_NOT_FOUND));
        if (!report.isPending()) {
            throw new CustomException(OPEN_CHAT_REPORT_ALREADY_HANDLED);
        }
        report.approve();
    }

    public void cancelReport(Long reportId) {
        OpenChatMessageReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new CustomException(OPEN_CHAT_REPORT_NOT_FOUND));
        if (!report.isPending()) {
            throw new CustomException(OPEN_CHAT_REPORT_ALREADY_HANDLED);
        }
        report.cancel();
    }

    @Transactional(readOnly = true)
    public long countReports(String studentNumber, ReportStatus status) {
        return reportRepository.countByTargetStudentNumberAndStatus(studentNumber, status);
    }
}
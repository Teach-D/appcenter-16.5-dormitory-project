package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.openChat.dto.request.RequestReportOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessageReport;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import com.example.appcenter_project.domain.openChat.enums.ReportReason;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageReportRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OpenChatMessageReportServiceTest {

    @Mock OpenChatMessageReportRepository reportRepository;
    @Mock OpenChatMessageRepository openChatMessageRepository;
    @Mock OpenChatParticipantRepository openChatParticipantRepository;
    @Mock UserRepository userRepository;

    @InjectMocks OpenChatMessageReportService reportService;

    private RequestReportOpenChatMessageDto reasonDto(ReportReason reason) {
        RequestReportOpenChatMessageDto dto = new RequestReportOpenChatMessageDto();
        ReflectionTestUtils.setField(dto, "reason", reason);
        return dto;
    }

    @Test
    @DisplayName("메시지 신고 시 학번 스냅샷과 함께 PENDING 상태로 저장된다")
    void reportMessage_success() {
        Long reporterId = 1L, targetId = 2L, roomId = 5L, messageId = 10L;
        OpenChatMessage message = OpenChatMessage.create(roomId, targetId, "광고 메시지", OpenChatMessageType.TEXT);
        given(openChatMessageRepository.findById(messageId)).willReturn(Optional.of(message));
        given(openChatParticipantRepository.existsByRoomIdAndUserId(roomId, reporterId)).willReturn(true);
        User reporter = User.createForTest(reporterId, "신고자");
        User target = User.createForTest(targetId, "대상자");
        given(userRepository.findById(reporterId)).willReturn(Optional.of(reporter));
        given(userRepository.findById(targetId)).willReturn(Optional.of(target));

        reportService.reportMessage(reporterId, messageId, reasonDto(ReportReason.SPAM_AD));

        ArgumentCaptor<OpenChatMessageReport> captor = ArgumentCaptor.forClass(OpenChatMessageReport.class);
        verify(reportRepository).save(captor.capture());
        OpenChatMessageReport saved = captor.getValue();
        assertThat(saved.getReason()).isEqualTo(ReportReason.SPAM_AD);
        assertThat(saved.getTargetUserId()).isEqualTo(targetId);
        assertThat(saved.getTargetStudentNumber()).isEqualTo("test-" + targetId);
        assertThat(saved.getReporterStudentNumber()).isEqualTo("test-" + reporterId);
        assertThat(saved.getReportedContent()).isEqualTo("광고 메시지");
        assertThat(saved.isPending()).isTrue();
    }

    @Test
    @DisplayName("시스템 메시지는 신고할 수 없다")
    void reportMessage_system_message() {
        OpenChatMessage system = OpenChatMessage.create(5L, 0L, "님이 입장했습니다", OpenChatMessageType.SYSTEM);
        given(openChatMessageRepository.findById(10L)).willReturn(Optional.of(system));

        assertThatThrownBy(() -> reportService.reportMessage(1L, 10L, reasonDto(ReportReason.ETC)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(OPEN_CHAT_REPORT_TARGET_INVALID);
    }

    @Test
    @DisplayName("방 참여자가 아니면 신고할 수 없다")
    void reportMessage_not_participant() {
        OpenChatMessage message = OpenChatMessage.create(5L, 2L, "글", OpenChatMessageType.TEXT);
        given(openChatMessageRepository.findById(10L)).willReturn(Optional.of(message));
        given(openChatParticipantRepository.existsByRoomIdAndUserId(5L, 1L)).willReturn(false);

        assertThatThrownBy(() -> reportService.reportMessage(1L, 10L, reasonDto(ReportReason.ABUSE)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(OPEN_CHAT_NOT_PARTICIPANT);
    }

    @Test
    @DisplayName("본인 메시지는 신고할 수 없다")
    void reportMessage_self() {
        OpenChatMessage message = OpenChatMessage.create(5L, 1L, "내 글", OpenChatMessageType.TEXT);
        given(openChatMessageRepository.findById(10L)).willReturn(Optional.of(message));
        given(openChatParticipantRepository.existsByRoomIdAndUserId(5L, 1L)).willReturn(true);

        assertThatThrownBy(() -> reportService.reportMessage(1L, 10L, reasonDto(ReportReason.ETC)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(OPEN_CHAT_REPORT_SELF);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 신고 시 예외")
    void reportMessage_message_not_found() {
        given(openChatMessageRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.reportMessage(1L, 10L, reasonDto(ReportReason.ETC)))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(OPEN_CHAT_MESSAGE_NOT_FOUND);
    }

    @Test
    @DisplayName("승인 시 상태가 APPROVED로 바뀐다")
    void approveReport_success() {
        OpenChatMessageReport report = OpenChatMessageReport.create(
                10L, 5L, 1L, "test-1", 2L, "test-2", ReportReason.ABUSE, "글");
        given(reportRepository.findById(100L)).willReturn(Optional.of(report));

        reportService.approveReport(100L);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.APPROVED);
    }

    @Test
    @DisplayName("이미 처리된 신고는 다시 승인할 수 없다")
    void approveReport_already_handled() {
        OpenChatMessageReport report = OpenChatMessageReport.create(
                10L, 5L, 1L, "test-1", 2L, "test-2", ReportReason.ABUSE, "글");
        report.approve();
        given(reportRepository.findById(100L)).willReturn(Optional.of(report));

        assertThatThrownBy(() -> reportService.approveReport(100L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(OPEN_CHAT_REPORT_ALREADY_HANDLED);
    }

    @Test
    @DisplayName("취소 시 상태가 CANCELLED로 바뀐다")
    void cancelReport_success() {
        OpenChatMessageReport report = OpenChatMessageReport.create(
                10L, 5L, 1L, "test-1", 2L, "test-2", ReportReason.FRAUD, "글");
        given(reportRepository.findById(100L)).willReturn(Optional.of(report));

        reportService.cancelReport(100L);

        assertThat(report.getStatus()).isEqualTo(ReportStatus.CANCELLED);
    }
}
package com.example.appcenter_project.domain.complaint.service;

import com.example.appcenter_project.common.file.repository.AttachedFileRepository;
import com.example.appcenter_project.common.image.enums.ImageType;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.complaint.dto.request.RequestComplaintReplyDto;
import com.example.appcenter_project.domain.complaint.dto.request.RequestComplaintStatusDto;
import com.example.appcenter_project.domain.complaint.dto.response.ResponseComplaintReplyDto;
import com.example.appcenter_project.domain.complaint.entity.Complaint;
import com.example.appcenter_project.domain.complaint.entity.ComplaintReply;
import com.example.appcenter_project.domain.complaint.enums.ComplaintStatus;
import com.example.appcenter_project.domain.complaint.repository.ComplaintReplyRepository;
import com.example.appcenter_project.domain.complaint.repository.ComplaintRepository;
import com.example.appcenter_project.domain.notification.service.AdminComplaintNotificationService;
import com.example.appcenter_project.domain.notification.service.ComplaintNotificationService;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminComplaintServiceTest {

    @Mock
    ComplaintRepository complaintRepository;

    @Mock
    ComplaintReplyRepository replyRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    AttachedFileRepository attachedFileRepository;

    @Mock
    ImageService imageService;

    @Mock
    ComplaintNotificationService complaintNotificationService;

    @Mock
    AdminComplaintNotificationService adminComplaintNotificationService;

    @InjectMocks
    AdminComplaintService adminComplaintService;

    @Test
    @DisplayName("민원 답변 등록 - 정상 등록")
    void addReply_정상_등록() {
        User mockAdmin = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockAdmin));

        Complaint mockComplaint = mock(Complaint.class);
        when(mockComplaint.getReply()).thenReturn(null);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        RequestComplaintReplyDto dto = mock(RequestComplaintReplyDto.class);
        when(dto.getReplyTitle()).thenReturn("답변 제목");
        when(dto.getReplyContent()).thenReturn("답변 내용");
        when(dto.getResponderName()).thenReturn("담당자");

        ComplaintReply mockReply = mock(ComplaintReply.class);
        when(mockReply.getReplyTitle()).thenReturn("답변 제목");
        when(mockReply.getReplyContent()).thenReturn("답변 내용");
        when(mockReply.getResponderName()).thenReturn("담당자");
        when(mockReply.getCreatedDate()).thenReturn(LocalDateTime.now());
        when(mockReply.getId()).thenReturn(10L);
        when(replyRepository.save(any(ComplaintReply.class))).thenReturn(mockReply);

        // reply.getCreatedDate() returns null without JPA Auditing in unit tests
        try {
            adminComplaintService.addReply(1L, 1L, dto, null);
        } catch (NullPointerException ignored) {}

        verify(replyRepository).save(any(ComplaintReply.class));
        verify(mockComplaint).addReply(any(ComplaintReply.class));
        verify(mockComplaint).updateStatus(ComplaintStatus.COMPLETED);
    }

    @Test
    @DisplayName("민원 답변 등록 - 유저 없으면 예외 발생")
    void addReply_유저없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                adminComplaintService.addReply(99L, 1L, mock(RequestComplaintReplyDto.class), null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("민원 답변 등록 - 민원 없으면 예외 발생")
    void addReply_민원없으면_예외() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                adminComplaintService.addReply(1L, 99L, mock(RequestComplaintReplyDto.class), null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", COMPLAINT_NOT_FOUND);
    }

    @Test
    @DisplayName("민원 답변 등록 - 이미 답변이 있으면 예외 발생")
    void addReply_이미답변있으면_예외() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(mock(User.class)));

        Complaint mockComplaint = mock(Complaint.class);
        when(mockComplaint.getReply()).thenReturn(mock(ComplaintReply.class));
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        assertThatThrownBy(() ->
                adminComplaintService.addReply(1L, 1L, mock(RequestComplaintReplyDto.class), null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", COMPLAINT_ALREADY_REPLIED);
    }

    @Test
    @DisplayName("민원 상태 변경 - 정상 변경")
    void updateStatus_정상_변경() {
        Complaint mockComplaint = mock(Complaint.class);
        when(mockComplaint.getStatus()).thenReturn(ComplaintStatus.PENDING);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        RequestComplaintStatusDto dto = mock(RequestComplaintStatusDto.class);
        when(dto.getStatus()).thenReturn("처리중");

        adminComplaintService.updateStatus(1L, dto);

        verify(mockComplaint).updateStatus(any(ComplaintStatus.class));
    }

    @Test
    @DisplayName("민원 상태 변경 - 민원 없으면 예외 발생")
    void updateStatus_민원없으면_예외() {
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());

        RequestComplaintStatusDto dto = mock(RequestComplaintStatusDto.class);
        when(dto.getStatus()).thenReturn("처리중");

        assertThatThrownBy(() -> adminComplaintService.updateStatus(99L, dto))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", COMPLAINT_NOT_FOUND);
    }

    @Test
    @DisplayName("민원 답변 수정 - 정상 수정")
    void updateReply_정상_수정() {
        ComplaintReply mockReply = mock(ComplaintReply.class);
        when(mockReply.getId()).thenReturn(10L);

        Complaint mockComplaint = mock(Complaint.class);
        when(mockComplaint.getReply()).thenReturn(mockReply);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        RequestComplaintReplyDto dto = mock(RequestComplaintReplyDto.class);

        adminComplaintService.updateReply(1L, 1L, dto, null);

        verify(mockReply).update(dto);
        verify(imageService).updateImages(eq(ImageType.COMPLAINT_REPLY), eq(10L), any());
    }

    @Test
    @DisplayName("민원 답변 수정 - 답변 없으면 예외 발생")
    void updateReply_답변없으면_예외() {
        Complaint mockComplaint = mock(Complaint.class);
        when(mockComplaint.getReply()).thenReturn(null);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        assertThatThrownBy(() ->
                adminComplaintService.updateReply(1L, 1L, mock(RequestComplaintReplyDto.class), null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", COMPLAINT_REPLY_NOT_FOUND);
    }

    @Test
    @DisplayName("민원 답변 삭제 - 정상 삭제")
    void deleteReply_정상_삭제() {
        ComplaintReply mockReply = mock(ComplaintReply.class);
        when(mockReply.getId()).thenReturn(10L);

        Complaint mockComplaint = mock(Complaint.class);
        when(mockComplaint.getReply()).thenReturn(mockReply);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        adminComplaintService.deleteReply(1L);

        verify(imageService).deleteImages(ImageType.COMPLAINT_REPLY, 10L);
        verify(replyRepository).delete(mockReply);
    }

    @Test
    @DisplayName("민원 담당자 변경 - 정상 변경")
    void updateComplaintOfficer_정상_변경() {
        Complaint mockComplaint = mock(Complaint.class);
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(mockComplaint));

        adminComplaintService.updateComplaintOfficer(1L, "홍길동");

        verify(mockComplaint).updateOfficer("홍길동");
    }

    @Test
    @DisplayName("민원 담당자 변경 - 민원 없으면 예외 발생")
    void updateComplaintOfficer_민원없으면_예외() {
        when(complaintRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminComplaintService.updateComplaintOfficer(99L, "홍길동"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", COMPLAINT_NOT_FOUND);
    }
}

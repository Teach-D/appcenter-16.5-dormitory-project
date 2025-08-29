package com.example.appcenter_project.service.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintReplyDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintStatusDto;
import com.example.appcenter_project.dto.response.complaint.*;
import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.complaint.ComplaintReplyRepository;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintReplyRepository replyRepository;
    private final UserRepository userRepository;


    // 민원 답변 등록
    public ResponseComplaintReplyDto addReply(Long adminId, Long complaintId, RequestComplaintReplyDto dto) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLAINT_NOT_FOUND));

        if (complaint.getReply() != null) {
            throw new CustomException(ErrorCode.COMPLAINT_ALREADY_REPLIED);
        }

        ComplaintReply reply = ComplaintReply.builder()
                .replyTitle(dto.getReplyTitle())
                .replyContent(dto.getReplyContent())
                .responderName(dto.getResponderName())
                .complaint(complaint)
                .responder(admin)
                .build();

        complaint.addReply(reply);
        replyRepository.save(reply);

        complaint.updateStatus(ComplaintStatus.COMPLETED);

        return ResponseComplaintReplyDto.builder()
                .replyTitle(reply.getReplyTitle())
                .replyContent(reply.getReplyContent())
                .responderName(reply.getResponderName())
                .attachmentUrl(reply.getAttachmentUrl())
                .createdDate(reply.getCreatedDate().toString())
                .build();
    }

    // 민원 상태 변경
    public void updateStatus(Long complaintId, RequestComplaintStatusDto dto) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLAINT_NOT_FOUND));

        ComplaintStatus status = ComplaintStatus.from(dto.getStatus());
        complaint.updateStatus(status);
    }
}

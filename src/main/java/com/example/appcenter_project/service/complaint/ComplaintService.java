package com.example.appcenter_project.service.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintReplyDto;
import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.entity.complaint.ComplaintReply;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.complaint.ComplaintRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;

    // 민원 등록
    public ResponseComplaintDto createComplaint(Long userId, RequestComplaintDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getContent() == null || dto.getContent().isBlank()
                || dto.getCaseNumber() == null || dto.getCaseNumber().isBlank()
                || dto.getContact() == null || dto.getContact().isBlank()) {
            throw new CustomException(ErrorCode.COMPLAINT_REQUIRED_FIELD_MISSING);
        }

        ComplaintType type = ComplaintType.from(dto.getType());
        DormType dormType = DormType.from(dto.getDormType());

        Complaint complaint = Complaint.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .caseNumber(dto.getCaseNumber())
                .contact(dto.getContact())
                .dormType(dormType)
                .type(type)
                .user(user)
                .build();

        Complaint saved = complaintRepository.save(complaint);

        return ResponseComplaintDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .caseNumber(saved.getCaseNumber())
                .contact(saved.getContact())
                .dormType(saved.getDormType().toValue())
                .type(saved.getType().toValue())
                .status(saved.getStatus().toValue())
                .createdDate(saved.getCreatedDate().toString())
                .build();
    }

    // 민원 전체 조회 (최신순)
    public List<ResponseComplaintListDto> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedDateDesc()
                .stream()
                .map(c -> ResponseComplaintListDto.builder()
                        .date(c.getCreatedDate().toLocalDate()
                                .format(DateTimeFormatter.ofPattern("MM.dd")))
                        .type(c.getType().toValue())
                        .title(c.getTitle())
                        .status(c.getStatus().toValue())
                        .build())
                .collect(Collectors.toList());
    }

    //상세조회
    public ResponseComplaintDetailDto getComplaintDetail(Long complaintId) {
        Complaint c = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMPLAINT_NOT_FOUND));

        ResponseComplaintReplyDto replyDto = null;
        ComplaintReply r = c.getReply();
        if (r != null) {
            replyDto = ResponseComplaintReplyDto.builder()
                    .replyTitle(r.getReplyTitle())
                    .replyContent(r.getReplyContent())
                    .responderName(r.getResponderName())
                    .attachmentUrl(r.getAttachmentUrl())
                    .createdDate(r.getCreatedDate().toString())
                    .build();
        }

        return ResponseComplaintDetailDto.builder()
                .id(c.getId())
                .title(c.getTitle())
                .content(c.getContent())
                .type(c.getType().toValue())
                .dormType(c.getDormType().toValue())
                .caseNumber(c.getCaseNumber())
                .contact(c.getContact())
                .status(c.getStatus().toValue())
                .createdDate(c.getCreatedDate().toString())
                .reply(replyDto)
                .build();
    }



}

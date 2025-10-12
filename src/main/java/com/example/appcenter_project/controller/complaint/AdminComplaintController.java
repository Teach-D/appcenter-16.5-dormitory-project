package com.example.appcenter_project.controller.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintReplyDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintSearchDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintStatusDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintReplyDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.complaint.AdminComplaintService;
import com.example.appcenter_project.service.complaint.ComplaintService;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
public class AdminComplaintController implements AdminComplaintApiSpecification {

    private final ComplaintService complaintService;         // 기존 조회 로직 재사용
    private final AdminComplaintService adminComplaintService; // 관리자 전용 기능

    // 민원 전체 조회
    @GetMapping
    public ResponseEntity<List<ResponseComplaintListDto>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    // 민원 상세 조회
    @GetMapping("/{complaintId}")
    public ResponseEntity<ResponseComplaintDetailDto> getComplaintDetail(
            @PathVariable Long complaintId, HttpServletRequest request
    ) {
        return ResponseEntity.ok(complaintService.getComplaintDetail(complaintId, request));
    }

    // 민원 답변 등록
    @PostMapping(value = "/{complaintId}/reply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseComplaintReplyDto> addReply(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long complaintId,
            @RequestPart RequestComplaintReplyDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> images
    ) {
        ResponseComplaintReplyDto response =
                adminComplaintService.addReply(admin.getId(), complaintId, dto, images);
        return ResponseEntity.ok(response);
    }

    // 민원 상태 변경
    @PatchMapping("/{complaintId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long complaintId,
            @RequestBody RequestComplaintStatusDto dto
    ) {
        adminComplaintService.updateStatus(complaintId, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{complaintId}/officer/{officer}")
    public ResponseEntity<Void> updateComplaintOfficer(
            @PathVariable Long complaintId,
            @PathVariable String officer
    ) {
        adminComplaintService.updateComplaintOfficer(complaintId, officer);
        return ResponseEntity.ok().build();
    }

    // 민원 답변 수정
    @PutMapping(value = "/{complaintId}/reply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseComplaintReplyDto> updateReply(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long complaintId,
            @RequestPart RequestComplaintReplyDto dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> images
    ) {
        adminComplaintService.updateReply(admin.getId(), complaintId, dto, images);
        return ResponseEntity.status(OK).build();
    }

    // 민원 답변 삭제
    @DeleteMapping("/{complaintId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long complaintId) {
        adminComplaintService.deleteReply(complaintId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<ResponseComplaintListDto>> searchComplaints(
            @ModelAttribute RequestComplaintSearchDto dto
    ) {
        return ResponseEntity.ok(
                complaintService.searchComplaints(null, dto)
        );
    }


}

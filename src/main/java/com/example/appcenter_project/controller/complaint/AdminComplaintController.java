package com.example.appcenter_project.controller.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintReplyDto;
import com.example.appcenter_project.dto.request.complaint.RequestComplaintStatusDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintReplyDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.complaint.AdminComplaintService;
import com.example.appcenter_project.service.complaint.ComplaintService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/complaints")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
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
            @PathVariable Long complaintId
    ) {
        return ResponseEntity.ok(complaintService.getComplaintDetail(complaintId));
    }

    // 민원 답변 등록
    @PostMapping("/{complaintId}/reply")
    public ResponseEntity<ResponseComplaintReplyDto> addReply(
            @AuthenticationPrincipal CustomUserDetails admin,
            @PathVariable Long complaintId,
            @RequestBody RequestComplaintReplyDto dto
    ) {
        ResponseComplaintReplyDto response =
                adminComplaintService.addReply(admin.getId(), complaintId, dto);
        return ResponseEntity.ok(response);
    }

    // 민원 상태 변경
    @PutMapping("/{complaintId}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long complaintId,
            @RequestBody RequestComplaintStatusDto dto
    ) {
        adminComplaintService.updateStatus(complaintId, dto);
        return ResponseEntity.ok().build();
    }
}

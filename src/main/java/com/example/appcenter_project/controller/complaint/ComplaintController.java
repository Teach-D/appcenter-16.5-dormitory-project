package com.example.appcenter_project.controller.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.complaint.ComplaintService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/complaints")
@RequiredArgsConstructor
public class ComplaintController implements ComplaintApiSpecification {

    private final ComplaintService complaintService;

    // 민원 등록
    @Override
    @PostMapping
    public ResponseEntity<ResponseComplaintDto> createComplaint(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody RequestComplaintDto dto) {
        ResponseComplaintDto response = complaintService.createComplaint(userDetails.getId(), dto);
        return ResponseEntity.ok(response);
    }

    // 민원 목록 조회 (최신순)
    @Override
    @GetMapping
    public ResponseEntity<List<ResponseComplaintListDto>> getAllComplaints() {
        return ResponseEntity.ok(complaintService.getAllComplaints());
    }

    // 민원 상세 조회
    @Override
    @GetMapping("/{complaintId}")
    public ResponseEntity<ResponseComplaintDetailDto> getComplaint(
            @PathVariable Long complaintId) {
        return ResponseEntity.ok(complaintService.getComplaintDetail(complaintId));
    }
}

package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatReportDto;
import com.example.appcenter_project.domain.openChat.enums.ReportStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "OpenChat Report Admin", description = "오픈채팅 신고 관리자 API (ROLE_ADMIN 전용)")
public interface OpenChatReportAdminApiSpecification {

    @Operation(summary = "신고 목록 조회", description = "신고 목록을 최신순으로 페이징 조회한다. status 미지정 시 전체. ROLE_ADMIN 전용.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
    })
    ResponseEntity<Page<ResponseOpenChatReportDto>> findReports(
            @Parameter(description = "신고 상태(미지정 시 전체)", example = "PENDING")
            @RequestParam(required = false) ReportStatus status,
            @ParameterObject Pageable pageable);

    @Operation(summary = "신고 승인", description = "승인대기 상태의 신고를 승인 처리.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신고", content = @Content),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음", content = @Content)
    })
    ResponseEntity<Void> approveReport(
            @Parameter(description = "신고 ID", required = true, example = "1") @PathVariable Long reportId);

    @Operation(summary = "신고 취소(반려)", description = "승인대기 상태의 신고를 취소 처리.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "이미 처리된 신고", content = @Content),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
            @ApiResponse(responseCode = "404", description = "신고를 찾을 수 없음", content = @Content)
    })
    ResponseEntity<Void> cancelReport(
            @Parameter(description = "신고 ID", required = true, example = "1") @PathVariable Long reportId);

    @Operation(summary = "학번 기준 신고 수", description = "특정 학번이 대상인 신고 수. status 미지정 시 전체.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "집계 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
    })
    ResponseEntity<Long> countReports(
            @Parameter(description = "대상 학번", required = true) @RequestParam String studentNumber,
            @Parameter(description = "집계 상태(미지정 시 전체)", example = "APPROVED") @RequestParam(required = false) ReportStatus status);
}
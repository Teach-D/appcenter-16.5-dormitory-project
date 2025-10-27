package com.example.appcenter_project.domain.report.controller;

import com.example.appcenter_project.domain.report.dto.request.RequestReportDto;
import com.example.appcenter_project.domain.report.dto.response.ResponseReportDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Report", description = "신고 관리 API")
public interface ReportApiSpecification {

    @Operation(
            summary = "신고 등록",
            description = "새로운 신고를 등록합니다. 인증된 사용자만 신고를 등록할 수 있습니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 등록 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content
            )
    })
    @PostMapping
    ResponseEntity<Void> saveReport(
            @Parameter(hidden = true)
            @AuthenticationPrincipal CustomUserDetails user,
            @Parameter(description = "신고 등록 요청 데이터", required = true)
            @Valid @RequestBody RequestReportDto requestReportDto
    );

    @Operation(
            summary = "특정 신고 조회",
            description = "신고 ID를 통해 특정 신고의 상세 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseReportDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "신고를 찾을 수 없음",
                    content = @Content
            )
    })
    @GetMapping("/{reportId}")
    ResponseEntity<ResponseReportDto> findReport(
            @Parameter(description = "신고 ID", required = true, example = "1")
            @PathVariable Long reportId
    );

    @Operation(
            summary = "모든 신고 조회",
            description = "등록된 모든 신고를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 목록 조회 성공",
                    content = @Content(schema = @Schema(implementation = ResponseReportDto.class))
            )
    })
    @GetMapping
    ResponseEntity<List<ResponseReportDto>> findAllReports();

    @Operation(
            summary = "신고 삭제",
            description = "신고 ID를 통해 특정 신고를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "신고 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "신고를 찾을 수 없음",
                    content = @Content
            )
    })
    @DeleteMapping("/{reportId}")
    ResponseEntity<Void> deleteReport(
            @Parameter(description = "삭제할 신고 ID", required = true, example = "1")
            @PathVariable Long reportId
    );
}

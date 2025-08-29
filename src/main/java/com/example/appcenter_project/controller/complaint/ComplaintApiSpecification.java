package com.example.appcenter_project.controller.complaint;

import com.example.appcenter_project.dto.request.complaint.RequestComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintDetailDto;
import com.example.appcenter_project.dto.response.complaint.ResponseComplaintListDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Complaint API", description = "민원 관련 API")
public interface ComplaintApiSpecification {

    @Operation(
            summary = "민원 등록",
            description = "로그인한 사용자가 민원을 등록합니다. 상태는 기본 대기중으로 저장됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "등록 성공",
                            content = @Content(schema = @Schema(implementation = ResponseComplaintDto.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
            }
    )
    ResponseEntity<ResponseComplaintDto> createComplaint(
            @AuthenticationPrincipal
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails,

            @Valid @RequestBody
            @Parameter(description = "민원 등록 정보", required = true)
            RequestComplaintDto requestComplaintDto
    );

    @Operation(
            summary = "민원 목록 조회(최신순)",
            description = "등록된 민원을 최신순으로 조회합니다. 목록용 필드(날짜, 유형, 제목, 현황)만 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseComplaintListDto.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponseComplaintListDto>> getAllComplaints();

    @Operation(
            summary = "민원 상세 조회",
            description = "민원 ID로 상세 정보를 조회합니다. 답변이 존재하면 함께 반환됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseComplaintDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "민원을 찾을 수 없음")
            }
    )
    ResponseEntity<ResponseComplaintDetailDto> getComplaint(
            @PathVariable
            @Parameter(description = "민원 ID", required = true, example = "1")
            Long complaintId
    );
}

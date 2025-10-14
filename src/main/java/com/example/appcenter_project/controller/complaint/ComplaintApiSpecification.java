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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Complaint API", description = "민원 관련 API")
public interface ComplaintApiSpecification {

    @Operation(
            summary = "민원 등록",
            description = "로그인한 사용자가 민원을 등록합니다. 상태는 기본 대기중으로 저장됩니다. 파일 첨부가 가능합니다." +
            "type : 소음,흡연,음주,호실변경신청,벌점 및 상점 문의, 물건 적치 신고",
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

            @Valid @RequestPart
            @Parameter(description = "민원 등록 정보", required = true)
            RequestComplaintDto dto,
            
            @RequestPart(value = "files", required = false)
            @Parameter(description = "첨부 파일 목록 (선택사항)")
            List<MultipartFile> files
    );

    @Operation(
            summary = "내 민원 목록 조회 (최신순)",
            description = "로그인한 사용자가 등록한 민원을 최신순으로 조회합니다. 목록용 필드(날짜, 유형, 제목, 현황)만 반환합니다.",
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
    ResponseEntity<List<ResponseComplaintListDto>> getAllComplaints(
            @AuthenticationPrincipal 
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails
    );

    @Operation(
            summary = "민원 상세 조회",
            description = "민원 ID로 상세 정보를 조회합니다. 본인이 작성한 민원만 조회 가능하며, 답변이 존재하면 함께 반환됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseComplaintDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "민원을 찾을 수 없음"),
                    @ApiResponse(responseCode = "403", description = "본인의 민원이 아님")
            }
    )
    ResponseEntity<ResponseComplaintDetailDto> getComplaint(
            @AuthenticationPrincipal 
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails,
            
            @PathVariable
            @Parameter(description = "민원 ID", required = true, example = "1")
            Long complaintId,
            
            @Parameter(description = "HTTP 요청 정보", hidden = true)
            HttpServletRequest request
    );

    @Operation(
            summary = "민원 수정",
            description = "본인이 작성한 민원을 수정합니다. 처리 완료된 민원은 수정할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "404", description = "민원을 찾을 수 없음"),
                    @ApiResponse(responseCode = "403", description = "본인의 민원이 아니거나 수정할 수 없는 상태")
            }
    )
    ResponseEntity<Void> updateComplaint(
            @AuthenticationPrincipal
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails,
            
            @PathVariable
            @Parameter(description = "민원 ID", required = true, example = "1")
            Long complaintId,
            
            @RequestPart
            @Parameter(description = "민원 수정 정보", required = true)
            RequestComplaintDto dto,
            
            @RequestPart(value = "files", required = false)
            @Parameter(description = "첨부 파일 목록 (선택사항)")
            List<MultipartFile> files
    );

    @Operation(
            summary = "민원 삭제",
            description = "본인이 작성한 민원을 삭제합니다. 처리 완료된 민원은 삭제할 수 없습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "민원을 찾을 수 없음"),
                    @ApiResponse(responseCode = "403", description = "본인의 민원이 아니거나 삭제할 수 없는 상태")
            }
    )
    ResponseEntity<Void> deleteComplaint(
            @AuthenticationPrincipal
            @Parameter(description = "로그인 사용자", required = true)
            CustomUserDetails userDetails,
            
            @PathVariable
            @Parameter(description = "민원 ID", required = true, example = "1")
            Long complaintId
    );
}

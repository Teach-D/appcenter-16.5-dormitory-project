package com.example.appcenter_project.controller.announcement;

import com.example.appcenter_project.dto.AttachedFileDto;
import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDto;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "공지사항 API", description = "공지사항 관리 관련 API")
public interface AnnouncementApiSpecification {

    @Operation(
            summary = "공지사항 생성",
            description = "새로운 공지사항을 생성합니다. 파일 첨부가 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "공지사항 생성 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "401", description = "인증 필요"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    ResponseEntity<Void> saveAnnouncement(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestAnnouncementDto")
            @Parameter(description = "공지사항 정보", required = true) RequestAnnouncementDto requestAnnouncementDto,
            @RequestPart(value = "files", required = false)
            @Parameter(description = "첨부 파일들 (선택사항)") List<MultipartFile> files);

    @Operation(
            summary = "모든 공지사항 조회",
            description = "모든 공지사항 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseAnnouncementDto.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponseAnnouncementDto>> findAllAnnouncements();

    @Operation(
            summary = "특정 공지사항 조회",
            description = "ID로 특정 공지사항의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseAnnouncementDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
            }
    )
    ResponseEntity<ResponseAnnouncementDetailDto> findAnnouncement(
            @PathVariable
            @Parameter(description = "공지사항 ID", required = true, example = "1") Long announcementId);

    @Operation(
            summary = "공지사항 첨부파일 조회",
            description = "특정 공지사항의 첨부파일 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = AttachedFileDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
            }
    )
    ResponseEntity<List<AttachedFileDto>> findAnnouncementFile(
            @PathVariable
            @Parameter(description = "공지사항 ID", required = true, example = "1") Long announcementId,
            HttpServletRequest request);

    @Operation(
            summary = "공지사항 수정",
            description = "기존 공지사항을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = ResponseAnnouncementDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    ResponseEntity<ResponseAnnouncementDto> updateAnnouncement(
            @Valid @RequestBody
            @Parameter(description = "수정할 공지사항 정보", required = true) RequestAnnouncementDto requestAnnouncementDto,
            @PathVariable
            @Parameter(description = "공지사항 ID", required = true, example = "1") Long announcementId);

    @Operation(
            summary = "공지사항 및 첨부파일 수정",
            description = "기존 공지사항과 첨부파일을 함께 수정합니다. 기존 첨부파일은 모두 삭제되고 새로운 파일들로 교체됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = ResponseAnnouncementDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    ResponseEntity<ResponseAnnouncementDto> updateAnnouncementWithFiles(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestAnnouncementDto")
            @Parameter(description = "수정할 공지사항 정보", required = true) RequestAnnouncementDto requestAnnouncementDto,
            @PathVariable
            @Parameter(description = "공지사항 ID", required = true, example = "1") Long announcementId,
            @RequestPart(value = "files", required = false)
            @Parameter(description = "새로운 첨부 파일들 (선택사항)") List<MultipartFile> files);


    @Operation(
            summary = "공지사항 삭제",
            description = "공지사항을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    void deleteAnnouncement(
            @PathVariable
            @Parameter(description = "공지사항 ID", required = true, example = "1") Long announcementId);
}

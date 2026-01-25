package com.example.appcenter_project.domain.announcement.controller;

import com.example.appcenter_project.common.file.dto.AttachedFileDto;
import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "공지사항 API", description = "공지사항 관리 관련 API")
public interface AnnouncementApiSpecification {

    @Operation(
            summary = "공지사항 생성",
            description = """
        새로운 공지사항을 생성합니다. 파일 첨부가 가능합니다.
        
        ### 카테고리 입력 값 (category)
        
        | 영문 코드 | 한글 의미 |
        |----------|----------|
        | LIFE_GUIDANCE | 생활지도 |
        | FACILITY | 시설 |
        | EVENT_LECTURE | 행사/강좌 |
        | BTL_DORMITORY | BTL기숙사 |
        | ETC | 기타 |
        | MOVE_IN_OUT | 입퇴사 공지 |
       
        """,
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
            description = """
        모든 공지사항 조회합니다.
        
        ### 카테고리 출력 값 (category)
        
        | 영문 코드 | 한글 의미 |
        |----------|----------|
        | LIFE_GUIDANCE | 생활지도 |
        | FACILITY | 시설 |
        | EVENT_LECTURE | 행사/강좌 |
        | BTL_DORMITORY | BTL기숙사 |
        | ETC | 기타 |
        | MOVE_IN_OUT | 입퇴사 공지 |
       
        """,
            parameters = {
                    @Parameter(
                            name = "type",
                            description = "공지사항 작성 주체(ALL, DORMITORY, UNI_DORM, SUPPORTERS)",
                            example = "DORMITORY",
                            schema = @Schema(type = "string", allowableValues = {"ALL", "DORMITORY", "UNI_DORM", "SUPPORTERS"})
                    ),
                    @Parameter(
                            name = "category",
                            description = "공지사항 카테고리(ALL(전체), LIFE_GUIDANCE(생활지도), FACILITY(시설), " +
                                    "EVENT_LECTURE(행사/강좌), BTL_DORMITORY(BTL기숙사), " +
                                    "MOVE_IN_OUT(입퇴사 공지), ETC(기타))",
                            example = "ALL",
                            schema = @Schema(type = "string", allowableValues =
                                    {"ALL", "LIFE_GUIDANCE", "FACILITY", "EVENT_LECTURE", "BTL_DORMITORY", "MOVE_IN_OUT", "ETC"})
                    )
            },
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
    ResponseEntity<List<ResponseAnnouncementDto>> findAllAnnouncements(
            @RequestParam(defaultValue = "생활원") String type, @RequestParam(defaultValue = "입퇴사 공지") String category, String search
    );

    @Operation(
            summary = "모든 공지사항 조회 (무한스크롤)",
            description = """
    모든 공지사항을 무한스크롤 방식으로 조회합니다.
    
    ### 무한스크롤 사용법
    - 첫 로딩: lastId 없이 호출
    - 다음 페이지: 이전 응답의 마지막 게시물 ID를 lastId로 전달
    - 빈 배열 반환 시 더 이상 데이터 없음
    
    ### 카테고리 출력 값 (category)
    
    | 영문 코드 | 한글 의미 |
    |----------|----------|
    | LIFE_GUIDANCE | 생활지도 |
    | FACILITY | 시설 |
    | EVENT_LECTURE | 행사/강좌 |
    | BTL_DORMITORY | BTL기숙사 |
    | ETC | 기타 |
    | MOVE_IN_OUT | 입퇴사 공지 |
   
    """,
            parameters = {
                    @Parameter(
                            name = "type",
                            description = "공지사항 작성 주체(ALL, DORMITORY, UNI_DORM, SUPPORTERS)",
                            example = "DORMITORY",
                            schema = @Schema(type = "string", allowableValues = {"ALL", "DORMITORY", "UNI_DORM", "SUPPORTERS"})
                    ),
                    @Parameter(
                            name = "category",
                            description = "공지사항 카테고리(ALL(전체), LIFE_GUIDANCE(생활지도), FACILITY(시설), " +
                                    "EVENT_LECTURE(행사/강좌), BTL_DORMITORY(BTL기숙사), " +
                                    "MOVE_IN_OUT(입퇴사 공지), ETC(기타))",
                            example = "ALL",
                            schema = @Schema(type = "string", allowableValues =
                                    {"ALL", "LIFE_GUIDANCE", "FACILITY", "EVENT_LECTURE", "BTL_DORMITORY", "MOVE_IN_OUT", "ETC"})
                    ),
                    @Parameter(
                            name = "search",
                            description = "검색어 (제목 기준)",
                            example = "입사",
                            required = false
                    ),
                    @Parameter(
                            name = "lastId",
                            description = "마지막으로 조회한 공지사항 ID (무한스크롤용 커서, 첫 로딩 시 null)",
                            example = "50",
                            required = false,
                            schema = @Schema(type = "integer", format = "int64")
                    ),
                    @Parameter(
                            name = "size",
                            description = "한 번에 조회할 공지사항 개수",
                            example = "10",
                            schema = @Schema(type = "integer", defaultValue = "10")
                    )
            },
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
    ResponseEntity<List<ResponseAnnouncementDto>> findAllAnnouncementsScroll(
            @RequestParam(defaultValue = "생활원") String type,
            @RequestParam(defaultValue = "입퇴사 공지") String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long lastId,
            @RequestParam(defaultValue = "10") int size
    );

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
            @AuthenticationPrincipal CustomUserDetails user,
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
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공지사항 ID", required = true, example = "1") Long announcementId);
}

package com.example.appcenter_project.controller.tip;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.tip.TipImageDto;
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
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Tip API", description = "팁 게시글 관련 API")
public interface TipApiSpecification {

    @Operation(
            summary = "팁 게시글 등록",
            description = "토큰, 팁 등록정보, 이미지 파일을 통해 팁 게시글 등록을 진행합니다. 이미지는 선택사항입니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "팁 게시글 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    public ResponseEntity<Void> saveTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestTipDto")
            @Parameter(description = "팁 게시글 등록 정보", required = true) RequestTipDto requestTipDto,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "팁 게시글 이미지 파일 목록 (선택사항)", required = false) List<MultipartFile> images);

    @Operation(
            summary = "모든 팁 게시글 조회",
            description = "등록된 모든 팁 게시글 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팁 게시글 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseTipDto.class))
                            )
                    )
            }
    )
    public ResponseEntity<List<ResponseTipDto>> findAllTips();

    @Operation(
            summary = "일일 랜덤 팁 게시글 3개 조회",
            description = "하루 동안 고정된 랜덤 팁 게시글 3개를 조회합니다. 매일 자정에 새로운 3개의 팁이 선정됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "일일 랜덤 팁 게시글 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseTipDto.class))
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "등록된 팁 게시글이 3개 미만입니다.",
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<List<ResponseTipDto>> findDailyRandomTips();

    @Operation(
            summary = "특정 팁 게시글 상세 조회",
            description = "팁 게시글 ID로 특정 팁 게시글의 상세 정보를 조회합니다. (이미지 제외)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팁 게시글 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseTipDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팁 게시글 ID입니다.", content = @Content(examples = {}))
            }
    )
    public ResponseEntity<ResponseTipDetailDto> findTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId,
            HttpServletRequest request
    );

/*
    @Operation(
            summary = "특정 팁 게시글의 이미지 메타정보 조회",
            description = "팁 게시글 ID로 해당 게시글의 이미지 메타데이터 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팁 게시글 이미지 메타정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TipImageDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 존재하지 않는 팁 게시글 ID입니다. (TIP_NOT_FOUND)
                            - 존재하지 않는 이미지 파일입니다. (IMAGE_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<List<TipImageDto>> getTipImages(
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId);

    @Operation(
            summary = "팁 게시글 이미지 파일 조회",
            description = "파일명으로 실제 이미지 파일을 응답합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이미지 파일 조회 성공",
                            content = @Content(mediaType = "image/*")
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 이미지 파일입니다.", content = @Content(examples = {}))
            }
    )
    public ResponseEntity<Resource> viewImage(
            @RequestParam
            @Parameter(description = "이미지 파일명", required = true, example = "uuid_image.jpg") String filename);
*/

    @Operation(
            summary = "팁 게시글 좋아요",
            description = "특정 팁 게시글에 좋아요를 추가합니다. 좋아요 후 총 좋아요 수를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 추가 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "integer", description = "총 좋아요 수", example = "15")
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "이미 좋아요한 게시글입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 존재하지 않는 팁 게시글 ID입니다. (TIP_NOT_FOUND)
                            - 회원가입하지 않은 사용자입니다. (USER_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<Integer> likePlusTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId);

    @Operation(
            summary = "팁 게시글 좋아요 취소",
            description = "특정 팁 게시글의 좋아요를 취소합니다. 좋아요 취소 후 총 좋아요 수를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 취소 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "integer", description = "총 좋아요 수", example = "14")
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "좋아요를 누르지 않은 게시글입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 존재하지 않는 팁 게시글 ID입니다. (TIP_NOT_FOUND)
                            - 회원가입하지 않은 사용자입니다. (USER_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<Integer> unlikePlusTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId);

    @Operation(
            summary = "팁 게시글 수정",
            description = "작성자만 자신의 팁 게시글을 수정할 수 있습니다. 이미지는 선택사항이며, 이미지를 제공하면 기존 이미지가 교체됩니다.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "팁 게시글 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 수정 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 팁 게시글입니다.")
            }
    )
    public ResponseEntity<Void> updateTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart
            @Parameter(description = "수정할 팁 게시글 정보", required = true) RequestTipDto requestTipDto,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "수정할 이미지 파일 목록 (선택사항, 제공시 기존 이미지 교체)", required = false) List<MultipartFile> images,
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId);

    @Operation(
            summary = "팁 게시글 삭제",
            description = "작성자만 자신의 팁 게시글을 삭제할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "팁 게시글 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 삭제 권한이 없습니다."),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 존재하지 않는 팁 게시글 ID입니다. (TIP_NOT_FOUND)
                            - 회원가입하지 않은 사용자입니다. (USER_NOT_FOUND)
                            """
                    )
            }
    )
    public ResponseEntity<Void> deleteTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId);

    @Operation(
            summary = "팁 게시글 이미지 목록 조회",
            description = "팁 게시글의 모든 이미지 URL 정보를 목록으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "팁 게시글 이미지 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ImageLinkDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404",
                            description = "존재하지 않는 팁 게시글 ID입니다.",
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<List<ImageLinkDto>> findTipImagesByTipId(
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId,
            HttpServletRequest request);

    @Operation(
            summary = "팁 게시글 이미지 전체 삭제",
            description = "작성자만 자신의 팁 게시글의 모든 이미지를 삭제할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "팁 게시글 이미지 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 삭제 권한이 없습니다."),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 존재하지 않는 팁 게시글 ID입니다. (TIP_NOT_FOUND)
                            - 회원가입하지 않은 사용자입니다. (USER_NOT_FOUND)
                            """
                    )
            }
    )
    public ResponseEntity<Void> deleteTipImages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "팁 게시글 ID", required = true, example = "1") Long tipId);
}

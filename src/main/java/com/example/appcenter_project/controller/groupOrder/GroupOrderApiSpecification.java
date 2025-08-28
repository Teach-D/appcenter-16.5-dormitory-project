package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderPopularSearch;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Tag(name = "Group Order API", description = "공동구매 게시글 관련 API")
public interface GroupOrderApiSpecification {

    @Operation(
            summary = "공동구매 게시글 등록",
            description = "토큰, 공동구매 등록정보, 이미지 파일을 통해 공동구매 게시글 등록을 진행합니다. 이미지는 선택사항입니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "공동구매 게시글 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Void> saveGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestGroupOrderDto")
            @Parameter(description = "공동구매 게시글 등록 정보", required = true) RequestGroupOrderDto requestGroupOrderDto,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "공동구매 게시글 이미지 파일 목록 (선택사항)", required = false) List<MultipartFile> images);

    @Operation(
            summary = "공동구매 평점 추가",
            description = "완료된 공동구매에 대해 평점을 추가합니다. 평점은 0.5 단위로 1.0부터 5.0까지 가능합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "평점 추가 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 평점 값입니다. (1.0~5.0, 0.5 단위)"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 평점 추가 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글입니다."),
                    @ApiResponse(responseCode = "409", description = "이미 평점을 추가한 공동구매입니다.")
            }
    )
    ResponseEntity<Void> addRating(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId,
            @PathVariable
            @Parameter(description = "평점 (1.0~5.0, 0.5 단위)", required = true, example = "4.5") Float ratingScore);

    @Operation(
            summary = "특정 공동구매 게시글 상세 조회",
            description = "공동구매 게시글 ID로 특정 공동구매 게시글의 상세 정보를 조회합니다. (이미지 제외)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공동구매 게시글 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글 ID입니다.")
            }
    )
    ResponseEntity<ResponseGroupOrderDetailDto> findGroupOrderById(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId,
            HttpServletRequest request);

    @Operation(
            summary = "공동구매 게시글 이미지 목록 조회",
            description = "공동구매 게시글의 모든 이미지 URL 정보를 목록으로 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공동구매 게시글 이미지 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ImageLinkDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글 ID입니다.")
            }
    )
    ResponseEntity<List<ImageLinkDto>> getGroupOrderImages(
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId,
            HttpServletRequest request);

    @Operation(
            summary = "사용자의 공동구매 검색 기록 조회",
            description = "현재 로그인한 사용자의 공동구매 검색 기록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "검색 기록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(type = "string", example = "치킨"))
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<List<String>> findGroupOrderSearchLog(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(
            summary = "공동구매 게시글 목록 조회",
            description = "정렬 방식, 카테고리, 검색어를 통해 공동구매 게시글 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공동구매 게시글 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseGroupOrderDto.class))
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.")
            }
    )
    ResponseEntity<List<ResponseGroupOrderDto>> findGroupOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "마감임박순")
            @Parameter(description = "정렬 방식", example = "조회순",
                    schema = @Schema(allowableValues = {"조회순", "가격순", "마감임박순"})) String sort,
            @RequestParam(defaultValue = "전체")
            @Parameter(description = "공동구매 카테고리", example = "전체",
                    schema = @Schema(allowableValues = {"전체", "배달", "식료품", "생활용품", "기타"})) String type,
            @RequestParam(required = false)
            @Parameter(description = "검색어 (선택사항)", example = "치킨") Optional<String> search,
            HttpServletRequest request);

    @Operation(
            summary = "인기 검색어 조회",
            description = "공동구매 게시글의 인기 검색어 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "인기 검색어 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseGroupOrderPopularSearch.class))
                            )
                    )
            }
    )
    ResponseEntity<List<ResponseGroupOrderPopularSearch>> findGroupOrderPopularSearch();

    @Operation(
            summary = "공동구매 게시글 좋아요",
            description = "특정 공동구매 게시글에 좋아요를 추가합니다. 좋아요 후 총 좋아요 수를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 추가 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "integer", description = "총 좋아요 수", example = "15")
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "이미 좋아요한 게시글입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글이거나 회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Integer> likePlusGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId);

    @Operation(
            summary = "공동구매 게시글 좋아요 취소",
            description = "특정 공동구매 게시글의 좋아요를 취소합니다. 좋아요 취소 후 총 좋아요 수를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "좋아요 취소 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "integer", description = "총 좋아요 수", example = "14")
                            )
                    ),
                    @ApiResponse(responseCode = "401", description = "좋아요를 누르지 않은 게시글입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글이거나 회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Integer> likeMinusGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId);

    @Operation(
            summary = "공동구매 모집 완료 처리",
            description = "작성자만 자신의 공동구매 게시글을 모집 완료 상태로 변경할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모집 완료 처리 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 완료 처리 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글이거나 회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Void> completeGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId);

    @Operation(
            summary = "공동구매 모집 완료 취소",
            description = "작성자만 자신의 공동구매 게시글의 모집 완료 상태를 취소할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "모집 완료 취소 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 취소 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글이거나 회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Void> unCompleteGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId);

    @Operation(
            summary = "공동구매 게시글 수정",
            description = "작성자만 자신의 공동구매 게시글을 수정할 수 있습니다. 이미지는 선택사항이며, 이미지를 제공하면 기존 이미지가 교체됩니다.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "공동구매 게시글 수정 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 수정 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글입니다.")
            }
    )
    ResponseEntity<Void> updateGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId,
            @Valid @RequestPart("requestGroupOrderDto")
            @Parameter(description = "수정할 공동구매 게시글 정보", required = true) RequestGroupOrderDto requestGroupOrderDto,
            @RequestPart(value = "images", required = false)
            @Parameter(description = "수정할 이미지 파일 목록 (선택사항, 제공시 기존 이미지 교체)", required = false) List<MultipartFile> images);

    @Operation(
            summary = "공동구매 게시글 삭제",
            description = "작성자만 자신의 공동구매 게시글을 삭제할 수 있습니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "공동구매 게시글 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 삭제 권한이 없습니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글이거나 회원가입하지 않은 사용자입니다.")
            }
    )
    ResponseEntity<Void> deleteGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "공동구매 게시글 ID", required = true, example = "1") Long groupOrderId);
}

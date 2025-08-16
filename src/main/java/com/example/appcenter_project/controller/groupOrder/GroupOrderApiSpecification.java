package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderDto;
import com.example.appcenter_project.dto.response.groupOrder.GroupOrderImageDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Tag(name = "GroupOrder", description = "공동구매 관련 API")
public interface GroupOrderApiSpecification {

    @Operation(
            summary = "공동구매 등록",
            description = "토큰, 공동구매 등록정보, 이미지 파일을 통해 등록을 진행합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "공동구매 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "회원가입하지 않은 사용자입니다."),
                    @ApiResponse(responseCode = "409", description = "공동구매 제목이 중복되었습니다.")
            }
    )
    ResponseEntity<Void> saveGroupOrder(@AuthenticationPrincipal CustomUserDetails user,
                                        @Valid @RequestPart RequestGroupOrderDto requestGroupOrderDto, @RequestPart List<MultipartFile> images);


    @Operation(
            summary = "공동구매 단건 조회",
            description = "공동구매 아이디로 조회를 진행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공동구매 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 아이디입니다."),
            }
    )
    ResponseEntity<ResponseGroupOrderDetailDto> findGroupOrderById(@PathVariable Long groupOrderId);

    @Operation(
            summary = "공동구매 단건 이미지 링크 조회",
            description = "공동구매 아이디로 이미지 링크 조회를 진행합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공동구매 이미지 링크 조회 성공",
                            content = @Content(schema = @Schema(implementation = GroupOrderImageDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 아이디입니다."),
            }
    )
    ResponseEntity<List<GroupOrderImageDto>> getGroupOrderImages(@PathVariable Long groupOrderId);


    @Operation(
            summary = "공동구매 최근 검색어 조회",
            description = "유저의 공동구매 최근 검색어를 최대 5개까지 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공동구매 최근 검색어 조회 성공",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저입니다."),
            }
    )
    ResponseEntity<List<String>> findGroupOrderSearchLog(@AuthenticationPrincipal CustomUserDetails user);


    @Operation(
            summary = "이미지 보기",
            description = "filename에 해당하는 이미지를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이미지 반환 성공",
                            content = @Content(mediaType = "image/*")
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 이미지를 찾을 수 없습니다.")
            }
    )
    ResponseEntity<Resource> viewImage(@RequestParam String filename);


    @Operation(
            summary = "공동구매 게시글 목록 조회",
            description = "정렬 기준, 타입, 검색어를 기반으로 공동구매 게시글 목록을 조회합니다. 정렬기준이 없을 시 최신순입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "공동구매 게시글 목록 조회 성공",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResponseGroupOrderDto.class)))),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저입니다."),
            }
    )
    ResponseEntity<List<ResponseGroupOrderDto>> findGroupOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "DEADLINE") String sort,
            @RequestParam(defaultValue = "ALL") String type,
            @RequestParam(required = false) Optional<String> search
    );


    @Operation(
            summary = "공동구매 게시글 좋아요",
            description = "특정 공동구매 게시글에 좋아요를 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 추가 성공"),
                    @ApiResponse(responseCode = "400", description = "이미 좋아요를 누른 게시글입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글 또는 유저입니다.")
            }
    )
    ResponseEntity<Integer> likePlusGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupOrderId
    );

    @Operation(
            summary = "공동구매 게시글 좋아요 취소",
            description = "특정 공동구매 게시글의 좋아요를 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
                    @ApiResponse(responseCode = "400", description = "좋아요를 누르지 않은 게시글입니다."),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 게시글 또는 유저입니다.")
            }
    )
    ResponseEntity<Integer> likeMinusGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupOrderId
    );

    @Operation(
            summary = "공동구매 게시글 수정",
            description = "자신이 작성한 공동구매 게시글을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "공동구매 게시글 수정 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderDetailDto.class))),
                    @ApiResponse(responseCode = "400", description = "DTO 입력이 잘못되었습니다."),
                    @ApiResponse(responseCode = "403", description = "해당 공동구매 게시글을 생성을 하지 않았기 때문에 권한이 없는 사용자입니다."),
                    @ApiResponse(responseCode = "409", description = "공동구매 제목이 중복되었습니다.")

            }
    )
    ResponseEntity<ResponseGroupOrderDetailDto> updateGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupOrderId,
            @Valid @RequestBody RequestGroupOrderDto requestGroupOrderDto
    );


    @Operation(
            summary = "공동구매 게시글 삭제",
            description = "자신이 작성한 공동구매 게시글을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "공동구매 게시글 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "권한이 없는 사용자입니다."),
            }
    )
    ResponseEntity<Void> deleteGroupOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupOrderId
    );

    @Operation(
            summary = "공동구매 평점 추가",
            description = "완료된 공동구매에 대해 평점을 등록합니다. 평점은 0.0 ~ 5.0 사이의 값으로 입력해야 합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "평점 등록 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 평점 값입니다. (0.0 ~ 5.0 사이의 값을 입력해주세요)"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 또는 유저입니다."),
                    @ApiResponse(responseCode = "409", description = "이미 평점을 등록한 공동구매입니다.")
            }
    )
    ResponseEntity<Void> addRating(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupOrderId,
            @PathVariable Float ratingScore
    );
}
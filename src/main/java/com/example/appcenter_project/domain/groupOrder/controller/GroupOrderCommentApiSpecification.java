package com.example.appcenter_project.domain.groupOrder.controller;

import com.example.appcenter_project.domain.groupOrder.dto.request.RequestGroupOrderCommentDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "GroupOrderComment", description = "공동구매 댓글 관련 API")
public interface GroupOrderCommentApiSpecification {

    @Operation(
            summary = "공동구매 댓글 등록",
            description = "토큰과 댓글 등록정보를 통해 공동구매 댓글을 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "공동구매 댓글 등록 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderCommentDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "입력이 잘못되었습니다.",
                            content = @Content(examples = {})
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "유효하지 않은 토큰입니다.",
                            content = @Content(examples = {})
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 공동구매 글을 찾을 수 없습니다. (GROUP_ORDER_NOT_FOUND)
                            - 부모 댓글을 찾을 수 없습니다. (GROUP_ORDER_COMMENT_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 존재하는 리소스입니다. (예: 제목 중복 등)",
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<ResponseGroupOrderCommentDto> saveGroupOrderComment(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestGroupOrderCommentDto requestGroupOrderCommentDto);


    @Operation(
            summary = "공동구매 댓글 삭제",
            description = "토큰과 댓글 아이디를 통해 공동구매 댓글을 삭제합니다. 실제로 삭제되는 것이 아니라 '삭제된 메시지입니다'라고 변환해서 리턴합니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "공동구매 댓글 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글 아이디입니다."),
                    @ApiResponse(responseCode = "403", description = "댓글 삭제 권한이 없습니다.")
            }
    )
    public ResponseEntity<Void> deleteGroupOrderComment(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderCommentId);

}

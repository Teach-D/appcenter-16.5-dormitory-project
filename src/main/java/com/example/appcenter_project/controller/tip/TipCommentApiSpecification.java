package com.example.appcenter_project.controller.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "TipComment", description = "팁 댓글 관리 API")
public interface TipCommentApiSpecification {

    @Operation(
            summary = "팁 댓글 작성",
            description = "팁 게시글에 댓글을 작성하거나 기존 댓글에 대댓글을 작성할 수 있습니다. parentCommentId가 null이면 일반 댓글, 값이 있으면 대댓글로 작성됩니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseTipCommentDto.class))),
                    @ApiResponse(responseCode = "400", description = "입력이 잘못되었습니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 존재하지 않는 사용자입니다. (USER_NOT_FOUND)
                            - 존재하지 않는 팁 게시글입니다. (TIP_NOT_FOUND)
                            - 존재하지 않는 부모 댓글입니다. (TIP_COMMENT_NOT_FOUND)
                            """
                            , content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<ResponseTipCommentDto> saveTipComment(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody
            @Parameter(description = "댓글 작성 정보", required = true) RequestTipCommentDto requestTipCommentDto);

    @Operation(
            summary = "팁 댓글 삭제",
            description = "작성자만 자신의 팁 댓글을 삭제할 수 있습니다. 실제로는 soft delete로 처리됩니다.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰이거나 삭제 권한이 없습니다(TIP_COMMENT_NOT_OWNED_BY_USER)."),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 댓글이거나 삭제 권한이 없습니다.")
            }
    )
    public ResponseEntity<Void> deleteTipComment(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable
            @Parameter(description = "삭제할 댓글 ID", required = true, example = "1") Long tipCommentId);
}

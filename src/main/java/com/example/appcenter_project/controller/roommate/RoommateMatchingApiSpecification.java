package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestMatchingDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "RoommateMatching", description = "룸메이트 매칭 관련 API")
public interface RoommateMatchingApiSpecification {

    @Operation(
            summary = "룸메이트 매칭 요청",
            description = "상대방의 학번을 통해 룸메이트 매칭을 요청합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "매칭 요청 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (ROOMMATE_USER_NOT_FOUND)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 매칭 요청을 보낸 상태입니다. (ROOMMATE_MATCHING_ALREADY_REQUESTED)", content = @Content)
            }
    )
    ResponseEntity<?> requestMatching(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "받는 사람의 학번 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestMatchingDto.class))
            ) RequestMatchingDto requestDto
    );

    @Operation(
            summary = "룸메이트 매칭 수락",
            description = "룸메이트 매칭 요청을 수락합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 수락 성공"),
                    @ApiResponse(responseCode = "404", description = "매칭 요청을 찾을 수 없습니다. (ROOMMATE_MATCHING_NOT_FOUND)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 처리된 매칭입니다. (ROOMMATE_MATCHING_ALREADY_COMPLETED)", content = @Content)
            }
    )
    ResponseEntity<Void> acceptMatching(
            @Parameter(description = "매칭 ID", example = "1") @PathVariable Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "룸메이트 매칭 거절",
            description = "룸메이트 매칭 요청을 거절합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 거절 성공"),
                    @ApiResponse(responseCode = "404", description = "매칭 요청을 찾을 수 없습니다. (ROOMMATE_MATCHING_NOT_FOUND)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 처리된 매칭입니다. (ROOMMATE_MATCHING_ALREADY_COMPLETED)", content = @Content)
            }
    )
    ResponseEntity<Void> rejectMatching(
            @Parameter(description = "매칭 ID", example = "1") @PathVariable Long matchingId
    );


}

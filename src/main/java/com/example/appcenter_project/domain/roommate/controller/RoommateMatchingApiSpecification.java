package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.dto.request.RequestMatchingByChatRoomDto;
import com.example.appcenter_project.domain.roommate.dto.request.RequestMatchingDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseReceivedRoommateMatchingDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateMatchingDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "RoommateMatching", description = "룸메이트 매칭 관련 API")
public interface RoommateMatchingApiSpecification {

    @Operation(
            summary = "룸메이트 매칭 요청 (학번)",
            description = "상대방의 학번을 통해 룸메이트 매칭을 요청합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "매칭 요청 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다. (ROOMMATE_USER_NOT_FOUND)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 매칭 요청을 보낸 상태입니다. (ROOMMATE_MATCHING_ALREADY_REQUESTED)", content = @Content)
            }
    )
    ResponseEntity<ResponseRoommateMatchingDto> requestMatching(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "받는 사람의 학번 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestMatchingDto.class))
            ) RequestMatchingDto requestDto
    );

    @Operation(
            summary = "룸메이트 매칭 요청 (채팅방 ID)",
            description = "채팅방 ID를 통해 룸메이트 매칭을 요청합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "매칭 요청 성공"),
                    @ApiResponse(responseCode = "404", description = "채팅방 또는 사용자를 찾을 수 없습니다. (ROOMMATE_CHAT_ROOM_NOT_FOUND, ROOMMATE_USER_NOT_FOUND)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 매칭 요청을 보낸 상태입니다. (ROOMMATE_MATCHING_ALREADY_REQUESTED)", content = @Content)
            }
    )
    ResponseEntity<ResponseRoommateMatchingDto> requestMatchingByChatRoom(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "채팅방 ID 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RequestMatchingByChatRoomDto.class))
            ) RequestMatchingByChatRoomDto requestDto
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
                    @ApiResponse(responseCode = "403", description = "본인이 받은 매칭 요청이 아닙니다. (ROOMMATE_MATCHING_NOT_FOR_USER)", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 처리된 매칭입니다. (ROOMMATE_MATCHING_ALREADY_COMPLETED)", content = @Content)
            }
    )
    ResponseEntity<Void> rejectMatching(
            @Parameter(description = "매칭 ID", example = "1") @PathVariable Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "나에게 온 룸메이트 매칭 요청 리스트 조회",
            description = "나에게 요청된 모든 룸메이트 매칭 요청(수락 대기 상태) 리스트를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "매칭 요청 리스트 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseReceivedRoommateMatchingDto.class) // ← 여기!
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없습니다. (ROOMMATE_USER_NOT_FOUND)",
                            content = @Content
                    )
            }
    )
    ResponseEntity<?> getReceivedMatchings(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

    @Operation(
            summary = "룸메이트 매칭 취소",
            description = "내가 요청한 룸메이트 매칭을 취소(삭제)합니다. (매칭 완료 상태일 때만 가능)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 취소 성공"),
                    @ApiResponse(responseCode = "404", description = "매칭 요청을 찾을 수 없습니다. (ROOMMATE_MATCHING_NOT_FOUND)", content = @Content),
                    @ApiResponse(responseCode = "400", description = "매칭이 완료된 상태가 아닙니다. (ROOMMATE_MATCHING_NOT_COMPLETED)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "본인이 요청한 매칭이 아닙니다. (ROOMMATE_MATCHING_NOT_FOR_USER)", content = @Content)
            }
    )
    ResponseEntity<Void> cancelMatching(
            @Parameter(description = "매칭 ID", example = "1") @PathVariable Long matchingId,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    );

}

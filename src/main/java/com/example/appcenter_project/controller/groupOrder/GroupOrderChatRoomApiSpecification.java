package com.example.appcenter_project.controller.groupOrder;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Tag(name = "GroupOrderChatRoom", description = "공동구매 채팅방 관련 API")
public interface GroupOrderChatRoomApiSpecification {

    @Operation(
            summary = "공동구매 채팅방 참가",
            description = "해당 공동구매 ID에 해당하는 채팅방에 로그인한 사용자가 참가합니다.",
            parameters = {
                    @Parameter(name = "groupOrderId", description = "공동구매 ID", example = "1")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "채팅방 참가 완료"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "404", description = "해당 공동구매가 존재하지 않음")
            }
    )
    @PostMapping("/group-order/{groupOrderId}")
    ResponseEntity<Void> joinChatRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long groupOrderId
    );

    @Operation(
            summary = "공동구매 채팅방 목록 조회",
            description = "현재 로그인한 사용자가 참여 중인 모든 공동구매 채팅방 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 목록 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = ResponseGroupOrderChatRoomDto.class)))
                    ),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )
    @GetMapping
    ResponseEntity<List<ResponseGroupOrderChatRoomDto>> findGroupOrderChatRoomList(
            @AuthenticationPrincipal CustomUserDetails user
    );

    @Operation(
            summary = "채팅방 상세 조회 (채팅방 ID 기준)",
            description = "채팅방 ID를 기준으로 채팅방 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "groupOrderChatRoomId", description = "채팅방 ID", example = "10")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 상세 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseGroupOrderChatRoomDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 채팅방이 존재하지 않음")
            }
    )
    @GetMapping("/{groupOrderChatRoomId}")
    ResponseEntity<ResponseGroupOrderChatRoomDetailDto> findGroupOrderChatRoom(
            @PathVariable Long groupOrderChatRoomId
    );

    @Operation(
            summary = "공동구매 기준 채팅방 조회",
            description = "공동구매 ID를 기준으로 해당 공동구매에 대한 채팅방 상세 정보를 조회합니다.",
            parameters = {
                    @Parameter(name = "groupOrderId", description = "공동구매 ID", example = "1")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseGroupOrderChatRoomDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "해당 공동구매가 존재하지 않음")
            }
    )
    @GetMapping("/group-order/{groupOrderId}")
    ResponseEntity<ResponseGroupOrderChatRoomDetailDto> findGroupOrderChatRoomByGroupOrder(
            @PathVariable Long groupOrderId
    );

    @Operation(
            summary = "공동구매 채팅방 나가기",
            description = "현재 로그인한 사용자가 특정 공동구매 채팅방에서 나갑니다.",
            parameters = {
                    @Parameter(name = "chatRoomId", description = "채팅방 ID", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "채팅방 나가기 완료"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "404", description = "해당 채팅방이 존재하지 않음")
            }
    )
    @PatchMapping("/{chatRoomId}")
    ResponseEntity<Void> leaveChatRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long chatRoomId
    );
}

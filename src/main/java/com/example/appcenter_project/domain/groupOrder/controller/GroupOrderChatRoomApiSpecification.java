package com.example.appcenter_project.domain.groupOrder.controller;

import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatRoomDetailDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "GroupOrderChatRoom", description = "공동구매 채팅방 관련 API")
public interface GroupOrderChatRoomApiSpecification {

    @Operation(
            summary = "공동구매 채팅방 참여",
            description = "공동구매 아이디를 통해 해당 공동구매의 채팅방에 참여합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "채팅방 참여 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 존재하지 않는 공동구매 아이디입니다. (GROUP_ORDER_NOT_FOUND)
                            """
                    ),
            }
    )
    public ResponseEntity<Void> joinChatRoom(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long groupOrderId);


    @Operation(
            summary = "참여중인 공동구매 채팅방 목록 조회",
            description = "사용자가 참여중인 공동구매 채팅방 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderChatRoomDto.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다.", content = @Content(examples = {})),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없습니다.", content = @Content(examples = {}))
            }
    )
    public ResponseEntity<List<ResponseGroupOrderChatRoomDto>> findGroupOrderChatRoomList(@AuthenticationPrincipal CustomUserDetails user);

    @Operation(
            summary = "공동구매 채팅방 상세 조회",
            description = "채팅방 아이디를 통해 공동구매 채팅방의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 상세 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderChatRoomDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 존재하지 않는 공동구매 채팅방 아이디입니다. (GROUP_ORDER_CHAT_ROOM_NOT_FOUND)
                            """,
                            content = @Content(examples = {})
                    )
            }
    )
    public ResponseEntity<ResponseGroupOrderChatRoomDetailDto> findGroupOrderChatRoom(@PathVariable Long groupOrderChatRoomId);

    @Operation(
            summary = "공동구매 아이디로 채팅방 조회",
            description = "공동구매 아이디를 통해 해당 공동구매의 채팅방 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "채팅방 조회 성공",
                            content = @Content(schema = @Schema(implementation = ResponseGroupOrderChatRoomDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "404", description = "존재하지 않는 공동구매 아이디입니다.", content = @Content(examples = {}))
            }
    )
    public ResponseEntity<ResponseGroupOrderChatRoomDetailDto> findGroupOrderChatRoomByGroupOrder(@PathVariable Long groupOrderId);

    @Operation(
            summary = "공동구매 채팅방 퇴장",
            description = "채팅방 아이디를 통해 해당 채팅방에서 퇴장합니다.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "채팅방 퇴장 성공"),
                    @ApiResponse(responseCode = "403", description = "유효하지 않은 토큰입니다."),
                    @ApiResponse(responseCode = "404",
                            description = """
                            다음 중 하나일 수 있습니다:
                            - 사용자를 찾을 수 없습니다. (USER_NOT_FOUND)
                            - 존재하지 않는 공동구매 채팅방 아이디입니다. (GROUP_ORDER_CHAT_ROOM_NOT_FOUND)
                            - 사용자의 공동구매 채팅방 참여 정보를 찾을 수 없습니다. (USER_GROUP_ORDER_CHAT_ROOM_NOT_FOUND)
                            """
                    ),
            }
    )
    public ResponseEntity<Void> leaveChatRoom(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long chatRoomId);
}

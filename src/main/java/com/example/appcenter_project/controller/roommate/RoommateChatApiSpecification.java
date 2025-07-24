package com.example.appcenter_project.controller.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateChatDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateChatDto;
import com.example.appcenter_project.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "RoommateChat", description = "룸메이트 채팅 관련 API")
public interface RoommateChatApiSpecification  {

    @Operation(
            summary = "채팅 메시지 전송",
            description = "해당 채팅방에 메시지를 전송합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "메시지 전송 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseRoommateChatDto.class))),
                    @ApiResponse(responseCode = "404", description = "유저 또는 채팅방 없음 (USER_NOT_FOUND, ROOMMATE_CHAT_ROOM_NOT_FOUND)",
                            content = @Content()),
                    @ApiResponse(responseCode = "403", description = "채팅방 권한 없음 (ROOMMATE_CHAT_ROOM_FORBIDDEN)",
                            content = @Content())
            }
    )
    ResponseEntity<ResponseRoommateChatDto> sendChat(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @RequestBody @Parameter(description = "채팅 메시지 요청 DTO", required = true)
            RequestRoommateChatDto request
    );

    @Operation(
            summary = "채팅 내역 조회",
            description = "채팅방에 입장 시, 해당 방의 전체 채팅 내역을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "채팅 내역 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponseRoommateChatDto.class))),
                    @ApiResponse(responseCode = "404", description = "유저 또는 채팅방 없음 (USER_NOT_FOUND, ROOMMATE_CHAT_ROOM_NOT_FOUND)",
                            content = @Content()),
                    @ApiResponse(responseCode = "403", description = "채팅방 권한 없음 (ROOMMATE_CHAT_ROOM_FORBIDDEN)",
                            content = @Content())
            }
    )
    ResponseEntity<List<ResponseRoommateChatDto>> getChatList(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "조회할 채팅방 ID", example = "1") @PathVariable Long roomId
    );

    @Operation(
            summary = "채팅 읽음 처리",
            description = "해당 채팅방의 안 읽은 메시지를 읽음 처리합니다. (백업용)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
                    @ApiResponse(responseCode = "404", description = "유저 또는 채팅방 없음 (USER_NOT_FOUND, ROOMMATE_CHAT_ROOM_NOT_FOUND)",
                            content = @Content())
            }
    )
    ResponseEntity<Void> markAsRead(
            @Parameter(hidden = true) CustomUserDetails userDetails,
            @Parameter(description = "읽음 처리할 채팅방 ID", example = "1") @PathVariable Long roomId
    );

    @Operation(
            summary = "[WebSocket] 채팅 메시지 전송",
            description = """
                    WebSocket을 통해 채팅 메시지를 전송합니다.  
                    STOMP 엔드포인트: `/pub/roommate/chat`  
                    메시지 형식:
                    ```json
                    {
                      "roommateChattingRoomId": 1,
                      "content": "안녕하세요"
                    }
                    ```
                    """
    )
    default void sendChatViaWebSocket() {
        // 설명용으로 Swagger UI에만 노출되며 실제 구현은 컨트롤러에 있음.
    }
}

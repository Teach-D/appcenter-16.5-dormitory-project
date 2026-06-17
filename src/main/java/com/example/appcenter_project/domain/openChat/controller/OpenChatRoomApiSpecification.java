package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseLeaveOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDetailDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomTab;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import java.util.Map;

@Tag(name = "OpenChat", description = "오픈 채팅방 API")
public interface OpenChatRoomApiSpecification {

    @Operation(summary = "채팅방 생성", description = "새 오픈 채팅방을 생성하고 생성자를 방장으로 등록한다")
    ResponseEntity<Map<String, Long>> createRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid RequestCreateOpenChatRoomDto request);

    @Operation(summary = "채팅방 목록 조회", description = "탭 파라미터에 따라 MY/DORMITORY/ALL 목록 조회")
    ResponseEntity<Page<ResponseOpenChatRoomDto>> getRooms(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam OpenChatRoomTab tab,
            Pageable pageable);

    @Operation(summary = "채팅방 입장", description = "지정한 채팅방에 입장한다")
    ResponseEntity<ResponseOpenChatRoomDetailDto> joinRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId);

    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나간다")
    ResponseEntity<ResponseLeaveOpenChatRoomDto> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId);

    @Operation(summary = "알림 설정 변경", description = "해당 채팅방의 알림 수신 여부를 변경한다 (enabled=true/false)")
    ResponseEntity<Void> updateNotification(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam boolean enabled);

    @Operation(summary = "채팅방 삭제", description = "채팅방을 강제 삭제한다")
    ResponseEntity<Void> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId);

    @Operation(summary = "참여자 강제퇴장", description = "방장 또는 ADMIN이 특정 참여자를 강제퇴장시킨다")
    ResponseEntity<Void> kickParticipant(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId);
}

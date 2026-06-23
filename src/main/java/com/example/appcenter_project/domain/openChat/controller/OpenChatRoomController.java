package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseLeaveOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatParticipantListDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDetailDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomTab;
import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-chat-rooms")
public class OpenChatRoomController implements OpenChatRoomApiSpecification {

    private final OpenChatRoomService openChatRoomService;

    @PostMapping
    public ResponseEntity<Map<String, Long>> createRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid RequestCreateOpenChatRoomDto request) {
        Long roomId = openChatRoomService.createRoom(request, user.getId());
        return ResponseEntity.status(CREATED).body(Map.of("roomId", roomId));
    }

    @GetMapping
    public ResponseEntity<Page<ResponseOpenChatRoomDto>> getRooms(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam OpenChatRoomTab tab,
            Pageable pageable) {
        Page<ResponseOpenChatRoomDto> result;
        if (tab == OpenChatRoomTab.DORMITORY) {
            String dormType = getDormType(user);
            result = openChatRoomService.getRoomsForDormitory(user.getId(), dormType, pageable);
        } else {
            result = openChatRoomService.getRooms(user.getId(), tab, pageable);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{roomId}/participants/me")
    public ResponseEntity<ResponseOpenChatRoomDetailDto> joinRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam(required = false) String password) {
        ResponseOpenChatRoomDetailDto result = openChatRoomService.joinRoom(user.getId(), roomId, password);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{roomId}/participants/me")
    public ResponseEntity<ResponseLeaveOpenChatRoomDto> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long newHostUserId) {
        ResponseLeaveOpenChatRoomDto result = openChatRoomService.leaveRoom(roomId, user.getId(), newHostUserId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{roomId}/participants/me/notification")
    public ResponseEntity<Void> updateNotification(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam boolean enabled) {
        openChatRoomService.updateNotification(user.getId(), roomId, enabled);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roomId}/participants/{targetUserId}")
    public ResponseEntity<Void> kickParticipant(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId) {
        openChatRoomService.kickParticipant(user.getId(), roomId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId) {
        openChatRoomService.deleteRoom(roomId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roomId}/hosts/{targetUserId}")
    public ResponseEntity<Void> grantHost(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId) {
        openChatRoomService.grantHost(roomId, user.getId(), targetUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roomId}/hosts/{targetUserId}")
    public ResponseEntity<Void> revokeHostByAdmin(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId) {
        openChatRoomService.revokeHostByAdmin(roomId, user.getId(), targetUserId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{roomId}/hosts/me")
    public ResponseEntity<Void> transferHost(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestParam Long targetUserId) {
        openChatRoomService.transferHost(roomId, user.getId(), targetUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{roomId}/participants")
    public ResponseEntity<ResponseOpenChatParticipantListDto> getParticipants(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId) {
        ResponseOpenChatParticipantListDto result = openChatRoomService.getParticipants(roomId, user.getId());
        return ResponseEntity.ok(result);
    }

    private String getDormType(CustomUserDetails user) {
        User u = user.getUser();
        if (u == null || u.getDormType() == null) {
            return "NONE";
        }
        return u.getDormType().name();
    }
}

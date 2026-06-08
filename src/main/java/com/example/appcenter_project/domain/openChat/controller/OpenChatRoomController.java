package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseLeaveOpenChatRoomDto;
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
            @PathVariable Long roomId) {
        ResponseOpenChatRoomDetailDto result = openChatRoomService.joinRoom(user.getId(), roomId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{roomId}/participants/me")
    public ResponseEntity<ResponseLeaveOpenChatRoomDto> leaveRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId) {
        ResponseLeaveOpenChatRoomDto result = openChatRoomService.leaveRoom(user.getId(), roomId);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<Void> deleteRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId) {
        openChatRoomService.deleteRoom(user.getId(), roomId);
        return ResponseEntity.noContent().build();
    }

    private String getDormType(CustomUserDetails user) {
        User u = user.getUser();
        if (u == null || u.getDormType() == null) {
            return "NONE";
        }
        return u.getDormType().name();
    }
}

package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateDormOfficialRoomDto;
import com.example.appcenter_project.domain.openChat.service.OpenChatDormOfficialRoomService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/open-chat-rooms")
@RequiredArgsConstructor
public class OpenChatRoomAdminController {

    private final OpenChatDormOfficialRoomService openChatDormOfficialRoomService;

    @PostMapping("/dorm")
    public ResponseEntity<Map<String, Long>> createDormOfficialRoom(
            @AuthenticationPrincipal(errorOnInvalidType = false) CustomUserDetails userDetails,
            @RequestBody @Valid RequestCreateDormOfficialRoomDto request
    ) {
        Long adminId = userDetails != null ? userDetails.getId() : 0L;
        Long roomId = openChatDormOfficialRoomService.createDormOfficialRoom(adminId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("roomId", roomId));
    }
}

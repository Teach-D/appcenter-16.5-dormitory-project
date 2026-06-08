package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateDerivedRoomDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseDerivedRoomCreatedDto;
import com.example.appcenter_project.domain.openChat.service.OpenChatInvitationService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-chat-rooms")
public class OpenChatDerivedRoomController {

    private final OpenChatInvitationService openChatInvitationService;

    @PostMapping("/derived")
    public ResponseEntity<ResponseDerivedRoomCreatedDto> createDerivedRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid RequestCreateDerivedRoomDto request) {
        ResponseDerivedRoomCreatedDto result = openChatInvitationService.createDerivedRoom(user.getId(), request);
        return ResponseEntity.status(CREATED).body(result);
    }
}

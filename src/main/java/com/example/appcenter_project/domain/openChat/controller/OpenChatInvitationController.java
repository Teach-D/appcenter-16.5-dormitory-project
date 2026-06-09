package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.request.RequestSendInvitationDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseInvitationAcceptDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseInvitationCreatedDto;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatParticipantListDto;
import com.example.appcenter_project.domain.openChat.service.OpenChatInvitationService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-chat-rooms")
public class OpenChatInvitationController {

    private final OpenChatInvitationService openChatInvitationService;

    @PostMapping("/{roomId}/invitations")
    public ResponseEntity<ResponseInvitationCreatedDto> sendInvitation(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @RequestBody @Valid RequestSendInvitationDto request) {
        ResponseInvitationCreatedDto result = openChatInvitationService.sendInvitation(user.getId(), roomId, request);
        return ResponseEntity.status(CREATED).body(result);
    }

    @PostMapping("/{roomId}/invitations/{invitationId}/accept")
    public ResponseEntity<ResponseInvitationAcceptDto> acceptInvitation(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @PathVariable Long invitationId) {
        ResponseInvitationAcceptDto result = openChatInvitationService.acceptInvitation(user.getId(), roomId, invitationId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{roomId}/invitations/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId,
            @PathVariable Long invitationId) {
        openChatInvitationService.rejectInvitation(user.getId(), roomId, invitationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{roomId}/participants")
    public ResponseEntity<ResponseOpenChatParticipantListDto> getParticipants(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long roomId) {
        ResponseOpenChatParticipantListDto result = openChatInvitationService.getParticipants(user.getId(), roomId);
        return ResponseEntity.ok(result);
    }
}

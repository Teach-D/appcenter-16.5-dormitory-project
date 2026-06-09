package com.example.appcenter_project.domain.studentIdDisclosure.controller;

import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureSendDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureStatusDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "StudentIdDisclosure", description = "학번 공개 요청 API")
public interface StudentIdDisclosureApiSpecification {

    @Operation(summary = "학번 공개 요청 발송", description = "같은 오픈채팅방 참여자에게 학번 공개를 요청합니다.")
    @PostMapping
    ResponseEntity<ResponseDisclosureSendDto> sendRequest(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid RequestCreateDisclosureDto request);

    @Operation(summary = "학번 공개 요청 취소", description = "내가 보낸 학번 공개 요청을 취소합니다.")
    @DeleteMapping("/{requestId}")
    ResponseEntity<Void> cancel(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long requestId);

    @Operation(summary = "학번 공개 요청 수락", description = "받은 학번 공개 요청을 수락합니다.")
    @PostMapping("/{requestId}/accept")
    ResponseEntity<ResponseDisclosureAcceptDto> accept(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long requestId);

    @Operation(summary = "학번 공개 요청 거절", description = "받은 학번 공개 요청을 거절합니다.")
    @PostMapping("/{requestId}/reject")
    ResponseEntity<Void> reject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long requestId);

    @Operation(summary = "학번 공개 상태 조회", description = "특정 사용자와의 학번 공개 상태를 조회합니다.")
    @GetMapping("/status")
    ResponseEntity<ResponseDisclosureStatusDto> getStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long roomId,
            @RequestParam Long targetId);
}

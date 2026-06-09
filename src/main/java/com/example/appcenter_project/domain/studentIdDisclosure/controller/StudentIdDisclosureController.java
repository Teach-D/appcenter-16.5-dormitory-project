package com.example.appcenter_project.domain.studentIdDisclosure.controller;

import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureSendDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureStatusDto;
import com.example.appcenter_project.domain.studentIdDisclosure.service.StudentIdDisclosureRequestService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/student-id-disclosures")
public class StudentIdDisclosureController implements StudentIdDisclosureApiSpecification {

    private final StudentIdDisclosureRequestService disclosureService;

    @PostMapping
    public ResponseEntity<ResponseDisclosureSendDto> sendRequest(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid RequestCreateDisclosureDto request) {
        ResponseDisclosureSendDto result = disclosureService.sendRequest(user.getId(), request);
        return ResponseEntity.status(CREATED).body(result);
    }

    @DeleteMapping("/{requestId}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long requestId) {
        disclosureService.cancel(user.getId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<ResponseDisclosureAcceptDto> accept(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long requestId) {
        ResponseDisclosureAcceptDto result = disclosureService.accept(user.getId(), requestId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long requestId) {
        disclosureService.reject(user.getId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseDisclosureStatusDto> getStatus(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam Long roomId,
            @RequestParam Long targetId) {
        ResponseDisclosureStatusDto result = disclosureService.getStatus(user.getId(), roomId, targetId);
        return ResponseEntity.ok(result);
    }
}

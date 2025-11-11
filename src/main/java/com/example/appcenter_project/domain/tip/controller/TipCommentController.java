package com.example.appcenter_project.domain.tip.controller;

import com.example.appcenter_project.domain.tip.dto.request.RequestTipCommentDto;
import com.example.appcenter_project.domain.tip.dto.response.ResponseTipCommentDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.tip.service.TipCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tip-comments")
public class TipCommentController implements TipCommentApiSpecification {

    private final TipCommentService tipCommentService;

    @PostMapping
    public ResponseEntity<ResponseTipCommentDto> saveTipComment(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestTipCommentDto requestTipCommentDto) {
        return ResponseEntity.status(CREATED).body(tipCommentService.saveTipComment(user.getId(), requestTipCommentDto));
    }

    @DeleteMapping("/{tipCommentId}")
    public ResponseEntity<Void> deleteTipComment(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipCommentId) {
        tipCommentService.deleteTipComment(user.getId(), tipCommentId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}

package com.example.appcenter_project.controller.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.tip.TipCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tip-comments")
public class TipCommentController {

    private final TipCommentService tipCommentService;

    @PostMapping
    public ResponseEntity<ResponseTipCommentDto> saveTipComment(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestBody RequestTipCommentDto requestTipCommentDto) {
        return ResponseEntity.status(CREATED).body(tipCommentService.saveTipComment(user.getId(), requestTipCommentDto));
    }

}

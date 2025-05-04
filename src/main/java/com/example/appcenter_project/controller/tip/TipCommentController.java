package com.example.appcenter_project.controller.tip;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderCommentDto;
import com.example.appcenter_project.dto.request.tip.RequestTipCommentDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderCommentDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipCommentDto;
import com.example.appcenter_project.jwt.SecurityUser;
import com.example.appcenter_project.service.tip.TipCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tip-comments")
public class TipCommentController {

    private final TipCommentService tipCommentService;

    @PostMapping
    public ResponseEntity<ResponseTipCommentDto> saveTipComment(@AuthenticationPrincipal SecurityUser user, @RequestBody RequestTipCommentDto requestTipCommentDto) {
        return ResponseEntity.status(CREATED).body(tipCommentService.saveTipComment(user.getId(), requestTipCommentDto));
    }

}

package com.example.appcenter_project.domain.tip.controller;

import com.example.appcenter_project.common.image.dto.ImageLinkDto;
import com.example.appcenter_project.domain.tip.dto.request.RequestTipDto;
import com.example.appcenter_project.domain.tip.dto.response.ResponseTipDetailDto;
import com.example.appcenter_project.domain.tip.dto.response.ResponseTipDto;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.tip.service.TipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/tips")
public class TipController implements TipApiSpecification {

    private final TipService tipService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestTipDto") RequestTipDto requestTipDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        tipService.saveTip(user.getId(), requestTipDto, images);
        return ResponseEntity.status(CREATED).build();

    }

    @GetMapping("/{tipId}")
    public ResponseEntity<ResponseTipDetailDto> findTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId, HttpServletRequest request) {
        return ResponseEntity.status(OK).body(tipService.findTip(user, tipId, request));
    }

    @GetMapping
    public ResponseEntity<List<ResponseTipDto>> findAllTips() {
        return ResponseEntity.status(OK).body(tipService.findAllTips());
    }

    @GetMapping("/daily-random")
    public ResponseEntity<List<ResponseTipDto>> findDailyRandomTips() {
        List<ResponseTipDto> dailyRandomTips = tipService.findDailyRandomTips();
        if (dailyRandomTips.isEmpty()) {
            return ResponseEntity.status(NO_CONTENT).build();
        }
        return ResponseEntity.status(OK).body(dailyRandomTips);
    }

    @GetMapping("/{tipId}/image")
    public ResponseEntity<List<ImageLinkDto>> findTipImages(
            @PathVariable Long tipId,
            HttpServletRequest request) {
        try {
            List<ImageLinkDto> imageLinkDtos = tipService.findTipImages(tipId, request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageLinkDtos);
        } catch (Exception e) {
            log.error("이미지 조회가 실패했습니다. ", e);
            throw e;
        }
    }

    @PatchMapping("/{tipId}/like")
    public ResponseEntity<Integer> likeTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.likeTip(user.getId(), tipId));
    }

    @PatchMapping("/{tipId}/unlike")
    public ResponseEntity<Integer> unlikeTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.unlikeTip(user.getId(), tipId));
    }

    @PutMapping(value = "/{tipId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateTip(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestPart RequestTipDto requestTipDto, @RequestPart(value = "images", required = false) List<MultipartFile> images, @PathVariable Long tipId) {
        tipService.updateTip(user.getId(), requestTipDto, images, tipId);
        return ResponseEntity.status(ACCEPTED).build();
    }

    @DeleteMapping("/{tipId}")
    public ResponseEntity<Void> deleteTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        tipService.deleteTip(user.getId(), tipId);
        return ResponseEntity.status(NO_CONTENT).build();
    }

    @DeleteMapping("/{tipId}/image")
    public ResponseEntity<Void> deleteTipImages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long tipId) {
        tipService.deleteTipImages(user.getId(), tipId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
package com.example.appcenter_project.controller.tip;

import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.tip.TipImageDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.tip.TipService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    // 모든 Tip 조회
    @GetMapping
    public ResponseEntity<List<ResponseTipDto>> findAllTips() {
        return ResponseEntity.status(OK).body(tipService.findAllTips());
    }

    // 일일 랜덤 Tip 3개 조회
    @GetMapping("/daily-random")
    public ResponseEntity<List<ResponseTipDto>> findDailyRandomTips() {
        List<ResponseTipDto> dailyRandomTips = tipService.findDailyRandomTips();
        if (dailyRandomTips.isEmpty()) {
            return ResponseEntity.status(NO_CONTENT).build();
        }
        return ResponseEntity.status(OK).body(dailyRandomTips);
    }

    // 2. 특정 Tip의 이미지를 제외한 정보 하나 조회
    @GetMapping("/{tipId}")
    public ResponseEntity<ResponseTipDetailDto> findTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.findTip(user, tipId));
    }
/*    // 3. 특정 Tip의 이미지 메타 정보 목록 조회
    @GetMapping("/{tipId}/images")
    public ResponseEntity<List<TipImageDto>> getTipImages(@PathVariable Long tipId) {
        List<TipImageDto> images = tipService.findTipImages(tipId);
        return ResponseEntity.ok(images);
    }

    // 4. 실제 이미지 파일 응답
    @GetMapping("/images/view")
    public ResponseEntity<Resource> viewImage(@RequestParam String filename) {
        Resource resource = tipService.loadImageAsResource(filename);
        File file = new File(resource.getFilename());
        String contentType = tipService.getImageContentType(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }*/

    @PatchMapping("/{tipId}/like")
    public ResponseEntity<Integer> likePlusTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.likePlusTip(user.getId(), tipId));
    }

    @PatchMapping("/{tipId}/unlike")
    public ResponseEntity<Integer> unlikePlusTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.unlikePlusTip(user.getId(), tipId));
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

    // Tip 이미지 조회 (다중 이미지)
    @GetMapping("/{tipId}/image")
    public ResponseEntity<List<ImageLinkDto>> findTipImagesByTipId(
            @PathVariable Long tipId,
            HttpServletRequest request) {
        try {
            List<ImageLinkDto> imageLinkDtos = tipService.findTipImageUrlsByTipId(tipId, request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(imageLinkDtos);
        } catch (Exception e) {
            log.error("Error retrieving tip images: ", e);
            throw e;
        }
    }

    // Tip 이미지 삭제 (모든 이미지)
    @DeleteMapping("/{tipId}/image")
    public ResponseEntity<Void> deleteTipImages(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long tipId) {
        tipService.deleteTipImages(user.getId(), tipId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}

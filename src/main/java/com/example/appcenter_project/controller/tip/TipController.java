package com.example.appcenter_project.controller.tip;

import com.example.appcenter_project.dto.request.tip.RequestTipDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDetailDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.dto.response.tip.TipImageDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.tip.TipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
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
public class TipController {

    private final TipService tipService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveTip(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestTipDto") RequestTipDto requestTipDto,
            @RequestPart("images") List<MultipartFile> images) {
        tipService.saveTip(user.getId(), requestTipDto, images);
        return ResponseEntity.status(CREATED).build();

    }

    // 모든 Tip 조회
    @GetMapping
    public ResponseEntity<List<ResponseTipDto>> findAllTips() {
        return ResponseEntity.status(OK).body(tipService.findAllTips());
    }

    // 2. 특정 Tip의 이미지를 제외한 정보 하나 조회
    @GetMapping("/{tipId}")
    public ResponseEntity<ResponseTipDetailDto> findTip(@PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.findTip(tipId));
    }

    // 3. 특정 Tip의 이미지 메타 정보 목록 조회
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
    }

    @PatchMapping("/like/{tipId}")
    public ResponseEntity<Integer> likePlusTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        return ResponseEntity.status(OK).body(tipService.likePlusTip(user.getId(), tipId));
    }

    @PutMapping("/{tipId}")
    public ResponseEntity<Void> updateTip(@AuthenticationPrincipal CustomUserDetails user, @Valid @RequestPart RequestTipDto requestTipDto, @RequestPart List<MultipartFile> images, @PathVariable Long tipId) {
        tipService.updateTip(user.getId(), requestTipDto, images, tipId);
        return ResponseEntity.status(ACCEPTED).build();
    }

    @DeleteMapping("/{tipId}")
    public ResponseEntity<Void> deleteTip(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long tipId) {
        tipService.deleteTip(user.getId(), tipId);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}

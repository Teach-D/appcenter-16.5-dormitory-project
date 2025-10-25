package com.example.appcenter_project.domain.announcement.controller;

import com.example.appcenter_project.common.file.dto.AttachedFileDto;
import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDto;
import com.example.appcenter_project.domain.announcement.service.AnnouncementFileService;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.example.appcenter_project.domain.announcement.service.AnnouncementService;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/announcements")
public class AnnouncementController implements AnnouncementApiSpecification {

    private final AnnouncementService announcementService;
    private final AnnouncementFileService announcementFileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveAnnouncement(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestAnnouncementDto") RequestAnnouncementDto requestAnnouncementDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        announcementService.saveAnnouncement(user.getId(), requestAnnouncementDto, files);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping("/{announcementId}")
    public ResponseEntity<ResponseAnnouncementDetailDto> findAnnouncement(@PathVariable Long announcementId) {
        return ResponseEntity.status(OK).body(announcementService.findAnnouncement(announcementId));
    }

    @GetMapping("/{announcementId}/image")
    public ResponseEntity<List<AttachedFileDto>> findAnnouncementFile(
            @PathVariable Long announcementId,
            HttpServletRequest request) {
        try {
            List<AttachedFileDto> fileDtos = announcementFileService.findAttachedFileByAnnouncementId(announcementId, request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(fileDtos);
        } catch (Exception e) {
            log.error("Error retrieving announcement files: ", e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<ResponseAnnouncementDto>> findAllAnnouncements() {
        return ResponseEntity.status(OK).body(announcementService.findAllAnnouncements());
    }

    @PutMapping("/{announcementId}")
    public ResponseEntity<ResponseAnnouncementDto> updateAnnouncement(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody RequestAnnouncementDto requestAnnouncementDto,
            @PathVariable Long announcementId) {
        return ResponseEntity.status(OK).body(announcementService.updateAnnouncement(user.getId(), requestAnnouncementDto, announcementId));
    }

    @PutMapping(value = "/{announcementId}/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseAnnouncementDto> updateAnnouncementWithFiles(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestAnnouncementDto") RequestAnnouncementDto requestAnnouncementDto,
            @PathVariable Long announcementId,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        ResponseAnnouncementDto response = announcementService.updateAnnouncementWithFiles(
                user.getId(), requestAnnouncementDto, announcementId, files);

        return ResponseEntity.status(OK).body(response);
    }

    @DeleteMapping("/{announcementId}/file")
    public ResponseEntity<Void> deleteAttachedFile(
            @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable Long announcementId,
            @RequestParam String filePath) {
        announcementService.deleteAttachedFile(user.getId(), announcementId, filePath);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{announcementId}")
    public void deleteAnnouncement(@AuthenticationPrincipal CustomUserDetails user, @PathVariable Long announcementId) {
        announcementService.deleteAnnouncement(user.getId(), announcementId);
    }
}

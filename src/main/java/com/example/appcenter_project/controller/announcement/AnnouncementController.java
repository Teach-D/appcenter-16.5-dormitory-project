package com.example.appcenter_project.controller.announcement;

import com.example.appcenter_project.dto.AttachedFileDto;
import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDto;
import com.example.appcenter_project.security.CustomUserDetails;
import com.example.appcenter_project.service.announcement.AnnouncementService;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<Void> saveAnnouncement(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestAnnouncementDto") RequestAnnouncementDto requestAnnouncementDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        announcementService.saveAnnouncement(requestAnnouncementDto, files);
        return ResponseEntity.status(CREATED).build();
    }

    @GetMapping
    @Override
    public ResponseEntity<List<ResponseAnnouncementDto>> findAllAnnouncements() {
        return ResponseEntity.status(OK).body(announcementService.findAllAnnouncements());
    }

    @GetMapping("/{announcementId}")
    @Override
    public ResponseEntity<ResponseAnnouncementDetailDto> findAnnouncement(@PathVariable Long announcementId) {
        return ResponseEntity.status(OK).body(announcementService.findAnnouncement(announcementId));
    }

    @GetMapping("/{announcementId}/image")
    @Override
    public ResponseEntity<List<AttachedFileDto>> findAnnouncementFile(
            @PathVariable Long announcementId,
            HttpServletRequest request) {
        try {
            List<AttachedFileDto> fileDtos = announcementService.findAttachedFileByAnnouncementId(announcementId, request);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(fileDtos);
        } catch (Exception e) {
            log.error("Error retrieving announcement files: ", e);
            throw e;
        }
    }

    @PutMapping("/{announcementId}")
    @Override
    public ResponseEntity<ResponseAnnouncementDto> updateAnnouncement(
            @Valid @RequestBody RequestAnnouncementDto requestAnnouncementDto,
            @PathVariable Long announcementId) {
        return ResponseEntity.status(OK).body(announcementService.updateAnnouncement(requestAnnouncementDto, announcementId));
    }

    @PutMapping(value = "/{announcementId}/with-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<ResponseAnnouncementDto> updateAnnouncementWithFiles(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestPart("requestAnnouncementDto") RequestAnnouncementDto requestAnnouncementDto,
            @PathVariable Long announcementId,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        ResponseAnnouncementDto response = announcementService.updateAnnouncementWithFiles(
                requestAnnouncementDto, announcementId, files);

        return ResponseEntity.status(OK).body(response);
    }

    @DeleteMapping("/{announcementId}/file")
    public ResponseEntity<Void> deleteAttachedFile(
            @PathVariable Long announcementId,
            @RequestParam String filePath) {
        announcementService.deleteAttachedFile(announcementId, filePath);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{announcementId}")
    @Override
    public void deleteAnnouncement(@PathVariable Long announcementId) {
        announcementService.deleteAnnouncement(announcementId);
    }
}

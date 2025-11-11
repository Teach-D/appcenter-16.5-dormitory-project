package com.example.appcenter_project.domain.announcement.service;

import com.example.appcenter_project.common.file.dto.AttachedFileDto;
import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.common.file.entity.CrawledAnnouncementFile;
import com.example.appcenter_project.common.file.entity.ManualAnnouncementFile;
import com.example.appcenter_project.common.file.repository.AttachedFileRepository;
import com.example.appcenter_project.common.file.repository.CrawledAnnouncementFileRepository;
import com.example.appcenter_project.common.file.repository.ManualAnnouncementFileRepository;
import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.repository.AnnouncementRepository;
import com.example.appcenter_project.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementFileService {

    private final AttachedFileRepository attachedFileRepository;
    private final CrawledAnnouncementFileRepository crawledAnnouncementFileRepository;
    private final ManualAnnouncementFileRepository manualAnnouncementFileRepository;
    private final AnnouncementRepository announcementRepository;

    public void saveUploadFile(ManualAnnouncement announcement, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // 개발 환경에 맞는 경로 설정
            String basePath = System.getProperty("user.dir");
            String filePath = basePath + "/files/manual-announcement/";

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(filePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
            }

            // 첨부파일 저장
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    log.warn("Empty file skipped during tip image save");
                    continue;
                }

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String uploadFileName = "manual-announcement_" + announcement.getId() + "_" + uuid + fileExtension;
                File destinationFile = new File(filePath + uploadFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("ManualAnnouncement file saved successfully: {}", destinationFile.getAbsolutePath());

                    ManualAnnouncementFile attachedFile = ManualAnnouncementFile.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .fileName(file.getOriginalFilename())
                            .fileSize(file.getSize())
                            .manualAnnouncement(announcement)
                            .build();

                    manualAnnouncementFileRepository.save(attachedFile);

                } catch (IOException e) {
                    log.error("Failed to save Announcement file for announcement {}: ", announcement.getId(), e);
                    throw new CustomException(IMAGE_NOT_FOUND);
                }
            }
        }
    }

    // 파일 확장자 추출 헬퍼 메소드
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return ".pdf"; // 기본 확장자
        }

        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return ".pdf"; // 확장자가 없으면 기본값
        }

        return fileName.substring(lastDotIndex).toLowerCase();
    }

    public List<AttachedFileDto> findAttachedFileByAnnouncementId(Long announcementId, HttpServletRequest request) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        if (announcement instanceof CrawledAnnouncement) {
            log.info("CrawledAnnouncement : {}", announcement.getId());
            List<CrawledAnnouncementFile> crawledAnnouncementFiles = crawledAnnouncementFileRepository.findByCrawledAnnouncementId(announcementId);

            List<AttachedFileDto> attachedFileDtos = new ArrayList<>();
            for (CrawledAnnouncementFile crawledAnnouncementFile : crawledAnnouncementFiles) {
                log.info("fileName : {}", crawledAnnouncementFile.getFileName());
                AttachedFileDto attachedFileDto = AttachedFileDto.builder()
                        .filePath(crawledAnnouncementFile.getFilePath())
                        .fileName(crawledAnnouncementFile.getFileName())
                        .fileSize(crawledAnnouncementFile.getFileSize())
                        .build();

                attachedFileDtos.add(attachedFileDto);
            }

            return attachedFileDtos;
        }

        List<AttachedFile> attachedFiles = manualAnnouncementFileRepository.findByManualAnnouncementId(announcement.getId());

        if (attachedFiles.isEmpty()) {
            log.info("No file found for announcement {}", announcementId);
            return new ArrayList<>(); // 빈 리스트 반환
        }

        // BaseURL 생성
        String baseUrl = getBaseUrl(request);
        List<AttachedFileDto> attachedFileDtos = new ArrayList<>();

        for (AttachedFile attachedFile : attachedFiles) {
            File file = new File(attachedFile.getFilePath());
            if (file.exists()) {
                String fileUrl = baseUrl + "/api/files/manual-announcement/" + attachedFile.getId();

                // 정적 리소스 URL 생성 (User와 동일한 방식)
                String staticImageUrl = getStaticAttachedFileUrl(attachedFile.getFilePath(), baseUrl);
                String changeUrl = staticImageUrl.replace("http", "https");

                AttachedFileDto attachedFileDto = AttachedFileDto.builder()
                        .filePath(changeUrl)
                        .fileName(attachedFile.getFileName())
                        .fileSize(attachedFile.getFileSize())
                        .build();

                attachedFileDtos.add(attachedFileDto);
            } else {
                log.warn("AttachedFile not found: {}", attachedFile.getFilePath());
            }
        }

        log.info("Found {} valid AttachedFile", attachedFileDtos.size(), announcementId);
        return attachedFileDtos;
    }

    // BaseURL 생성 헬퍼 메서드
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder baseUrl = new StringBuilder();
        baseUrl.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            baseUrl.append(":").append(serverPort);
        }
        baseUrl.append(contextPath);

        return baseUrl.toString();
    }

    // 정적 첨부파일 이미지 URL 생성 헬퍼 메소드
    private String getStaticAttachedFileUrl(String filePath, String baseUrl) {
        try {
            String fileName = Paths.get(filePath).getFileName().toString();
            return baseUrl + "/files/manual-announcement/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for Attached file path: {}", filePath);
            return null;
        }
    }

    public void deleteAttachedFileDetail(String filePath, Announcement announcement) {
        AttachedFile attachedFile = attachedFileRepository.findByFilePathAndAnnouncement(filePath, announcement).orElseThrow(() -> new CustomException(ATTACHEDFILE_NOT_REGISTERED));

        File file = new File(filePath);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("Failed to delete tip Attached file: {}", filePath);
            }
        } else {
            log.warn("Attached file not found: {}", filePath);
        }

        attachedFileRepository.delete(attachedFile);
        // announcement.getAttachedFiles().remove(attachedFile);
    }

    public void deleteExistingAttachedFiles(Announcement announcement) {
        log.info("[deleteExistingAttachedFiles] 기존 첨부파일 삭제 시작 - announcementId={}", announcement.getId());

        List<AttachedFile> existingFiles = manualAnnouncementFileRepository.findByManualAnnouncementId(announcement.getId());

        for (AttachedFile attachedFile : existingFiles) {
            // 파일시스템에서 파일 삭제
            File file = new File(attachedFile.getFilePath());
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    log.info("파일 삭제 성공: {}", attachedFile.getFilePath());
                } else {
                    log.warn("파일 삭제 실패: {}", attachedFile.getFilePath());
                }
            } else {
                log.warn("삭제할 파일이 존재하지 않음: {}", attachedFile.getFilePath());
            }
            attachedFileRepository.delete(attachedFile);
        }

        log.info("[deleteExistingAttachedFiles] 기존 첨부파일 삭제 완료 - 삭제된 파일 수: {}", existingFiles.size());
    }
}
package com.example.appcenter_project.service.announcement;

import com.example.appcenter_project.dto.AttachedFileDto;
import com.example.appcenter_project.dto.ImageLinkDto;
import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDto;
import com.example.appcenter_project.dto.response.tip.ResponseTipDto;
import com.example.appcenter_project.entity.Image;
import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.announcement.AttachedFile;
import com.example.appcenter_project.enums.image.ImageType;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.exception.ErrorCode;
import com.example.appcenter_project.repository.announcement.AnnouncementRepository;
import com.example.appcenter_project.repository.announcement.AttachedFileRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.example.appcenter_project.exception.ErrorCode.*;
import static com.example.appcenter_project.exception.ErrorCode.IMAGE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AttachedFileRepository attachedFileRepository;
    private final AnnouncementRepository announcementRepository;

    public void saveAnnouncement(RequestAnnouncementDto requestAnnouncementDto, List<MultipartFile> files) {

        // 공지사항 저장
        Announcement announcement = RequestAnnouncementDto.dtoToEntity(requestAnnouncementDto);
        announcementRepository.save(announcement);

        // 첨부파일 저장
        saveUploadFile(announcement, files);
    }

    private void saveUploadFile(Announcement announcement, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            // 개발 환경에 맞는 경로 설정
            String basePath = System.getProperty("user.dir");
            String filePath = basePath + "/files/announcement/";

            // 디렉토리 생성 (존재하지 않으면)
            File directory = new File(filePath);
            if (!directory.exists()) {
                boolean created = directory.mkdirs();
            }

            // 첨부파 저장
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) {
                    log.warn("Empty file skipped during tip image save");
                    continue;
                }

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uuid = UUID.randomUUID().toString();
                String uploadFileName = "announcement_" + announcement.getId() + "_" + uuid + timestamp + fileExtension;
                File destinationFile = new File(filePath + uploadFileName);

                try {
                    file.transferTo(destinationFile);
                    log.info("Announcement file saved successfully: {}", destinationFile.getAbsolutePath());

                    AttachedFile attachedFile = AttachedFile.builder()
                            .filePath(destinationFile.getAbsolutePath())
                            .announcement(announcement)
                            .build();

                    attachedFileRepository.save(attachedFile);

                    announcement.getAttachedFiles().add(attachedFile);

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

        List<AttachedFile> attachedFiles = attachedFileRepository.findByAnnouncement(announcement);

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
                String fileUrl = baseUrl + "/api/files/announcement/" + attachedFile.getId();

                // 정적 리소스 URL 생성 (User와 동일한 방식)
                String staticImageUrl = getStaticAttachedFileUrl(attachedFile.getFilePath(), baseUrl);
                String changeUrl = staticImageUrl.replace("http", "https");

                AttachedFileDto attachedFileDto = AttachedFileDto.builder()
                        .fileUrl(fileUrl)
                        .fileName(changeUrl)
                        .fileSize(file.length())
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
            return baseUrl + "/files/announcement/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for Attached file path: {}", filePath);
            return null;
        }
    }

    public void deleteAttachedFile(Long announcementId, String filePath) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
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
        announcement.getAttachedFiles().remove(attachedFile);
    }

    public List<ResponseAnnouncementDto> findAllAnnouncements() {
        log.info("[findAllAnnouncements] 공지사항 전체 조회 요청");

        List<Announcement> announcements = announcementRepository.findAll();
        Collections.reverse(announcements);

        List<ResponseAnnouncementDto> responseAnnouncementDtos = new ArrayList<>();

        for (Announcement announcement : announcements) {
            ResponseAnnouncementDto responseAnnouncementDto = ResponseAnnouncementDto.entityToDto(announcement);
            responseAnnouncementDtos.add(responseAnnouncementDto);
        }

        log.info("[findAllAnnouncements] 변환 완료 - 반환할 공지사항 수: {}", responseAnnouncementDtos.size());

        return responseAnnouncementDtos;
    }

    public ResponseAnnouncementDetailDto findAnnouncement(Long announcementId) {
        log.info("[findAnnouncement] 공지사항 상세 조회 요청 - announcementId={}", announcementId);

        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
        announcement.plusViewCount();
        log.debug("[findAnnouncement] 공지사항 조회수 증가 - currentViewCount={}", announcement.getViewCount());

        ResponseAnnouncementDetailDto dto = ResponseAnnouncementDetailDto.entityToDto(announcement);
        log.info("[findAnnouncement] 공지사항 조회 완료 - title=\"{}\", id={}", dto.getTitle(), dto.getId());

        return dto;
    }

    public void deleteAnnouncement(Long announcementId) {
        announcementRepository.deleteById(announcementId);
    }

    public ResponseAnnouncementDto updateAnnouncement(RequestAnnouncementDto requestAnnouncementDto, Long announcementId) {

        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
        announcement.update(requestAnnouncementDto);

        return ResponseAnnouncementDto.entityToDto(announcement);
    }
}

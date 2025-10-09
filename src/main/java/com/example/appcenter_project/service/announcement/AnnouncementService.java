package com.example.appcenter_project.service.announcement;

import com.example.appcenter_project.dto.AttachedFileDto;
import com.example.appcenter_project.dto.request.announement.RequestAnnouncementDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.dto.response.announcement.ResponseAnnouncementDto;
import com.example.appcenter_project.entity.announcement.Announcement;
import com.example.appcenter_project.entity.file.AttachedFile;
import com.example.appcenter_project.entity.announcement.CrawledAnnouncement;
import com.example.appcenter_project.entity.announcement.ManualAnnouncement;
import com.example.appcenter_project.entity.file.CrawledAnnouncementFile;
import com.example.appcenter_project.entity.file.ManualAnnouncementFile;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.announcement.AnnouncementType;
import com.example.appcenter_project.enums.user.Role;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.announcement.AnnouncementRepository;
import com.example.appcenter_project.repository.announcement.CrawledAnnouncementRepository;
import com.example.appcenter_project.repository.announcement.ManualAnnouncementRepository;
import com.example.appcenter_project.repository.file.AttachedFileRepository;
import com.example.appcenter_project.repository.file.CrawledAnnouncementFileRepository;
import com.example.appcenter_project.repository.file.ManualAnnouncementFileRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.fcm.FcmMessageService;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.example.appcenter_project.enums.announcement.AnnouncementType.*;
import static com.example.appcenter_project.exception.ErrorCode.*;
import static com.example.appcenter_project.exception.ErrorCode.IMAGE_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AttachedFileRepository attachedFileRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final ManualAnnouncementRepository manualAnnouncementRepository;
    private final CrawledAnnouncementFileRepository crawledAnnouncementFileRepository;
    private final ManualAnnouncementFileRepository manualAnnouncementFileRepository;
    private final FcmMessageService fcmMessageService;

    public void saveAnnouncement(Long userId, RequestAnnouncementDto requestAnnouncementDto, List<MultipartFile> files) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Role role = user.getRole();

        // 서포터즈 계정은 서포터즈 공지만 저장 가능
        if (role == Role.ROLE_DORM_SUPPORTERS) {
            saveAnnouncementDetail(requestAnnouncementDto, files, SUPPORTERS);
        }
        // 기숙사 계정은 기숙사 공지만 저장 가능
        else if (role == Role.ROLE_DORM_MANAGER || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_LIFE_MANAGER) {
            saveAnnouncementDetail(requestAnnouncementDto, files, DORMITORY);
        }
        // 관리자 계정은 모든 공지 저장 가능
        else if (role == Role.ROLE_ADMIN) {
            saveAnnouncementDetail(requestAnnouncementDto, files, UNI_DORM);
        }
    }

    private void saveAnnouncementDetail(RequestAnnouncementDto requestAnnouncementDto, List<MultipartFile> files, AnnouncementType announcementType) {
        // 공지사항 저장
        ManualAnnouncement manualAnnouncement = RequestAnnouncementDto.dtoToEntity(requestAnnouncementDto, announcementType);
        manualAnnouncementRepository.save(manualAnnouncement);

        // 첨부파일 저장
        saveUploadFile(manualAnnouncement, files);

        sendNotification(requestAnnouncementDto, announcementType);
    }

    private void sendNotification(RequestAnnouncementDto requestAnnouncementDto, AnnouncementType announcementType) {
        if (announcementType == DORMITORY) {
            fcmMessageService.sendNotificationDormitoryPerson(requestAnnouncementDto.getTitle(), requestAnnouncementDto.getContent());
        } else if (announcementType == SUPPORTERS) {
            fcmMessageService.sendNotificationDormitoryPerson(requestAnnouncementDto.getTitle(), requestAnnouncementDto.getContent());
        }else if (announcementType == UNI_DORM) {
            fcmMessageService.sendNotificationToAllUsers(requestAnnouncementDto.getTitle(), requestAnnouncementDto.getContent());
        }
    }

    private void saveUploadFile(ManualAnnouncement announcement, List<MultipartFile> files) {
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
            return baseUrl + "/files/announcement/" + fileName;
        } catch (Exception e) {
            log.warn("Could not generate static URL for Attached file path: {}", filePath);
            return null;
        }
    }

    public void deleteAttachedFile(Long userId, Long announcementId, String filePath) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Role role = user.getRole();

        // 서포터즈 계정은 서포터즈 공지 파일만 삭제 가능
        if (role == Role.ROLE_DORM_SUPPORTERS) {
            if ((announcement.getAnnouncementType()) == SUPPORTERS) {
                deleteAttachedFileDetail(filePath, announcement);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 기숙사 계정은 기숙사 공지 파일만 삭제 가능
        else if (role == Role.ROLE_DORM_MANAGER || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_LIFE_MANAGER) {
            if ((announcement.getAnnouncementType()) == DORMITORY) {
                deleteAttachedFileDetail(filePath, announcement);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 관리자 계정은 모든 공지 파일만 삭제 가능
        else if (role == Role.ROLE_ADMIN) {
            deleteAttachedFileDetail(filePath, announcement);
        }
    }

    private void deleteAttachedFileDetail(String filePath, Announcement announcement) {
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

    public List<ResponseAnnouncementDto> findAllAnnouncements() {
        List<Announcement> announcements = announcementRepository.findAll();

        // 정렬: 각 타입에 맞는 날짜 기준으로 최신순
        announcements.sort((a1, a2) -> {
            LocalDateTime date1 = getCompareDate(a1);
            LocalDateTime date2 = getCompareDate(a2);
            return date2.compareTo(date1); // 최신순 (내림차순)
        });

        return announcements.stream()
                .map(ResponseAnnouncementDto::entityToDto)
                .collect(Collectors.toList());
    }

    private LocalDateTime getCompareDate(Announcement announcement) {
        if (announcement instanceof CrawledAnnouncement) {
            CrawledAnnouncement crawled = (CrawledAnnouncement) announcement;
            return crawled.getCrawledDate() != null
                    ? crawled.getCrawledDate().atStartOfDay()
                    : crawled.getCreatedDate();
        } else {
            // ManualAnnouncement는 BaseTimeEntity의 createdDate 사용
            return announcement.getCreatedDate();
        }
    }

    public ResponseAnnouncementDetailDto findAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        if (announcement instanceof ManualAnnouncement) {
            ((ManualAnnouncement) announcement).plusViewCount();
            log.debug("[findAnnouncement] 공지사항 조회수 증가 - currentViewCount={}", announcement.getViewCount());
        }

        ResponseAnnouncementDetailDto dto = ResponseAnnouncementDetailDto.entityToDto(announcement);
        return dto;
    }

    public void deleteAnnouncement(Long userId, Long announcementId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        ManualAnnouncement announcement = manualAnnouncementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
        Role role = user.getRole();

        // 서포터즈 계정은 서포터즈 공지만 삭제 가능
        if (role == Role.ROLE_DORM_SUPPORTERS) {
            if ((announcement.getAnnouncementType()) == SUPPORTERS) {
                announcementRepository.deleteById(announcementId);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 기숙사 계정은 기숙사 공지만 삭제
        else if (role == Role.ROLE_DORM_MANAGER || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_LIFE_MANAGER) {
            if ((announcement.getAnnouncementType()) == DORMITORY) {
                announcementRepository.deleteById(announcementId);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 관리자 계정은 모든 공지 삭제 가능
        else if (role == Role.ROLE_ADMIN) {
            announcementRepository.deleteById(announcementId);
        }
    }

    public ResponseAnnouncementDto updateAnnouncement(Long userId, RequestAnnouncementDto requestAnnouncementDto, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        // 크롤링 공지사항은 수정 불가
        if (announcement instanceof CrawledAnnouncement) {
            return null;
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Role role = user.getRole();

        // 서포터즈 계정은 서포터즈 공지만 수정 가능
/*        if (role == Role.ROLE_DORM_SUPPORTERS) {
            if (announcement.getAnnouncementType()== SUPPORTERS) {
                ((ManualAnnouncement) announcement).update(requestAnnouncementDto);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 기숙사 계정은 기숙사 공지만 수정 가능
        else if (role == Role.ROLE_DORM_MANAGER || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_LIFE_MANAGER) {
            if (announcement.getAnnouncementType()== DORMITORY) {
                ((ManualAnnouncement) announcement).update(requestAnnouncementDto);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 관리자 계정은 모든 공지 수정 가능
        else if (role == Role.ROLE_ADMIN) {
            ((ManualAnnouncement) announcement).update(requestAnnouncementDto);
        }*/

        return ResponseAnnouncementDto.entityToDto(announcement);
    }

    public ResponseAnnouncementDto updateAnnouncementWithFiles(Long userId, RequestAnnouncementDto requestAnnouncementDto,
                                                               Long announcementId,
                                                               List<MultipartFile> files) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        Role role = user.getRole();
        ManualAnnouncement announcement = manualAnnouncementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        // 서포터즈 계정은 서포터즈 공지만 수정 가능
        if (role == Role.ROLE_DORM_SUPPORTERS) {
            if (announcement.getAnnouncementType() == SUPPORTERS) {
                return updateAnnouncementDetailWithFiles(requestAnnouncementDto, announcementId, files);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 기숙사 계정은 기숙사 공지만 수정 가능
        else if (role == Role.ROLE_DORM_MANAGER || role == Role.ROLE_DORM_ROOMMATE_MANAGER || role == Role.ROLE_DORM_LIFE_MANAGER) {
            if (announcement.getAnnouncementType() == DORMITORY) {
                return updateAnnouncementDetailWithFiles(requestAnnouncementDto, announcementId, files);
            } else {
                throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
            }
        }
        // 관리자 계정은 모든 공지 수정 가능
        else if (role == Role.ROLE_ADMIN) {
            return updateAnnouncementDetailWithFiles(requestAnnouncementDto, announcementId, files);
        }

        return null;
    }

    private ResponseAnnouncementDto updateAnnouncementDetailWithFiles(RequestAnnouncementDto requestAnnouncementDto, Long announcementId, List<MultipartFile> files) {
        // 1. 공지사항 조회
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        // 크롤링 공지사항은 수정 불가
        if (announcement instanceof CrawledAnnouncement) {
            return null;
        }

        // 2. 기존 첨부파일 삭제 (DB + 파일시스템)
        deleteExistingAttachedFiles(announcement);

/*        // 3. 공지사항 내용 업데이트
        ((ManualAnnouncement) announcement).update(requestAnnouncementDto);

        // 4. 새로운 첨부파일 저장
        if (files != null && !files.isEmpty()) {
            saveUploadFile(announcement, files);
        }*/

        return ResponseAnnouncementDto.entityToDto(announcement);
    }

    private void deleteExistingAttachedFiles(Announcement announcement) {
        log.info("[deleteExistingAttachedFiles] 기존 첨부파일 삭제 시작 - announcementId={}", announcement.getId());

        List<AttachedFile> existingFiles = attachedFileRepository.findByAnnouncement(announcement);

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
        }

        // DB에서 첨부파일 레코드 삭제 (cascade로 자동 삭제되지만 명시적으로 처리)
        attachedFileRepository.deleteByAnnouncement(announcement);

        // 엔티티의 컬렉션도 정리
//        announcement.getAttachedFiles().clear();

        log.info("[deleteExistingAttachedFiles] 기존 첨부파일 삭제 완료 - 삭제된 파일 수: {}", existingFiles.size());
    }
}

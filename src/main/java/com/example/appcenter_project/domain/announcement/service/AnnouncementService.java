package com.example.appcenter_project.domain.announcement.service;

import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDetailDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDto;
import com.example.appcenter_project.domain.announcement.entity.Announcement;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementCategory;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.announcement.repository.AnnouncementRepository;
import com.example.appcenter_project.domain.announcement.repository.ManualAnnouncementRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.notification.service.AnnouncementNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Comparator;
import java.util.List;

import static com.example.appcenter_project.domain.announcement.enums.AnnouncementType.*;
import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final ManualAnnouncementRepository manualAnnouncementRepository;
    private final AnnouncementNotificationService announcementNotificationService;
    private final AnnouncementFileService announcementFileService;

    // ========== Public Methods ========== //

    public void saveAnnouncement(Long userId, RequestAnnouncementDto requestAnnouncementDto, List<MultipartFile> files) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        AnnouncementType announcementType = determineAnnouncementType(user.getRole());
        Announcement announcement = createAnnouncement(requestAnnouncementDto, announcementType, files);

        // sendNotification(announcement, announcementType);
    }

    public ResponseAnnouncementDetailDto findAnnouncement(Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        plushViewCountIfManualAnnouncement(announcement);

        return ResponseAnnouncementDetailDto.from(announcement);
    }

    public List<ResponseAnnouncementDto> findAllAnnouncements(AnnouncementType type, AnnouncementCategory category, String search) {
        return announcementRepository.findAnnouncementComplex(type, category, search).stream()
                .sorted(Comparator.comparing(Announcement::getSortDate).reversed())
                .map(ResponseAnnouncementDto::from)
                .toList();
    }

    public ResponseAnnouncementDto updateAnnouncement(Long userId, RequestAnnouncementDto requestAnnouncementDto, Long announcementId) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        checkManagePermission(user, announcement.getAnnouncementType());

        announcement.update(requestAnnouncementDto);

        return ResponseAnnouncementDto.from(announcement);
    }

    public ResponseAnnouncementDto updateAnnouncementWithFiles(Long userId, RequestAnnouncementDto requestAnnouncementDto,
                                                               Long announcementId,
                                                               List<MultipartFile> files) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        checkManagePermission(user, announcement.getAnnouncementType());

        return updateAnnouncementDetailWithFiles(requestAnnouncementDto, announcement, files);
    }

    public void deleteAttachedFile(Long userId, Long announcementId, String filePath) {
        Announcement announcement = announcementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        checkManagePermission(user, announcement.getAnnouncementType());

        announcementFileService.deleteAttachedFileDetail(filePath, announcement);
    }

    public void deleteAnnouncement(Long userId, Long announcementId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        ManualAnnouncement announcement = manualAnnouncementRepository.findById(announcementId).orElseThrow(() -> new CustomException(ANNOUNCEMENT_NOT_REGISTERED));

        checkManagePermission(user, announcement.getAnnouncementType());

        announcementFileService.deleteExistingAttachedFiles(announcement);
        announcementRepository.deleteById(announcementId);
    }

    // ========== Private Methods ========== //

    private Announcement createAnnouncement(RequestAnnouncementDto requestAnnouncementDto, AnnouncementType announcementType, List<MultipartFile> files) {
        ManualAnnouncement manualAnnouncement = ManualAnnouncement.of(requestAnnouncementDto, announcementType);

        manualAnnouncementRepository.save(manualAnnouncement);
        announcementFileService.saveUploadFile(manualAnnouncement, files);

        return manualAnnouncement;
    }

    private AnnouncementType determineAnnouncementType(Role role) {
        return switch (role) {
            case ROLE_DORM_SUPPORTERS -> SUPPORTERS;
            case ROLE_DORM_MANAGER, ROLE_DORM_ROOMMATE_MANAGER, ROLE_DORM_LIFE_MANAGER -> DORMITORY;
            case ROLE_ADMIN -> UNI_DORM;
            default ->  null;
        };
    }

    private void sendNotification(Announcement announcement, AnnouncementType announcementType) {
        switch (announcementType) {
            case DORMITORY -> announcementNotificationService.sendDormitoryNotifications(announcement);
            case SUPPORTERS -> announcementNotificationService.sendSupportersNotifications(announcement);
            case UNI_DORM -> announcementNotificationService.sendUnidormNotifications(announcement);
        }
    }

    private static void plushViewCountIfManualAnnouncement(Announcement announcement) {
        if (announcement instanceof ManualAnnouncement) {
            ((ManualAnnouncement) announcement).plusViewCount();
        }
    }

    private ResponseAnnouncementDto updateAnnouncementDetailWithFiles(RequestAnnouncementDto requestAnnouncementDto, Announcement announcement, List<MultipartFile> files) {

        if (announcement instanceof ManualAnnouncement) {
            ManualAnnouncement manualAnnouncement = (ManualAnnouncement) announcement;

            announcementFileService.deleteExistingAttachedFiles(manualAnnouncement);

            if (files != null && !files.isEmpty()) {
                announcementFileService.saveUploadFile(manualAnnouncement, files);
            }

            manualAnnouncement.update(requestAnnouncementDto);
            return ResponseAnnouncementDto.from(manualAnnouncement);

        }

        announcement.update(requestAnnouncementDto);

        return ResponseAnnouncementDto.from(announcement);
    }

    private void checkManagePermission(User user, AnnouncementType announcementType) {
        Role role = user.getRole();

        switch (role) {
            case ROLE_DORM_SUPPORTERS -> {
                if (announcementType != SUPPORTERS) {
                    throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
                }
            }
            case ROLE_DORM_MANAGER, ROLE_DORM_ROOMMATE_MANAGER, ROLE_DORM_LIFE_MANAGER -> {
                if (announcementType != AnnouncementType.DORMITORY) {
                    throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
                }
            }
            case ROLE_ADMIN -> {

            }
            default -> throw new CustomException(ANNOUNCEMENT_FORBIDDEN);
        }
    }
}
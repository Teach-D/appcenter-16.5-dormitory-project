package com.example.appcenter_project.domain.announcement.service;

import com.example.appcenter_project.domain.announcement.dto.request.RequestAnnouncementDto;
import com.example.appcenter_project.domain.announcement.dto.response.ResponseAnnouncementDto;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.enums.AnnouncementType;
import com.example.appcenter_project.domain.announcement.repository.AnnouncementRepository;
import com.example.appcenter_project.domain.announcement.repository.ManualAnnouncementRepository;
import com.example.appcenter_project.domain.notification.service.AnnouncementNotificationService;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Transactional
class AnnouncementServiceTest {

    @Mock
    AnnouncementRepository announcementRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ManualAnnouncementRepository manualAnnouncementRepository;

    @Mock
    AnnouncementNotificationService announcementNotificationService;

    @Mock
    AnnouncementFileService announcementFileService;

    @InjectMocks
    AnnouncementService announcementService;

    @Test
    @DisplayName("공지사항 생성 - 서포터즈 계정이면 서포터즈 타입으로 저장")
    void saveAnnouncement_서포터즈계정() {
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ROLE_DORM_SUPPORTERS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RequestAnnouncementDto dto = mock(RequestAnnouncementDto.class);

        announcementService.saveAnnouncement(1L, dto, null);

        verify(manualAnnouncementRepository).save(any(ManualAnnouncement.class));
        verify(announcementNotificationService).sendSupportersNotifications(any());
    }

    @Test
    @DisplayName("공지사항 생성 - 관리자 계정이면 유니돔 타입으로 저장")
    void saveAnnouncement_관리자계정() {
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RequestAnnouncementDto dto = mock(RequestAnnouncementDto.class);

        announcementService.saveAnnouncement(1L, dto, null);

        verify(manualAnnouncementRepository).save(any(ManualAnnouncement.class));
        verify(announcementNotificationService).sendUnidormNotifications(any());
    }

    @Test
    @DisplayName("공지사항 생성 - 유저 없으면 예외")
    void saveAnnouncement_유저_없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.saveAnnouncement(99L, mock(RequestAnnouncementDto.class), null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("공지사항 단일 조회 - ManualAnnouncement")
    void findAnnouncement_정상_반환() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(mockAnnouncement.getCreatedDate()).thenReturn(LocalDateTime.now());
        when(mockAnnouncement.getModifiedDate()).thenReturn(LocalDateTime.now());
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));

        var result = announcementService.findAnnouncement(1L);

        assertThat(result).isNotNull();
        verify(mockAnnouncement).plusViewCount();
    }

    @Test
    @DisplayName("공지사항 단일 조회 - 없으면 예외")
    void findAnnouncement_없으면_예외() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementService.findAnnouncement(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ANNOUNCEMENT_NOT_REGISTERED);
    }

    @Test
    @DisplayName("공지사항 전체 조회 - 정상 반환")
    void findAllAnnouncements_정상_반환() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(mockAnnouncement.getSortDate()).thenReturn(LocalDateTime.now());
        when(announcementRepository.findAnnouncementComplex(any(), any(), any()))
                .thenReturn(List.of(mockAnnouncement));

        List<ResponseAnnouncementDto> result = announcementService.findAllAnnouncements(null, null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("공지사항 전체 조회 - 빈 목록이면 빈 리스트 반환")
    void findAllAnnouncements_빈_목록() {
        when(announcementRepository.findAnnouncementComplex(any(), any(), any())).thenReturn(List.of());

        List<ResponseAnnouncementDto> result = announcementService.findAllAnnouncements(null, null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("공지사항 수정 - 권한 있으면 정상 수정")
    void updateAnnouncement_정상_수정() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(mockAnnouncement.getAnnouncementType()).thenReturn(AnnouncementType.SUPPORTERS);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));

        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ROLE_DORM_SUPPORTERS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RequestAnnouncementDto dto = mock(RequestAnnouncementDto.class);
        ResponseAnnouncementDto result = announcementService.updateAnnouncement(1L, dto, 1L);

        verify(mockAnnouncement).update(dto);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("공지사항 수정 - 권한 없으면 예외")
    void updateAnnouncement_권한없음_예외() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(mockAnnouncement.getAnnouncementType()).thenReturn(AnnouncementType.DORMITORY);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));

        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ROLE_DORM_SUPPORTERS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        assertThatThrownBy(() -> announcementService.updateAnnouncement(1L, mock(RequestAnnouncementDto.class), 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ANNOUNCEMENT_FORBIDDEN);
    }

    @Test
    @DisplayName("공지사항 삭제 - 관리자면 정상 삭제")
    void deleteAnnouncement_정상_삭제() {
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ROLE_ADMIN);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(mockAnnouncement.getAnnouncementType()).thenReturn(AnnouncementType.DORMITORY);
        when(manualAnnouncementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));

        announcementService.deleteAnnouncement(1L, 1L);

        verify(announcementFileService).deleteExistingAttachedFiles(mockAnnouncement);
        verify(announcementRepository).deleteById(1L);
    }

    @Test
    @DisplayName("공지사항 삭제 - 권한 없으면 예외")
    void deleteAnnouncement_권한없음_예외() {
        User mockUser = mock(User.class);
        when(mockUser.getRole()).thenReturn(Role.ROLE_DORM_SUPPORTERS);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(mockAnnouncement.getAnnouncementType()).thenReturn(AnnouncementType.DORMITORY);
        when(manualAnnouncementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));

        assertThatThrownBy(() -> announcementService.deleteAnnouncement(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ANNOUNCEMENT_FORBIDDEN);
    }
}
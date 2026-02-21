package com.example.appcenter_project.domain.announcement.service;

import com.example.appcenter_project.common.file.entity.AttachedFile;
import com.example.appcenter_project.common.file.repository.AttachedFileRepository;
import com.example.appcenter_project.common.file.repository.CrawledAnnouncementFileRepository;
import com.example.appcenter_project.common.file.repository.ManualAnnouncementFileRepository;
import com.example.appcenter_project.domain.announcement.entity.CrawledAnnouncement;
import com.example.appcenter_project.domain.announcement.entity.ManualAnnouncement;
import com.example.appcenter_project.domain.announcement.repository.AnnouncementRepository;
import com.example.appcenter_project.global.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Transactional
class AnnouncementFileServiceTest {

    @Mock
    AttachedFileRepository attachedFileRepository;

    @Mock
    CrawledAnnouncementFileRepository crawledAnnouncementFileRepository;

    @Mock
    ManualAnnouncementFileRepository manualAnnouncementFileRepository;

    @Mock
    AnnouncementRepository announcementRepository;

    @InjectMocks
    AnnouncementFileService announcementFileService;

    @Test
    @DisplayName("첨부파일 조회 - ManualAnnouncement일 때 첨부파일 없으면 빈 리스트 반환")
    void findAttachedFileByAnnouncementId_ManualAnnouncement_빈목록() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(announcementRepository.findById(1L)).thenReturn(Optional.of(mockAnnouncement));
        when(manualAnnouncementFileRepository.findByManualAnnouncementId(any())).thenReturn(List.of());

        var result = announcementFileService.findAttachedFileByAnnouncementId(1L, mock(HttpServletRequest.class));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("첨부파일 조회 - CrawledAnnouncement일 때 첨부파일 없으면 빈 리스트 반환")
    void findAttachedFileByAnnouncementId_CrawledAnnouncement_빈목록() {
        CrawledAnnouncement mockAnnouncement = mock(CrawledAnnouncement.class);
        when(announcementRepository.findById(2L)).thenReturn(Optional.of(mockAnnouncement));
        when(crawledAnnouncementFileRepository.findByCrawledAnnouncementId(2L)).thenReturn(List.of());

        var result = announcementFileService.findAttachedFileByAnnouncementId(2L, mock(HttpServletRequest.class));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("첨부파일 조회 - 공지사항 없으면 예외")
    void findAttachedFileByAnnouncementId_없으면_예외() {
        when(announcementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementFileService.findAttachedFileByAnnouncementId(99L, mock(HttpServletRequest.class)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ANNOUNCEMENT_NOT_REGISTERED);
    }


    @Test
    @DisplayName("첨부파일 일괄 삭제 - 공지사항의 첨부파일 전부 삭제")
    void deleteExistingAttachedFiles_파일_삭제() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        AttachedFile mockFile = mock(AttachedFile.class);
        when(mockFile.getFilePath()).thenReturn("/non/existent/path/file.pdf");
        when(manualAnnouncementFileRepository.findByManualAnnouncementId(any())).thenReturn(List.of(mockFile));

        announcementFileService.deleteExistingAttachedFiles(mockAnnouncement);

        verify(attachedFileRepository).delete(mockFile);
    }

    @Test
    @DisplayName("첨부파일 일괄 삭제 - 파일 없으면 delete 호출 안 함")
    void deleteExistingAttachedFiles_파일_없으면_스킵() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(manualAnnouncementFileRepository.findByManualAnnouncementId(any())).thenReturn(List.of());

        announcementFileService.deleteExistingAttachedFiles(mockAnnouncement);

        verify(attachedFileRepository, never()).delete(any());
    }

    @Test
    @DisplayName("첨부파일 단건 삭제 - 경로로 파일 조회 후 DB 삭제")
    void deleteAttachedFileDetail_파일_삭제() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        AttachedFile mockFile = mock(AttachedFile.class);
        when(mockFile.getFilePath()).thenReturn("/non/existent/path/file.pdf");
        when(attachedFileRepository.findByFilePathAndAnnouncement("/path/file.pdf", mockAnnouncement))
                .thenReturn(Optional.of(mockFile));

        announcementFileService.deleteAttachedFileDetail("/path/file.pdf", mockAnnouncement);

        verify(attachedFileRepository).delete(mockFile);
    }

    @Test
    @DisplayName("첨부파일 단건 삭제 - 파일 없으면 예외")
    void deleteAttachedFileDetail_파일_없으면_예외() {
        ManualAnnouncement mockAnnouncement = mock(ManualAnnouncement.class);
        when(attachedFileRepository.findByFilePathAndAnnouncement(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> announcementFileService.deleteAttachedFileDetail("/path/file.pdf", mockAnnouncement))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ATTACHEDFILE_NOT_REGISTERED);
    }
}
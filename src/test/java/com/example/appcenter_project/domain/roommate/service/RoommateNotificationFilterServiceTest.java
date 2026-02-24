package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateNotificationFilterDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateNotificationFilter;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateNotificationFilterRepository;
import com.example.appcenter_project.domain.user.entity.User;
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

import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoommateNotificationFilterServiceTest {

    @Mock
    RoommateNotificationFilterRepository filterRepository;

    @Mock
    RoommateBoardRepository boardRepository;

    @Mock
    RoommateMatchingRepository roommateMatchingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ImageService imageService;

    @InjectMocks
    RoommateNotificationFilterService roommateNotificationFilterService;

    @Test
    @DisplayName("필터 저장/수정 - 신규 생성")
    void saveOrUpdateFilter_신규_생성() {
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(filterRepository.findByUserId(1L)).thenReturn(Optional.empty());

        RequestRoommateNotificationFilterDto dto = mock(RequestRoommateNotificationFilterDto.class);

        roommateNotificationFilterService.saveOrUpdateFilter(1L, dto);

        verify(filterRepository).save(any(RoommateNotificationFilter.class));
    }

    @Test
    @DisplayName("필터 저장/수정 - 기존 필터 업데이트")
    void saveOrUpdateFilter_기존_업데이트() {
        User mockUser = mock(User.class);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RoommateNotificationFilter existingFilter = mock(RoommateNotificationFilter.class);
        when(filterRepository.findByUserId(1L)).thenReturn(Optional.of(existingFilter));

        RequestRoommateNotificationFilterDto dto = mock(RequestRoommateNotificationFilterDto.class);

        roommateNotificationFilterService.saveOrUpdateFilter(1L, dto);

        verify(existingFilter).update(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(filterRepository, never()).save(any());
    }

    @Test
    @DisplayName("필터 저장/수정 - 유저 없으면 예외")
    void saveOrUpdateFilter_유저없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roommateNotificationFilterService.saveOrUpdateFilter(99L, mock(RequestRoommateNotificationFilterDto.class)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("필터 조회 - 정상 반환")
    void getFilter_정상_반환() {
        when(userRepository.existsById(1L)).thenReturn(true);

        RoommateNotificationFilter filter = mock(RoommateNotificationFilter.class);
        when(filterRepository.findByUserId(1L)).thenReturn(Optional.of(filter));

        ResponseRoommateNotificationFilterDto result = roommateNotificationFilterService.getFilter(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("필터 조회 - 필터 없으면 null 필드인 빈 응답 반환")
    void getFilter_필터없으면_빈응답반환() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(filterRepository.findByUserId(1L)).thenReturn(Optional.empty());

        ResponseRoommateNotificationFilterDto result = roommateNotificationFilterService.getFilter(1L);

        assertThat(result).isNotNull();
        assertThat(result.getDormType()).isNull();
        assertThat(result.getSmoking()).isNull();
    }

    @Test
    @DisplayName("필터 조회 - 유저 없으면 예외")
    void getFilter_유저없으면_예외() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> roommateNotificationFilterService.getFilter(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    @Test
    @DisplayName("필터 삭제 - 정상 삭제")
    void deleteFilter_정상_삭제() {
        when(userRepository.existsById(1L)).thenReturn(true);

        RoommateNotificationFilter filter = mock(RoommateNotificationFilter.class);
        when(filterRepository.findByUserId(1L)).thenReturn(Optional.of(filter));

        roommateNotificationFilterService.deleteFilter(1L);

        verify(filterRepository).delete(filter);
    }

    @Test
    @DisplayName("필터 삭제 - 필터 없으면 삭제 안함")
    void deleteFilter_필터없으면_삭제안함() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(filterRepository.findByUserId(1L)).thenReturn(Optional.empty());

        roommateNotificationFilterService.deleteFilter(1L);

        verify(filterRepository, never()).delete(any());
    }

    @Test
    @DisplayName("필터 삭제 - 유저 없으면 예외")
    void deleteFilter_유저없으면_예외() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> roommateNotificationFilterService.deleteFilter(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }
}

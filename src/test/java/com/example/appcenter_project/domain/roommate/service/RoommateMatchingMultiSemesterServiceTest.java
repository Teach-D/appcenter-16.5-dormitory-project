package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.notification.service.NotificationService;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.entity.RoommateMatching;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoommateMatchingMultiSemesterServiceTest {

    @Mock
    RoommateMatchingRepository roommateMatchingRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    MyRoommateRepository myRoommateRepository;

    @Mock
    RoommateChattingRoomRepository roommateChattingRoomRepository;

    @Mock
    FcmMessageService fcmMessageService;

    @Mock
    NotificationService notificationService;

    @Mock
    RoommateMatchingPeriodResolver periodResolver;

    @InjectMocks
    RoommateMatchingService roommateMatchingService;

    @BeforeEach
    void setUp() {
        given(periodResolver.resolveCurrent(any(LocalDate.class)))
                .willReturn(new MatchingPeriod(2026, SemesterType.FIRST, RoommateMatchingStatus.OPEN));
    }

    private User buildMockUser(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn("사용자" + id);
        when(user.getStudentNumber()).thenReturn("2025000" + id);
        return user;
    }

    @Test
    @DisplayName("MyRoommate 저장 — AC-9 매칭 수락 시 year+semester가 포함된 MyRoommate 저장")
    void should_save_myRoommate_with_year_and_semester_when_matching_accepted() {
        // given
        User receiver = buildMockUser(2L);
        User sender = buildMockUser(1L);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getStatus()).thenReturn(MatchingStatus.REQUEST);
        when(matching.getSender()).thenReturn(sender);
        when(matching.getId()).thenReturn(10L);
        given(roommateMatchingRepository.findById(10L)).willReturn(Optional.of(matching));

        given(roommateMatchingRepository.existsBySenderAndStatus(receiver, MatchingStatus.COMPLETED)).willReturn(false);
        given(roommateMatchingRepository.existsByReceiverAndStatus(receiver, MatchingStatus.COMPLETED)).willReturn(false);
        given(roommateMatchingRepository.findAllBySenderAndReceiverAndStatusNot(any(), any(), any()))
                .willReturn(List.of());
        given(notificationService.createRoommateAcceptNotification(any(), any()))
                .willReturn(mock(com.example.appcenter_project.domain.notification.entity.Notification.class));

        // when
        roommateMatchingService.acceptMatching(10L, 2L);

        // then
        then(myRoommateRepository).should(org.mockito.Mockito.times(2)).save(any(MyRoommate.class));
    }

    @Test
    @DisplayName("멱등 처리 — AC-12 동일 (user, year, semester) MyRoommate 이미 존재 시 중복 저장 안 함")
    void should_not_save_duplicate_myRoommate_when_same_semester_already_exists() {
        // given
        User receiver = buildMockUser(2L);
        User sender = buildMockUser(1L);
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getStatus()).thenReturn(MatchingStatus.REQUEST);
        when(matching.getSender()).thenReturn(sender);
        when(matching.getId()).thenReturn(10L);
        given(roommateMatchingRepository.findById(10L)).willReturn(Optional.of(matching));

        given(roommateMatchingRepository.existsBySenderAndStatus(receiver, MatchingStatus.COMPLETED)).willReturn(false);
        given(roommateMatchingRepository.existsByReceiverAndStatus(receiver, MatchingStatus.COMPLETED)).willReturn(false);
        given(roommateMatchingRepository.findAllBySenderAndReceiverAndStatusNot(any(), any(), any()))
                .willReturn(List.of());
        given(notificationService.createRoommateAcceptNotification(any(), any()))
                .willReturn(mock(com.example.appcenter_project.domain.notification.entity.Notification.class));

        MyRoommate existingMyRoommate = mock(MyRoommate.class);
        given(myRoommateRepository.findByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(Optional.of(existingMyRoommate));
        given(myRoommateRepository.findByUserIdAndYearAndSemester(2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.of(existingMyRoommate));

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateMatchingService.acceptMatching(10L, 2L);

        // then
        assertThatCode(action).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("현재 학기만 삭제 — AC-14 cancelMatching 시 현재 학기 MyRoommate만 삭제")
    void should_delete_only_current_semester_myRoommate_when_cancel_matching() {
        // given
        User sender = buildMockUser(1L);
        User receiver = buildMockUser(2L);

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getStatus()).thenReturn(MatchingStatus.COMPLETED);
        when(matching.getSender()).thenReturn(sender);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getId()).thenReturn(10L);
        given(roommateMatchingRepository.findById(10L)).willReturn(Optional.of(matching));

        given(myRoommateRepository.deleteByUserIdAndRoommateIdAndYearAndSemester(
                anyLong(), anyLong(), eq(2026), eq(SemesterType.FIRST))).willReturn(1);

        // when
        roommateMatchingService.cancelMatching(10L, 1L);

        // then
        then(myRoommateRepository).should().deleteByUserIdAndRoommateIdAndYearAndSemester(
                eq(1L), eq(2L), eq(2026), eq(SemesterType.FIRST));
        then(myRoommateRepository).should().deleteByUserIdAndRoommateIdAndYearAndSemester(
                eq(2L), eq(1L), eq(2026), eq(SemesterType.FIRST));
        then(myRoommateRepository).should(never()).deleteByUserIdAndRoommateId(anyLong(), anyLong());
    }
}

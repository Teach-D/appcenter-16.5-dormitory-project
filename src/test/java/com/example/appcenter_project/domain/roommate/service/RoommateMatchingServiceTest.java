package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.notification.service.NotificationService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseReceivedRoommateMatchingDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateMatchingDto;
import com.example.appcenter_project.domain.roommate.entity.MyRoommate;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.roommate.entity.RoommateMatching;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.domain.fcm.service.FcmMessageService;
import com.example.appcenter_project.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoommateMatchingServiceTest {

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

    @InjectMocks
    RoommateMatchingService roommateMatchingService;

    private User buildMockUser(Long id, String studentNumber) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn("사용자" + id);
        when(user.getStudentNumber()).thenReturn(studentNumber);
        RoommateBoard board = mock(RoommateBoard.class);
        when(user.getRoommateBoard()).thenReturn(board);
        return user;
    }

    @Test
    @DisplayName("매칭 요청 - 정상 요청")
    void requestMatching_정상_요청() {
        User sender = buildMockUser(1L, "20250001");
        User receiver = buildMockUser(2L, "20250002");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByStudentNumber("20250002")).thenReturn(Optional.of(receiver));

        when(roommateMatchingRepository.existsBySenderAndStatus(sender, MatchingStatus.COMPLETED)).thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(sender, MatchingStatus.COMPLETED)).thenReturn(false);
        when(roommateMatchingRepository.existsBySenderAndStatus(receiver, MatchingStatus.COMPLETED)).thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(receiver, MatchingStatus.COMPLETED)).thenReturn(false);

        when(roommateMatchingRepository.findBySenderAndReceiverAndStatus(receiver, sender, MatchingStatus.REQUEST))
                .thenReturn(Optional.empty());

        when(roommateMatchingRepository.existsBySenderAndReceiverAndStatus(sender, receiver, MatchingStatus.REQUEST))
                .thenReturn(false);

        RoommateMatching savedMatching = mock(RoommateMatching.class);
        when(savedMatching.getId()).thenReturn(10L);
        when(savedMatching.getReceiver()).thenReturn(receiver);
        when(savedMatching.getStatus()).thenReturn(MatchingStatus.REQUEST);
        when(roommateMatchingRepository.save(any(RoommateMatching.class))).thenReturn(savedMatching);

        when(notificationService.createRoommateRequestNotification(anyString(), any()))
                .thenReturn(mock(com.example.appcenter_project.domain.notification.entity.Notification.class));

        ResponseRoommateMatchingDto result = roommateMatchingService.requestMatching(1L, "20250002");

        assertThat(result).isNotNull();
        verify(roommateMatchingRepository).save(any(RoommateMatching.class));
    }

    @Test
    @DisplayName("매칭 요청 - 보내는 유저 없으면 예외")
    void requestMatching_보내는유저없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateMatchingService.requestMatching(99L, "20250002"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_USER_NOT_FOUND);
    }

    @Test
    @DisplayName("매칭 요청 - 이미 매칭된 사용자면 예외")
    void requestMatching_이미매칭됨_예외() {
        User sender = buildMockUser(1L, "20250001");
        User receiver = buildMockUser(2L, "20250002");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByStudentNumber("20250002")).thenReturn(Optional.of(receiver));

        when(roommateMatchingRepository.existsBySenderAndStatus(sender, MatchingStatus.COMPLETED)).thenReturn(true);

        assertThatThrownBy(() -> roommateMatchingService.requestMatching(1L, "20250002"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_ALREADY_MATCHED);
    }

    @Test
    @DisplayName("매칭 요청 - 중복 요청이면 예외")
    void requestMatching_중복요청_예외() {
        User sender = buildMockUser(1L, "20250001");
        User receiver = buildMockUser(2L, "20250002");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findByStudentNumber("20250002")).thenReturn(Optional.of(receiver));

        when(roommateMatchingRepository.existsBySenderAndStatus(any(), eq(MatchingStatus.COMPLETED))).thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(any(), eq(MatchingStatus.COMPLETED))).thenReturn(false);
        when(roommateMatchingRepository.findBySenderAndReceiverAndStatus(receiver, sender, MatchingStatus.REQUEST))
                .thenReturn(Optional.empty());
        when(roommateMatchingRepository.existsBySenderAndReceiverAndStatus(sender, receiver, MatchingStatus.REQUEST))
                .thenReturn(true);

        assertThatThrownBy(() -> roommateMatchingService.requestMatching(1L, "20250002"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_MATCHING_ALREADY_REQUESTED);
    }

    @Test
    @DisplayName("매칭 수락 - 정상 수락")
    void acceptMatching_정상_수락() {
        User receiver = buildMockUser(2L, "20250002");
        User sender = buildMockUser(1L, "20250001");
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getStatus()).thenReturn(MatchingStatus.REQUEST);
        when(matching.getSender()).thenReturn(sender);
        when(matching.getId()).thenReturn(10L);
        when(roommateMatchingRepository.findById(10L)).thenReturn(Optional.of(matching));

        when(roommateMatchingRepository.existsBySenderAndStatus(receiver, MatchingStatus.COMPLETED)).thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(receiver, MatchingStatus.COMPLETED)).thenReturn(false);

        when(roommateMatchingRepository.findAllBySenderAndReceiverAndStatusNot(any(), any(), any()))
                .thenReturn(List.of());

        when(notificationService.createRoommateAcceptNotification(anyString(), any()))
                .thenReturn(mock(com.example.appcenter_project.domain.notification.entity.Notification.class));

        roommateMatchingService.acceptMatching(10L, 2L);

        verify(matching).complete();
        verify(myRoommateRepository, times(2)).save(any(MyRoommate.class));
    }

    @Test
    @DisplayName("매칭 수락 - 매칭 없으면 예외")
    void acceptMatching_매칭없으면_예외() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(mock(User.class)));
        when(roommateMatchingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateMatchingService.acceptMatching(99L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_MATCHING_NOT_FOUND);
    }

    @Test
    @DisplayName("매칭 수락 - 수신자가 아니면 예외")
    void acceptMatching_수신자아님_예외() {
        User user = buildMockUser(2L, "20250002");
        User realReceiver = buildMockUser(3L, "20250003");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getReceiver()).thenReturn(realReceiver); // 수신자는 3L
        when(roommateMatchingRepository.findById(10L)).thenReturn(Optional.of(matching));

        assertThatThrownBy(() -> roommateMatchingService.acceptMatching(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_MATCHING_NOT_FOR_USER);
    }

    @Test
    @DisplayName("매칭 거절 - 정상 거절")
    void rejectMatching_정상_거절() {
        User receiver = buildMockUser(2L, "20250002");
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getStatus()).thenReturn(MatchingStatus.REQUEST);
        when(roommateMatchingRepository.findById(10L)).thenReturn(Optional.of(matching));

        roommateMatchingService.rejectMatching(10L, 2L);

        verify(matching).fail();
    }

    @Test
    @DisplayName("매칭 거절 - 이미 완료된 요청이면 예외")
    void rejectMatching_이미완료_예외() {
        User receiver = buildMockUser(2L, "20250002");
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getStatus()).thenReturn(MatchingStatus.COMPLETED); // 이미 완료됨
        when(roommateMatchingRepository.findById(10L)).thenReturn(Optional.of(matching));

        assertThatThrownBy(() -> roommateMatchingService.rejectMatching(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_MATCHING_ALREADY_COMPLETED);
    }

    @Test
    @DisplayName("받은 매칭 목록 조회 - 정상 반환")
    void getReceivedMatchings_정상_반환() {
        User receiver = buildMockUser(2L, "20250002");
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));

        User sender = buildMockUser(1L, "20250001");
        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getId()).thenReturn(10L);
        when(matching.getSender()).thenReturn(sender);
        when(matching.getStatus()).thenReturn(MatchingStatus.REQUEST);

        when(roommateMatchingRepository.findAllByReceiverAndStatus(receiver, MatchingStatus.REQUEST))
                .thenReturn(List.of(matching));

        List<ResponseReceivedRoommateMatchingDto> result = roommateMatchingService.getReceivedMatchings(2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSenderId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("받은 매칭 목록 조회 - 없으면 빈 리스트")
    void getReceivedMatchings_없으면_빈리스트() {
        User receiver = buildMockUser(2L, "20250002");
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(roommateMatchingRepository.findAllByReceiverAndStatus(receiver, MatchingStatus.REQUEST))
                .thenReturn(List.of());

        List<ResponseReceivedRoommateMatchingDto> result = roommateMatchingService.getReceivedMatchings(2L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("매칭 취소 - 정상 취소")
    void cancelMatching_정상_취소() {
        User sender = buildMockUser(1L, "20250001");
        User receiver = buildMockUser(2L, "20250002");

        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getStatus()).thenReturn(MatchingStatus.COMPLETED);
        when(matching.getSender()).thenReturn(sender);
        when(matching.getReceiver()).thenReturn(receiver);
        when(matching.getId()).thenReturn(10L);
        when(roommateMatchingRepository.findById(10L)).thenReturn(Optional.of(matching));
        when(myRoommateRepository.deleteByUserIdAndRoommateId(anyLong(), anyLong())).thenReturn(1);

        roommateMatchingService.cancelMatching(10L, 1L);

        verify(roommateMatchingRepository).delete(matching);
    }

    @Test
    @DisplayName("매칭 취소 - COMPLETED 상태가 아니면 예외")
    void cancelMatching_완료상태아님_예외() {
        RoommateMatching matching = mock(RoommateMatching.class);
        when(matching.getStatus()).thenReturn(MatchingStatus.REQUEST);
        when(roommateMatchingRepository.findById(10L)).thenReturn(Optional.of(matching));

        assertThatThrownBy(() -> roommateMatchingService.cancelMatching(10L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_MATCHING_NOT_COMPLETED);
    }
}
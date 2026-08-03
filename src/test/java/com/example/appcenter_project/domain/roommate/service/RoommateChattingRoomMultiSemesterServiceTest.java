package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.block.service.BlockService;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateChatRoomDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.repository.MyRoommateRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
class RoommateChattingRoomMultiSemesterServiceTest {

    @Mock
    RoommateChattingRoomRepository roommateChattingRoomRepository;

    @Mock
    RoommateBoardRepository roommateBoardRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ImageService imageService;

    @Mock
    RoommateChattingChatService roommateChattingChatService;

    @Mock
    MyRoommateRepository myRoommateRepository;

    @Mock
    RoommateMatchingPeriodResolver periodResolver;

    @Mock
    BlockService blockService;

    @InjectMocks
    RoommateChattingRoomService roommateChattingRoomService;

    @BeforeEach
    void setUp() {
        given(periodResolver.resolveCurrent(any(LocalDate.class)))
                .willReturn(new MatchingPeriod(2026, SemesterType.FIRST, RoommateMatchingStatus.OPEN));
    }

    private User buildMockUser(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn("사용자" + id);
        return user;
    }

    private RoommateChattingRoom buildMockRoom(User host, User guest) {
        RoommateChattingRoom room = mock(RoommateChattingRoom.class);
        when(room.getId()).thenReturn(100L);
        when(room.getHost()).thenReturn(host);
        when(room.getGuest()).thenReturn(guest);
        when(room.isHostLeft()).thenReturn(false);
        when(room.isGuestLeft()).thenReturn(false);
        when(room.isBothLeft()).thenReturn(false);
        when(room.getChattingChatList()).thenReturn(new ArrayList<>());
        when(room.getRoommateBoard()).thenReturn(null);
        return room;
    }

    @Test
    @DisplayName("isRoommate=true — AC-13 BR-710 현재 학기에 매칭된 상대방과의 채팅방은 isRoommate=true")
    void should_return_isRoommate_true_when_matched_in_current_semester() {
        // given
        User user = buildMockUser(1L);
        User partner = buildMockUser(2L);
        RoommateChattingRoom room = buildMockRoom(user, partner);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateChattingRoomRepository.findAllByHostOrGuest(user, user)).willReturn(List.of(room));
        given(myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.of(mock(com.example.appcenter_project.domain.roommate.entity.MyRoommate.class)));
        given(roommateBoardRepository.findByUserId(anyLong())).willReturn(Optional.empty());

        // when
        List<ResponseRoommateChatRoomDto> result = roommateChattingRoomService
                .findRoommateChatRoomListByUser(userDetails, mock(HttpServletRequest.class));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRoommate()).isTrue();
    }

    @Test
    @DisplayName("isRoommate=false — AC-13 BR-710 이전 학기에만 매칭된 상대방과의 채팅방은 isRoommate=false")
    void should_return_isRoommate_false_when_matched_only_in_previous_semester() {
        // given
        User user = buildMockUser(1L);
        User partner = buildMockUser(2L);
        RoommateChattingRoom room = buildMockRoom(user, partner);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateChattingRoomRepository.findAllByHostOrGuest(user, user)).willReturn(List.of(room));
        given(myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());
        given(roommateBoardRepository.findByUserId(anyLong())).willReturn(Optional.empty());

        // when
        List<ResponseRoommateChatRoomDto> result = roommateChattingRoomService
                .findRoommateChatRoomListByUser(userDetails, mock(HttpServletRequest.class));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).isRoommate()).isFalse();
    }

    @Test
    @DisplayName("현재 학기 스코프 쿼리 사용 — AC-13 BR-710 isRoommate 판단 시 전 학기 포함 메서드 미호출")
    void should_use_semester_scoped_query_not_legacy_query_for_isRoommate() {
        // given
        User user = buildMockUser(1L);
        User partner = buildMockUser(2L);
        RoommateChattingRoom room = buildMockRoom(user, partner);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateChattingRoomRepository.findAllByHostOrGuest(user, user)).willReturn(List.of(room));
        given(myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());
        given(roommateBoardRepository.findByUserId(anyLong())).willReturn(Optional.empty());

        // when
        roommateChattingRoomService.findRoommateChatRoomListByUser(userDetails, mock(HttpServletRequest.class));

        // then
        then(myRoommateRepository).should(never()).findByUserIdAndRoommateId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("opponentBoardTitle — 상대의 현재 학기 게시글 제목을 반환")
    void should_return_opponent_current_semester_board_title() {
        User user = buildMockUser(1L);      // host(작성자) 시점 조회
        User partner = buildMockUser(2L);   // guest(요청자)
        RoommateChattingRoom room = buildMockRoom(user, partner);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateChattingRoomRepository.findAllByHostOrGuest(user, user)).willReturn(List.of(room));
        given(myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());

        RoommateBoard guestBoard = mock(com.example.appcenter_project.domain.roommate.entity.RoommateBoard.class);
        when(guestBoard.getTitle()).thenReturn("게스트 현재학기 모집글");
        given(roommateBoardRepository.findByUserIdAndYearAndSemester(2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.of(guestBoard));

        List<ResponseRoommateChatRoomDto> result = roommateChattingRoomService
                .findRoommateChatRoomListByUser(userDetails, mock(HttpServletRequest.class));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOpponentBoardTitle()).isEqualTo("게스트 현재학기 모집글");
    }

    @Test
    @DisplayName("opponentBoardTitle — 상대가 현재 학기 게시글 없으면 null")
    void should_return_null_opponent_board_title_when_none_in_current_semester() {
        User user = buildMockUser(1L);
        User partner = buildMockUser(2L);
        RoommateChattingRoom room = buildMockRoom(user, partner);

        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(1L);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateChattingRoomRepository.findAllByHostOrGuest(user, user)).willReturn(List.of(room));
        given(myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(1L, 2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());
        given(roommateBoardRepository.findByUserIdAndYearAndSemester(2L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());

        List<ResponseRoommateChatRoomDto> result = roommateChattingRoomService
                .findRoommateChatRoomListByUser(userDetails, mock(HttpServletRequest.class));

        assertThat(result.get(0).getOpponentBoardTitle()).isNull();
    }
}

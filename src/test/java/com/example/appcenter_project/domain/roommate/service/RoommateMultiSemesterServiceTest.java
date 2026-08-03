package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.notification.service.RoommateNotificationService;
import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateFormDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.RoommateMatchingStatus;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardLikeRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardReadRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateCheckListRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateChattingRoomRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.mixpanel.MixpanelService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
class RoommateMultiSemesterServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    RoommateCheckListRepository roommateCheckListRepository;

    @Mock
    RoommateBoardRepository roommateBoardRepository;

    @Mock
    RoommateBoardLikeRepository roommateBoardLikeRepository;

    @Mock
    RoommateMatchingRepository roommateMatchingRepository;

    @Mock
    RoommateChattingRoomRepository roommateChattingRoomRepository;

    @Mock
    RoommateBoardReadRepository roommateBoardReadRepository;

    @Mock
    ImageService imageService;

    @Mock
    RoommateNotificationService roommateNotificationService;

    @Mock
    MixpanelService mixpanelService;

    @Mock
    RoommateMatchingPeriodResolver periodResolver;

    @InjectMocks
    RoommateService roommateService;

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

    private RoommateBoard buildMockBoard(Long id, User user) {
        RoommateBoard board = mock(RoommateBoard.class);
        RoommateCheckList cl = buildMockCheckList(user);
        when(board.getId()).thenReturn(id);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(cl);
        when(board.getRoommateBoardLike()).thenReturn(0);
        when(board.getRoommateBoardLikeList()).thenReturn(new ArrayList<>());
        when(board.getCreatedDate()).thenReturn(LocalDateTime.now());
        return board;
    }

    private RoommateCheckList buildMockCheckList(User user) {
        RoommateCheckList cl = mock(RoommateCheckList.class);
        when(cl.getTitle()).thenReturn("룸메이트 찾아요");
        when(cl.getDormPeriod()).thenReturn(Collections.emptySet());
        when(cl.getMbti()).thenReturn("INFJ");
        when(cl.getUser()).thenReturn(user);
        return cl;
    }

    @Test
    @DisplayName("CustomException 발생 — AC-2 BR-710 현재 학기에 이미 게시글 있는 유저 POST /roommates")
    void should_throw_CustomException_when_board_already_exists_in_current_semester() {
        // given
        User user = buildMockUser(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateCheckListRepository.existsByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(true);

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.createRoommateCheckListandBoard(mock(RequestRoommateFormDto.class), 1L);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOMMATE_BOARD_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("409 미발생 — AC-3 다른 학기에는 게시글 없으면 정상 생성")
    void should_not_throw_when_board_exists_only_in_different_semester() {
        // given
        User user = buildMockUser(1L);
        RequestRoommateFormDto dto = mock(RequestRoommateFormDto.class);
        when(dto.getTitle()).thenReturn("룸메이트 찾아요");
        when(dto.getDormPeriod()).thenReturn(null);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(roommateCheckListRepository.existsByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(false);

        RoommateCheckList savedCl = buildMockCheckList(user);
        given(roommateCheckListRepository.save(any(RoommateCheckList.class))).willReturn(savedCl);

        RoommateBoard savedBoard = buildMockBoard(1L, user);
        given(roommateBoardRepository.save(any(RoommateBoard.class))).willReturn(savedBoard);

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.createRoommateCheckListandBoard(dto, 1L);

        // then
        assertThatCode(action).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("정상 반환 — AC-4 updateRoommateChecklistAndBoard boardId로 게시글 조회")
    void should_find_board_by_boardId_when_updating() {
        // given
        User user = buildMockUser(1L);
        RoommateBoard board = buildMockBoard(10L, user);
        RoommateCheckList checkList = board.getRoommateCheckList();

        given(roommateBoardRepository.findById(10L)).willReturn(Optional.of(board));
        given(roommateMatchingRepository.existsBySenderAndStatus(any(User.class), eq(MatchingStatus.COMPLETED)))
                .willReturn(false);
        given(roommateMatchingRepository.existsByReceiverAndStatus(any(User.class), eq(MatchingStatus.COMPLETED)))
                .willReturn(false);

        // when
        ResponseRoommatePostDto result = roommateService.updateRoommateChecklistAndBoard(
                mock(RequestRoommateFormDto.class), 10L, 1L, mock(HttpServletRequest.class));

        // then
        then(roommateBoardRepository).should().findById(10L);
        then(roommateBoardRepository).should(never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("CustomException 발생 — AC-5 BR-710 수정 시 소유자가 아닌 유저 403")
    void should_throw_CustomException_when_non_owner_updates_board() {
        // given
        User owner = buildMockUser(1L);
        User nonOwner = buildMockUser(2L);
        RoommateBoard board = buildMockBoard(10L, owner);

        given(roommateBoardRepository.findById(10L)).willReturn(Optional.of(board));

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.updateRoommateChecklistAndBoard(
                        mock(RequestRoommateFormDto.class), 10L, 2L, mock(HttpServletRequest.class));

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOMMATE_UPDATE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-4 수정 시 boardId에 해당하는 게시글 없음 404")
    void should_throw_CustomException_when_board_not_found_on_update() {
        // given
        given(roommateBoardRepository.findById(999L)).willReturn(Optional.empty());

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.updateRoommateChecklistAndBoard(
                        mock(RequestRoommateFormDto.class), 999L, 1L, mock(HttpServletRequest.class));

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
    }

    @Test
    @DisplayName("정상 삭제 — AC-6 deleteRoommateBoard boardId로 게시글 조회 후 삭제")
    void should_find_board_by_boardId_when_deleting() {
        // given
        User user = buildMockUser(1L);
        RoommateBoard board = buildMockBoard(10L, user);
        RoommateCheckList checkList = board.getRoommateCheckList();
        when(checkList.getId()).thenReturn(20L);

        given(roommateBoardRepository.findById(10L)).willReturn(Optional.of(board));

        // when
        roommateService.deleteRoommateBoard(10L, 1L);

        // then
        then(roommateBoardRepository).should().findById(10L);
        then(roommateBoardRepository).should(never()).findByUserId(anyLong());
        then(roommateBoardRepository).should().delete(board);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-6 삭제 시 소유자가 아닌 유저 403")
    void should_throw_CustomException_when_non_owner_deletes_board() {
        // given
        User owner = buildMockUser(1L);
        RoommateBoard board = buildMockBoard(10L, owner);

        given(roommateBoardRepository.findById(10L)).willReturn(Optional.of(board));

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.deleteRoommateBoard(10L, 2L);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOMMATE_DELETE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-6 삭제 시 boardId 없음 404")
    void should_throw_CustomException_when_board_not_found_on_delete() {
        // given
        given(roommateBoardRepository.findById(999L)).willReturn(Optional.empty());

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.deleteRoommateBoard(999L, 1L);

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
    }

    @Test
    @DisplayName("CustomException 발생 — AC-8 BR-710 현재 학기에 내 게시글이 없는데 유사도 조회 시 404")
    void should_throw_CustomException_when_my_board_not_found_in_current_semester_on_similar() {
        // given
        given(roommateBoardRepository.findByUserIdAndYearAndSemester(1L, 2026, SemesterType.FIRST))
                .willReturn(Optional.empty());

        // when
        org.assertj.core.api.ThrowableAssert.ThrowingCallable action =
                () -> roommateService.getSimilarRoommateBoards(1L, mock(HttpServletRequest.class));

        // then
        assertThatThrownBy(action)
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.ROOMMATE_BOARD_NOT_FOUND);
    }
}

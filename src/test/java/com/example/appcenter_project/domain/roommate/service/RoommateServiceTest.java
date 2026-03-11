package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.roommate.dto.request.RequestRoommateFormDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommateCheckListDto;
import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardLikeRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateCheckListRepository;
import com.example.appcenter_project.domain.roommate.repository.RoommateMatchingRepository;
import com.example.appcenter_project.domain.notification.service.RoommateNotificationService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoommateServiceTest {

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
    ImageService imageService;

    @Mock
    RoommateNotificationService roommateNotificationService;

    @InjectMocks
    RoommateService roommateService;

    private RoommateBoard buildMockBoard(Long id, User user) {
        RoommateBoard board = mock(RoommateBoard.class);
        RoommateCheckList cl = buildMockCheckList();
        when(board.getId()).thenReturn(id);
        when(board.getUser()).thenReturn(user);
        when(board.getRoommateCheckList()).thenReturn(cl);
        when(board.getRoommateBoardLike()).thenReturn(0);
        when(board.getRoommateBoardLikeList()).thenReturn(new ArrayList<>());
        when(board.getCreatedDate()).thenReturn(LocalDateTime.now());
        return board;
    }

    private RoommateCheckList buildMockCheckList() {
        RoommateCheckList cl = mock(RoommateCheckList.class);
        when(cl.getTitle()).thenReturn("룸메이트 찾아요");
        when(cl.getDormPeriod()).thenReturn(Collections.emptySet());
        when(cl.getMbti()).thenReturn("INFJ");
        when(cl.getUser()).thenReturn(mock(User.class));
        return cl;
    }

    private User buildMockUser(Long id) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn("홍길동");
        return user;
    }

    @Test
    @DisplayName("룸메이트 게시글 생성 - 정상 생성")
    void createRoommateCheckListandBoard_정상_생성() {
        User mockUser = buildMockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RequestRoommateFormDto dto = mock(RequestRoommateFormDto.class);
        when(dto.getTitle()).thenReturn("룸메이트 찾아요");
        when(dto.getDormPeriod()).thenReturn(null);

        RoommateCheckList savedCheckList = buildMockCheckList();
        when(roommateCheckListRepository.save(any(RoommateCheckList.class))).thenReturn(savedCheckList);

        RoommateBoard savedBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.save(any(RoommateBoard.class))).thenReturn(savedBoard);

        ResponseRoommatePostDto result = roommateService.createRoommateCheckListandBoard(dto, 1L);

        assertThat(result).isNotNull();
        verify(roommateCheckListRepository).save(any(RoommateCheckList.class));
        verify(roommateBoardRepository).save(any(RoommateBoard.class));
        verify(roommateNotificationService).sendFilteredNotifications(any(RoommateBoard.class));
    }

    @Test
    @DisplayName("룸메이트 게시글 생성 - 유저 없으면 예외")
    void createRoommateCheckListandBoard_유저없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                roommateService.createRoommateCheckListandBoard(mock(RequestRoommateFormDto.class), 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_USER_NOT_FOUND);
    }

    @Test
    @DisplayName("룸메이트 게시글 목록 조회 - 정상 반환")
    void getRoommateBoardList_정상_반환() {
        User mockUser = buildMockUser(1L);
        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);

        when(roommateBoardRepository.findAllByOrderByCreatedDateDesc()).thenReturn(List.of(mockBoard));
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateMatchingRepository.existsBySenderAndStatus(any(User.class), eq(MatchingStatus.COMPLETED)))
                .thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(any(User.class), eq(MatchingStatus.COMPLETED)))
                .thenReturn(false);

        List<ResponseRoommatePostDto> result = roommateService.getRoommateBoardList(null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("룸메이트 게시글 목록 조회 - 게시글 없으면 예외")
    void getRoommateBoardList_게시글없으면_예외() {
        when(roommateBoardRepository.findAllByOrderByCreatedDateDesc()).thenReturn(List.of());

        assertThatThrownBy(() -> roommateService.getRoommateBoardList(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_BOARD_NOT_FOUND);
    }

    @Test
    @DisplayName("룸메이트 게시글 단일 조회 - 정상 반환")
    void getRoommateBoardDetail_정상_반환() {
        User mockUser = buildMockUser(1L);
        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(mockBoard.isMatched()).thenReturn(false);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateMatchingRepository.existsBySenderAndStatus(any(User.class), eq(MatchingStatus.COMPLETED)))
                .thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(any(User.class), eq(MatchingStatus.COMPLETED)))
                .thenReturn(false);

        ResponseRoommatePostDto result = roommateService.getRoommateBoardDetail(1L, null);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("룸메이트 게시글 단일 조회 - 없으면 예외")
    void getRoommateBoardDetail_없으면_예외() {
        when(roommateBoardRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateService.getRoommateBoardDetail(99L, null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_BOARD_NOT_FOUND);
    }

    @Test
    @DisplayName("룸메이트 좋아요 추가 - 정상 추가")
    void likePlusRoommateBoard_정상_추가() {
        User mockUser = buildMockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateBoardLikeRepository.existsByUserAndRoommateBoard(mockUser, mockBoard)).thenReturn(false);
        when(mockBoard.plusLike()).thenReturn(1);

        Integer result = roommateService.likePlusRoommateBoard(1L, 1L);

        assertThat(result).isEqualTo(1);
        verify(roommateBoardLikeRepository).save(any(RoommateBoardLike.class));
    }

    @Test
    @DisplayName("룸메이트 좋아요 추가 - 이미 좋아요 누른 경우 예외")
    void likePlusRoommateBoard_이미좋아요_예외() {
        User mockUser = buildMockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateBoardLikeRepository.existsByUserAndRoommateBoard(mockUser, mockBoard)).thenReturn(true);

        assertThatThrownBy(() -> roommateService.likePlusRoommateBoard(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ALREADY_ROOMMATE_BOARD_LIKE_USER);
    }

    @Test
    @DisplayName("룸메이트 좋아요 취소 - 정상 취소")
    void likeMinusRoommateBoard_정상_취소() {
        User mockUser = buildMockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateBoardLikeRepository.existsByUserAndRoommateBoard(mockUser, mockBoard)).thenReturn(true);

        RoommateBoardLike mockLike = mock(RoommateBoardLike.class);
        when(roommateBoardLikeRepository.findByUserAndRoommateBoard(mockUser, mockBoard))
                .thenReturn(Optional.of(mockLike));
        when(mockBoard.minusLike()).thenReturn(0);

        Integer result = roommateService.likeMinusRoommateBoard(1L, 1L);

        assertThat(result).isEqualTo(0);
        verify(roommateBoardLikeRepository).delete(mockLike);
    }

    @Test
    @DisplayName("룸메이트 좋아요 취소 - 좋아요 없으면 예외")
    void likeMinusRoommateBoard_좋아요없으면_예외() {
        User mockUser = buildMockUser(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateBoardLikeRepository.existsByUserAndRoommateBoard(mockUser, mockBoard)).thenReturn(false);

        assertThatThrownBy(() -> roommateService.likeMinusRoommateBoard(1L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_BOARD_LIKE_NOT_FOUND);
    }

    @Test
    @DisplayName("내 룸메이트 체크리스트 조회 - 정상 반환")
    void getMyRoommateCheckList_정상_반환() {
        RoommateCheckList mockCheckList = buildMockCheckList();
        when(roommateCheckListRepository.findByUserId(1L)).thenReturn(Optional.of(mockCheckList));

        ResponseRoommateCheckListDto result = roommateService.getMyRoommateCheckList(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("내 룸메이트 체크리스트 조회 - 없으면 예외")
    void getMyRoommateCheckList_없으면_예외() {
        when(roommateCheckListRepository.findByUserId(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roommateService.getMyRoommateCheckList(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ROOMMATE_CHECKLIST_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글 소유자 매칭 여부 확인 - 매칭 안됨")
    void isRoommateBoardOwnerMatched_매칭안됨() {
        User mockUser = buildMockUser(1L);
        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateMatchingRepository.existsBySenderAndStatus(mockUser, MatchingStatus.COMPLETED)).thenReturn(false);
        when(roommateMatchingRepository.existsByReceiverAndStatus(mockUser, MatchingStatus.COMPLETED)).thenReturn(false);

        boolean result = roommateService.isRoommateBoardOwnerMatched(1L);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("게시글 소유자 매칭 여부 확인 - 매칭됨")
    void isRoommateBoardOwnerMatched_매칭됨() {
        User mockUser = buildMockUser(1L);
        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(roommateMatchingRepository.existsBySenderAndStatus(mockUser, MatchingStatus.COMPLETED)).thenReturn(true);

        boolean result = roommateService.isRoommateBoardOwnerMatched(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("좋아요 여부 확인 - 좋아요 누름")
    void isRoommateBoardLikedByUser_좋아요누름() {
        User mockUser = buildMockUser(1L);
        RoommateBoard mockBoard = buildMockBoard(1L, mockUser);
        when(roommateBoardRepository.findById(1L)).thenReturn(Optional.of(mockBoard));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(roommateBoardLikeRepository.existsByUserAndRoommateBoard(mockUser, mockBoard)).thenReturn(true);

        boolean result = roommateService.isRoommateBoardLikedByUser(1L, 1L);

        assertThat(result).isTrue();
    }
}
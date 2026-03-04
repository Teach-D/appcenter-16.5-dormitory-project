package com.example.appcenter_project.domain.roommate.service;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoard;
import com.example.appcenter_project.domain.roommate.entity.RoommateBoardLike;
import com.example.appcenter_project.domain.roommate.entity.RoommateCheckList;
import com.example.appcenter_project.domain.roommate.repository.RoommateBoardLikeRepository;
import com.example.appcenter_project.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoommateQueryServiceTest {

    @Mock
    RoommateBoardLikeRepository roommateBoardLikeRepository;

    @InjectMocks
    RoommateQueryService roommateQueryService;

    @Test
    @DisplayName("좋아요한 게시글 조회 - 정상 반환")
    void findLikedByUser_정상_반환() {
        RoommateCheckList mockCheckList = mock(RoommateCheckList.class);
        when(mockCheckList.getTitle()).thenReturn("룸메이트 찾아요");
        when(mockCheckList.getDormPeriod()).thenReturn(Collections.emptySet());
        when(mockCheckList.getMbti()).thenReturn("INFJ");

        User mockWriter = mock(User.class);
        when(mockWriter.getId()).thenReturn(2L);
        when(mockWriter.getName()).thenReturn("작성자");

        RoommateBoard mockBoard = mock(RoommateBoard.class);
        when(mockBoard.getId()).thenReturn(1L);
        when(mockBoard.getUser()).thenReturn(mockWriter);
        when(mockBoard.getRoommateCheckList()).thenReturn(mockCheckList);
        when(mockBoard.getRoommateBoardLike()).thenReturn(3);
        when(mockBoard.isMatched()).thenReturn(false);
        when(mockBoard.getCreatedDate()).thenReturn(LocalDateTime.now());

        RoommateBoardLike mockLike = mock(RoommateBoardLike.class);
        when(mockLike.getRoommateBoard()).thenReturn(mockBoard);

        when(roommateBoardLikeRepository.findByUserIdWithRoommateBoardAndRoommateCheckListAndUser(1L))
                .thenReturn(List.of(mockLike));

        List<ResponseRoommatePostDto> result = roommateQueryService.findLikedByUser(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBoardId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("좋아요한 게시글 조회 - 없으면 빈 리스트 반환")
    void findLikedByUser_없으면_빈리스트() {
        when(roommateBoardLikeRepository.findByUserIdWithRoommateBoardAndRoommateCheckListAndUser(99L))
                .thenReturn(List.of());

        List<ResponseRoommatePostDto> result = roommateQueryService.findLikedByUser(99L);

        assertThat(result).isEmpty();
    }
}
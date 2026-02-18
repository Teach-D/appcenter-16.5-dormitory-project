package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatRoomDetailDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderChatRoom;
import com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderChatRoomRepository;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.groupOrder.repository.UserGroupOrderChatRoomRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.example.appcenter_project.global.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GroupOrderChatRoomServiceTest {

    @Mock private GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    @Mock private UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;
    @Mock private GroupOrderRepository groupOrderRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private GroupOrderChatRoomService groupOrderChatRoomService;

    // ===== joinChatRoom =====

    @Test
    @DisplayName("채팅방 참여 - 정상 가입")
    void joinChatRoom_정상_가입() {
        User mockUser = mock(User.class);
        GroupOrderChatRoom mockChatRoom = mock(GroupOrderChatRoom.class);
        GroupOrder mockGroupOrder = mock(GroupOrder.class);

        when(mockUser.getUserGroupOrderChatRoomList()).thenReturn(new ArrayList<>());
        when(mockChatRoom.getUserGroupOrderChatRoomList()).thenReturn(new ArrayList<>());
        when(mockGroupOrder.getGroupOrderChatRoom()).thenReturn(mockChatRoom);

        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(mockGroupOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        groupOrderChatRoomService.joinChatRoom(1L, 1L);

        verify(userGroupOrderChatRoomRepository).save(any(UserGroupOrderChatRoom.class));
        assertThat(mockChatRoom.getUserGroupOrderChatRoomList()).hasSize(1);
        assertThat(mockUser.getUserGroupOrderChatRoomList()).hasSize(1);
    }

    @Test
    @DisplayName("채팅방 참여 - 공동구매 없으면 예외")
    void joinChatRoom_공동구매_없으면_예외() {
        when(groupOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.joinChatRoom(1L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_NOT_FOUND);
    }

    @Test
    @DisplayName("채팅방 참여 - 유저 없으면 예외")
    void joinChatRoom_유저_없으면_예외() {
        GroupOrderChatRoom mockChatRoom = mock(GroupOrderChatRoom.class);
        GroupOrder mockGroupOrder = mock(GroupOrder.class);
        when(mockGroupOrder.getGroupOrderChatRoom()).thenReturn(mockChatRoom);

        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(mockGroupOrder));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.joinChatRoom(99L, 1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    // ===== leaveChatRoom =====

    @Test
    @DisplayName("채팅방 퇴장 - 정상 퇴장 및 리스트에서 제거")
    void leaveChatRoom_정상_퇴장() {
        UserGroupOrderChatRoom mockUserChatRoom = mock(UserGroupOrderChatRoom.class);
        User mockUser = mock(User.class);
        GroupOrderChatRoom mockChatRoom = mock(GroupOrderChatRoom.class);

        List<UserGroupOrderChatRoom> userChatRooms = new ArrayList<>();
        userChatRooms.add(mockUserChatRoom);
        when(mockUser.getUserGroupOrderChatRoomList()).thenReturn(userChatRooms);

        when(groupOrderChatRoomRepository.findById(10L)).thenReturn(Optional.of(mockChatRoom));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userGroupOrderChatRoomRepository
                .findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(1L, 10L))
                .thenReturn(Optional.of(mockUserChatRoom));

        groupOrderChatRoomService.leaveChatRoom(1L, 10L);

        verify(userGroupOrderChatRoomRepository).delete(mockUserChatRoom);
        assertThat(userChatRooms).doesNotContain(mockUserChatRoom);
    }

    @Test
    @DisplayName("채팅방 퇴장 - 채팅방 없으면 예외")
    void leaveChatRoom_채팅방_없으면_예외() {
        when(groupOrderChatRoomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.leaveChatRoom(1L, 99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_CHAT_ROOM_NOT_FOUND);
    }

    @Test
    @DisplayName("채팅방 퇴장 - 참여 정보 없으면 예외")
    void leaveChatRoom_참여_정보_없으면_예외() {
        User mockUser = mock(User.class);
        GroupOrderChatRoom mockChatRoom = mock(GroupOrderChatRoom.class);

        when(groupOrderChatRoomRepository.findById(10L)).thenReturn(Optional.of(mockChatRoom));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        when(userGroupOrderChatRoomRepository
                .findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(1L, 10L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.leaveChatRoom(1L, 10L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_GROUP_ORDER_CHAT_ROOM_NOT_FOUND);
    }

    // ===== findGroupOrderChatRoomList =====

    @Test
    @DisplayName("채팅방 목록 조회 - 정상 반환")
    void findGroupOrderChatRoomList_정상_반환() {
        // RETURNS_DEEP_STUBS로 체인 호출(getGroupOrderChatRoom().getGroupOrder().getDeadline()) NPE 방지
        UserGroupOrderChatRoom userChatRoom = mock(UserGroupOrderChatRoom.class, RETURNS_DEEP_STUBS);
        when(userChatRoom.getModifiedDate()).thenReturn(null);
        when(userChatRoom.getChatRoomTitle()).thenReturn("테스트 채팅방");
        when(userChatRoom.getGroupOrderChatRoom().getGroupOrder().getDeadline()).thenReturn(null);

        User mockUser = mock(User.class);
        when(mockUser.getUserGroupOrderChatRoomList()).thenReturn(new ArrayList<>(List.of(userChatRoom)));
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        List<ResponseGroupOrderChatRoomDto> result = groupOrderChatRoomService.findGroupOrderChatRoomList(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("채팅방 목록 조회 - 유저 없으면 예외")
    void findGroupOrderChatRoomList_유저_없으면_예외() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.findGroupOrderChatRoomList(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", USER_NOT_FOUND);
    }

    // ===== findGroupOrderChatRoom =====

    @Test
    @DisplayName("채팅방 상세 조회 - 정상 반환")
    void findGroupOrderChatRoom_정상_반환() {
        GroupOrderChatRoom mockChatRoom = mock(GroupOrderChatRoom.class);
        GroupOrder mockGroupOrder = mock(GroupOrder.class);

        when(mockChatRoom.getGroupOrderChatList()).thenReturn(new ArrayList<>());
        when(mockGroupOrder.getId()).thenReturn(1L);

        when(groupOrderChatRoomRepository.findById(10L)).thenReturn(Optional.of(mockChatRoom));
        when(groupOrderRepository.findByGroupOrderChatRoom_id(10L)).thenReturn(Optional.of(mockGroupOrder));

        ResponseGroupOrderChatRoomDetailDto result = groupOrderChatRoomService.findGroupOrderChatRoom(10L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("채팅방 상세 조회 - 채팅방 없으면 예외")
    void findGroupOrderChatRoom_채팅방_없으면_예외() {
        when(groupOrderChatRoomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.findGroupOrderChatRoom(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_CHAT_ROOM_NOT_FOUND);
    }

    // ===== findGroupOrderChatRoomByGroupOrder =====

    @Test
    @DisplayName("공동구매로 채팅방 조회 - 정상 반환")
    void findGroupOrderChatRoomByGroupOrder_정상_반환() {
        GroupOrderChatRoom mockChatRoom = mock(GroupOrderChatRoom.class);
        GroupOrder mockGroupOrder = mock(GroupOrder.class);

        when(mockChatRoom.getGroupOrderChatList()).thenReturn(new ArrayList<>());
        when(mockGroupOrder.getId()).thenReturn(1L);
        when(mockGroupOrder.getGroupOrderChatRoom()).thenReturn(mockChatRoom);

        when(groupOrderRepository.findById(1L)).thenReturn(Optional.of(mockGroupOrder));

        ResponseGroupOrderChatRoomDetailDto result =
                groupOrderChatRoomService.findGroupOrderChatRoomByGroupOrder(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("공동구매로 채팅방 조회 - 공동구매 없으면 예외")
    void findGroupOrderChatRoomByGroupOrder_공동구매_없으면_예외() {
        when(groupOrderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> groupOrderChatRoomService.findGroupOrderChatRoomByGroupOrder(99L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", GROUP_ORDER_NOT_FOUND);
    }
}

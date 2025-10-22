package com.example.appcenter_project.domain.groupOrder.service;

import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatRoomDetailDto;
import com.example.appcenter_project.domain.groupOrder.dto.response.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrder;
import com.example.appcenter_project.domain.groupOrder.entity.GroupOrderChatRoom;
import com.example.appcenter_project.domain.groupOrder.entity.UserGroupOrderChatRoom;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderChatRoomRepository;
import com.example.appcenter_project.domain.groupOrder.repository.GroupOrderRepository;
import com.example.appcenter_project.domain.groupOrder.repository.UserGroupOrderChatRoomRepository;
import com.example.appcenter_project.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.example.appcenter_project.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderChatRoomService {

    private final GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    private final UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;
    private final GroupOrderRepository groupOrderRepository;
    private final UserRepository userRepository;

    // 유저가 채팅방에 가입하는 로직
    public void joinChatRoom(Long userId, Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        // GroupOrder에 연관관계가 있는 GroupOrderChatRoom 조회
        GroupOrderChatRoom groupOrderChatRoom = groupOrder.getGroupOrderChatRoom();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        UserGroupOrderChatRoom userGroupOrderChatRoom = UserGroupOrderChatRoom.builder().groupOrderChatRoom(groupOrderChatRoom).user(user).build();

        // OneToMany 추가
        groupOrderChatRoom.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);
        user.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);

        userGroupOrderChatRoomRepository.save(userGroupOrderChatRoom);
    }

    public void leaveChatRoom(Long userId, Long chatRoomId) {
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(chatRoomId).orElseThrow(() -> new CustomException(GROUP_ORDER_CHAT_ROOM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        UserGroupOrderChatRoom userGroupOrderChatRoom = userGroupOrderChatRoomRepository
                .findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(userId, chatRoomId).orElseThrow(() -> new CustomException(USER_GROUP_ORDER_CHAT_ROOM_NOT_FOUND));
        userGroupOrderChatRoomRepository.delete(userGroupOrderChatRoom);

        user.getUserGroupOrderChatRoomList().remove(userGroupOrderChatRoom);
    }

    public List<ResponseGroupOrderChatRoomDto> findGroupOrderChatRoomList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
        List<UserGroupOrderChatRoom> userGroupOrderChatRoomList = user.getUserGroupOrderChatRoomList();
        List<ResponseGroupOrderChatRoomDto> groupOrderChatRoomDtos = new ArrayList<>();

        // updateTime 기준 내림차순 정렬
        userGroupOrderChatRoomList.sort(
                Comparator.comparing(UserGroupOrderChatRoom::getModifiedDate, Comparator.nullsLast(Comparator.reverseOrder()))
        );
        for (UserGroupOrderChatRoom userGroupOrderChatRoom : userGroupOrderChatRoomList) {
            log.info(userGroupOrderChatRoom.getChatRoomTitle());
        }

        for (UserGroupOrderChatRoom userGroupOrderChatRoom : userGroupOrderChatRoomList) {
            ResponseGroupOrderChatRoomDto dto = ResponseGroupOrderChatRoomDto.entityToDto(userGroupOrderChatRoom);
            groupOrderChatRoomDtos.add(dto);
        }

        return groupOrderChatRoomDtos;
    }

    public ResponseGroupOrderChatRoomDetailDto findGroupOrderChatRoom(Long groupOrderChatRoomId) {
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(groupOrderChatRoomId).orElseThrow(() -> new CustomException(GROUP_ORDER_CHAT_ROOM_NOT_FOUND));
        GroupOrder groupOrder = groupOrderRepository.findByGroupOrderChatRoom_id(groupOrderChatRoomId).orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));

        // entity to dto
        return ResponseGroupOrderChatRoomDetailDto.entityToDto(groupOrderChatRoom, groupOrder);
    }

    public ResponseGroupOrderChatRoomDetailDto findGroupOrderChatRoomByGroupOrder(Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId)
                .orElseThrow(() -> new CustomException(GROUP_ORDER_NOT_FOUND));
        GroupOrderChatRoom groupOrderChatRoom = groupOrder.getGroupOrderChatRoom();

        // entity to dto
        return ResponseGroupOrderChatRoomDetailDto.entityToDto(groupOrderChatRoom, groupOrder);
    }
}
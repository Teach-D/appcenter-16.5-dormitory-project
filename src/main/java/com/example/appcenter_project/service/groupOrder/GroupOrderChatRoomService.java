package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDetailDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderRepository;
import com.example.appcenter_project.repository.groupOrder.UserGroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId).orElseThrow();

        // GroupOrder에 연관관계가 있는 GroupOrderChatRoom 조회
        GroupOrderChatRoom groupOrderChatRoom = groupOrder.getGroupOrderChatRoom();
        User user = userRepository.findById(userId).orElseThrow();

        UserGroupOrderChatRoom userGroupOrderChatRoom = UserGroupOrderChatRoom.builder().groupOrderChatRoom(groupOrderChatRoom).user(user).build();

        // OneToMany 추가
        groupOrderChatRoom.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);
        user.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);

        // GroupOrder의 currentPeople 1증가
        groupOrder.plusCurrentPeople();

        userGroupOrderChatRoomRepository.save(userGroupOrderChatRoom);
    }

    public void leaveChatRoom(Long userId, Long chatRoomId) {
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(chatRoomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        UserGroupOrderChatRoom userGroupOrderChatRoom = userGroupOrderChatRoomRepository.findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(userId, chatRoomId).orElseThrow();
        userGroupOrderChatRoomRepository.delete(userGroupOrderChatRoom);

        user.getUserGroupOrderChatRoomList().remove(userGroupOrderChatRoom);
    }

    public List<ResponseGroupOrderChatRoomDto> findGroupOrderChatRoomList(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();

        List<ResponseGroupOrderChatRoomDto> groupOrderChatRoomDtos = new ArrayList<>();

        for (UserGroupOrderChatRoom userGroupOrderChatRoom : user.getUserGroupOrderChatRoomList()) {
            ResponseGroupOrderChatRoomDto responseGroupOrderChatRoomDto = ResponseGroupOrderChatRoomDto.entityToDto(userGroupOrderChatRoom);
            groupOrderChatRoomDtos.add(responseGroupOrderChatRoomDto);
        }

        return groupOrderChatRoomDtos;
    }

    public ResponseGroupOrderChatRoomDetailDto findGroupOrderChatRoom(Long groupOrderChatRoomId) {
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(groupOrderChatRoomId).orElseThrow();
        GroupOrder groupOrder = groupOrderRepository.findByGroupOrderChatRoom_id(groupOrderChatRoomId).orElseThrow();

        // entity to dto
        return ResponseGroupOrderChatRoomDetailDto.entityToDto(groupOrderChatRoom, groupOrder);
    }

    public ResponseGroupOrderChatRoomDetailDto findGroupOrderChatRoomByGroupOrder(Long groupOrderId) {
        GroupOrder groupOrder = groupOrderRepository.findById(groupOrderId).orElseThrow();
        GroupOrderChatRoom groupOrderChatRoom = groupOrder.getGroupOrderChatRoom();

        // entity to dto
        return ResponseGroupOrderChatRoomDetailDto.entityToDto(groupOrderChatRoom, groupOrder);
    }
}

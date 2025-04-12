package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.groupOrder.UserGroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderChatRoomService {

    private final GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    private final UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;
    private final UserRepository userRepository;

    // 유저가 채팅방에 가입하는 로직
    public void joinChatRoom(Long userId, Long chatRoomId) {
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(chatRoomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        UserGroupOrderChatRoom userGroupOrderChatRoom = UserGroupOrderChatRoom.builder().groupOrderChatRoom(groupOrderChatRoom).user(user).build();

        // OneToMany 추가
        groupOrderChatRoom.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);
        user.getUserGroupOrderChatRoomList().add(userGroupOrderChatRoom);

        userGroupOrderChatRoomRepository.save(userGroupOrderChatRoom);
    }

    public void leaveChatRoom(Long userId, Long chatRoomId) {
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(chatRoomId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        UserGroupOrderChatRoom userGroupOrderChatRoom = userGroupOrderChatRoomRepository.findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(userId, chatRoomId).orElseThrow();
        userGroupOrderChatRoomRepository.delete(userGroupOrderChatRoom);

        user.getUserGroupOrderChatRoomList().remove(userGroupOrderChatRoom);
    }

    //

}

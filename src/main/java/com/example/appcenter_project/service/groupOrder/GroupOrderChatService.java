package com.example.appcenter_project.service.groupOrder;

import com.example.appcenter_project.dto.request.groupOrder.RequestGroupOrderChatDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatDto;
import com.example.appcenter_project.dto.response.groupOrder.ResponseGroupOrderChatRoomDto;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChat;
import com.example.appcenter_project.entity.groupOrder.GroupOrderChatRoom;
import com.example.appcenter_project.entity.groupOrder.UserGroupOrderChatRoom;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRepository;
import com.example.appcenter_project.repository.groupOrder.GroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.groupOrder.UserGroupOrderChatRoomRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.config.WebSocketEventListener.groupOrderChatRoomInUserMap;
import static com.example.appcenter_project.config.WebSocketEventListener.chatRoomListInUserMap;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupOrderChatService {

    private final GroupOrderChatRepository groupOrderChatRepository;
    private final UserRepository userRepository;
    private final GroupOrderChatRoomRepository groupOrderChatRoomRepository;
    private final UserGroupOrderChatRoomRepository userGroupOrderChatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ResponseGroupOrderChatDto sendGroupOrderChat(Long userId, RequestGroupOrderChatDto requestGroupOrderChatDto) {
        User user = userRepository.findById(userId).orElseThrow();
        GroupOrderChatRoom groupOrderChatRoom = groupOrderChatRoomRepository.findById(requestGroupOrderChatDto.getGroupOrderChatRomId()).orElseThrow();

        // 채팅방에 가입한 유저
        List<User> chatRoomAllUser = new ArrayList<>();
        for (UserGroupOrderChatRoom userGroupOrderChatRoom : groupOrderChatRoom.getUserGroupOrderChatRoomList()) {
            User chatRoomUser = userGroupOrderChatRoom.getUser();
            chatRoomAllUser.add(chatRoomUser);
        }

        // 현재 채팅방에 입장한 유저
        List<String> userInChatRoom = groupOrderChatRoomInUserMap.get(String.valueOf(groupOrderChatRoom.getId()));

        List<Long> unreadUser = new ArrayList<>();

        // 채팅방 가입 전체 인원중에서 현재 채팅방에 입장하지 않은 유저 선택 - unreaduser
        for (User userJoinChatRoom : chatRoomAllUser) {
            if (!userInChatRoom.contains(String.valueOf(userJoinChatRoom.getId()))) {
                unreadUser.add(userJoinChatRoom.getId());
            }
        }

        // entity to dto
        GroupOrderChat groupOrderChat = GroupOrderChat.builder().groupOrderChatRoom(groupOrderChatRoom).content(requestGroupOrderChatDto.getContent())
                .user(user).unreadUser(unreadUser).build();

        // 채팅 db에 저장
        GroupOrderChat saveGroupOrderChat = groupOrderChatRepository.save(groupOrderChat);

        // 채팅을 보냈을 때 채팅방에 가입되어 있는 유저의 채팅방 정보 변경
        // 1. 채팅방에 입장한 유저의 경우-읽지 않음 수 그대로
        List<User> readUsers = chatRoomAllUser.stream()
                .filter(joinUser -> !unreadUser.contains(joinUser.getId()))
                .toList();

        for (User readUser : readUsers) {
            UserGroupOrderChatRoom userGroupOrderChatRoom = userGroupOrderChatRoomRepository.findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(readUser.getId(), groupOrderChatRoom.getId()).orElseThrow();
            userGroupOrderChatRoom.update(groupOrderChatRoom.getTitle(), groupOrderChat.getContent(), userGroupOrderChatRoom.getUnreadCount(), LocalDateTime.now());
        }

        // 2. 채팅방에 입장하지 않은 유저의 경우-읽지 않음 수 + 1
        List<User> unreadUsers = chatRoomAllUser.stream()
                .filter(joinUser -> unreadUser.contains(joinUser.getId()))
                .toList();

        for (User readUser : readUsers) {
            UserGroupOrderChatRoom userGroupOrderChatRoom = userGroupOrderChatRoomRepository.findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(readUser.getId(), groupOrderChatRoom.getId()).orElseThrow();
            userGroupOrderChatRoom.update(groupOrderChatRoom.getTitle(), groupOrderChat.getContent(), userGroupOrderChatRoom.getUnreadCount() + 1, LocalDateTime.now());
        }

        // 채팅방 목록에 입장해 있는 유저에게 채팅방의 안읽은 메시지 실시간 전송
        List<User> userInChatRoomList = chatRoomAllUser.stream()
                .filter(joinUser -> chatRoomListInUserMap.containsValue(String.valueOf(joinUser.getId())))
                .toList();

        for (User sendUser : userInChatRoomList) {
            UserGroupOrderChatRoom userGroupOrderChatRoom = userGroupOrderChatRoomRepository.findUserGroupOrderChatRoomByUser_IdAndGroupOrderChatRoom_Id(sendUser.getId(), groupOrderChatRoom.getId()).orElseThrow();

            // 채팅방 목록에 입장한 유저에게 가장 최신 채팅의 정보 전송
            ResponseGroupOrderChatRoomDto responseGroupOrderChatRoomDto = ResponseGroupOrderChatRoomDto.builder().chatRoomId(groupOrderChatRoom.getId()).chatRoomTitle(groupOrderChatRoom.getTitle())
                    .recentChatContent(userGroupOrderChatRoom.getRecentChatContent()).unreadCount(userGroupOrderChatRoom.getUnreadCount())
                    .recentChatTime(userGroupOrderChatRoom.getUpdateTime()).build();
            messagingTemplate.convertAndSend("/sub/chatRoomList/chatRoom/" + groupOrderChatRoom.getId() + "/user/" + sendUser.getId(), responseGroupOrderChatRoomDto);
        }

        return ResponseGroupOrderChatDto.builder()
                .groupOrderChatId(saveGroupOrderChat.getId())
                .groupOrderChatRoomId(groupOrderChatRoom.getId())
                .content(requestGroupOrderChatDto.getContent())
                .unreadUserCount(unreadUser.size())
                .build();

    }
}

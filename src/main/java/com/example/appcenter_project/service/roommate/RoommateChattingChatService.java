package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateChatDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateChatDto;
import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.roommate.RoommateChattingChatRepository;
import com.example.appcenter_project.repository.roommate.RoommateChattingRoomRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class RoommateChattingChatService {

    private final RoommateChattingChatRepository chatRepository;
    private final RoommateChattingRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ResponseRoommateChatDto sendChat(Long userId, RequestRoommateChatDto request) {
        // 1. 보낸 사람 조회 (예외 처리 포함)
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 2. 채팅방 조회
        RoommateChattingRoom room = chatRoomRepository.findById(request.getRoommateChattingRoomId())
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        // 3. 보낸 사람 → 수신자 확인 및 보안 검증
        User receiver;
        if (room.getGuest().getId().equals(userId)) {
            receiver = room.getHost(); // 내가 요청자면 상대는 게시글 작성자
        } else if (room.getHost().getId().equals(userId)) {
            receiver = room.getGuest(); // 내가 작성자면 상대는 요청자
        } else {
            throw new CustomException(ROOMMATE_CHAT_ROOM_FORBIDDEN); // 해당 채팅방 소속이 아님
        }

        // 4. 수신자가 현재 WebSocket 방에 접속해 있는지 여부는 외부에서 처리되므로, 여기선 false로 기본 설정
        boolean read = false;

        // 5. 채팅 메시지 엔티티 생성
        RoommateChattingChat chat = RoommateChattingChat.builder()
                .roommateChattingRoom(room)
                .member(sender)
                .content(request.getContent())
                .readByReceiver(read)
                .build();

        // 6. DB에 저장
        RoommateChattingChat savedChat = chatRepository.save(chat);

        // 7. 실시간 전송 (수신자 ID가 명확하지 않아 room 단위로 전송)
        messagingTemplate.convertAndSend(
                "/sub/roommate/chat/" + room.getId(),
                ResponseRoommateChatDto.entityToDto(savedChat)
        );

        return ResponseRoommateChatDto.entityToDto(savedChat);
    }

    public void markAsRead(Long roomId, Long userId) {
        RoommateChattingRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 내가 보낸 메시지를 제외하고, 읽지 않은 메시지들을 모두 읽음 처리
        chatRepository.findByRoommateChattingRoomAndMemberNotAndReadByReceiverFalse(room, me)
                .forEach(RoommateChattingChat::markAsRead);
    }

    @Transactional(readOnly = true)
    public List<ResponseRoommateChatDto> getChatList(Long userId, Long roomId) {
        RoommateChattingRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 접근 권한 확인
        if (!room.getGuest().getId().equals(userId) && !room.getHost().getId().equals(userId)) {
            throw new CustomException(ROOMMATE_CHAT_ROOM_FORBIDDEN); // 이 채팅방에 속하지 않은 사용자입니다.
        }

        // 채팅 내역 조회
        List<RoommateChattingChat> chatList = chatRepository.findByRoommateChattingRoom(room);

        // 안 읽은 메시지 읽음 처리 (내가 보낸 거 제외)
        chatList.stream()
                .filter(chat -> !chat.getMember().getId().equals(userId) && !chat.isReadByReceiver())
                .forEach(RoommateChattingChat::markAsRead);

        return chatList.stream()
                .map(ResponseRoommateChatDto::entityToDto)
                .toList();
    }


}

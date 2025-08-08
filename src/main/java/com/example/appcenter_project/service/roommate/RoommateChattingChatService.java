package com.example.appcenter_project.service.roommate;

import com.example.appcenter_project.dto.request.roommate.RequestRoommateChatDto;
import com.example.appcenter_project.dto.response.roommate.ResponseRoommateChatDto;
import com.example.appcenter_project.dto.response.roommate.RoommateChatHistoryDto;
import com.example.appcenter_project.dto.response.roommate.RoommateChatRoomDetailDto;
import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.exception.CustomException;
import com.example.appcenter_project.repository.roommate.RoommateChattingChatRepository;
import com.example.appcenter_project.repository.roommate.RoommateChattingRoomRepository;
import com.example.appcenter_project.repository.user.UserRepository;
import com.example.appcenter_project.service.image.ImageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.example.appcenter_project.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoommateChattingChatService {

    private final RoommateChattingChatRepository chatRepository;
    private final RoommateChattingRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ImageService imageService;

    public ResponseRoommateChatDto sendChat(Long userId, RequestRoommateChatDto request) {
        log.info("💬 [채팅 전송 시작] userId: {}, roomId: {}, content: {}",
                userId, request.getRoommateChattingRoomId(), request.getContent());

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

        log.info("👥 [채팅방 참여자] 발신자: {} ({}), 수신자: {} ({})",
                sender.getId(), sender.getStudentNumber(),
                receiver.getId(), receiver.getStudentNumber());

        // 4. 수신자가 현재 WebSocket 방에 접속해 있는지 확인
        boolean isReceiverOnline = isUserOnlineInRoom(request.getRoommateChattingRoomId(), receiver.getId());
        log.info("🔍 [수신자 온라인 상태] receiverId: {}, isOnline: {}", receiver.getId(), isReceiverOnline);

        // 5. 채팅 메시지 엔티티 생성 (수신자가 온라인이면 자동으로 읽음 처리)
        RoommateChattingChat chat = RoommateChattingChat.builder()
                .roommateChattingRoom(room)
                .member(sender)
                .content(request.getContent())
                .readByReceiver(isReceiverOnline) // 수신자가 온라인이면 읽음 처리
                .build();

        // 6. DB에 저장
        RoommateChattingChat savedChat = chatRepository.save(chat);
        log.info("💾 [채팅 DB 저장 완료] chatId: {}, read: {}", savedChat.getId(), savedChat.isReadByReceiver());

        // 7. 실시간 전송 (수신자 ID가 명확하지 않아 room 단위로 전송)
        ResponseRoommateChatDto responseDto = ResponseRoommateChatDto.entityToDto(savedChat);
        String destination = "/sub/roommate/chat/" + room.getId();

        log.info("📡 [WebSocket 전송] destination: {}, chatId: {}", destination, savedChat.getId());
        messagingTemplate.convertAndSend(destination, responseDto);

        // 8. 수신자가 온라인이고 자동으로 읽음 처리된 경우, 읽음 알림 전송
        if (isReceiverOnline) {
            String readDestination = "/sub/roommate/chat/read/" + room.getId() + "/user/" + sender.getId();
            List<Long> readIds = List.of(savedChat.getId());
            log.info("📖 [자동 읽음 처리 알림] destination: {}, readIds: {}", readDestination, readIds);
            messagingTemplate.convertAndSend(readDestination, readIds);
        }

        return responseDto;
    }

    public void markAsRead(Long roomId, Long userId) {
        RoommateChattingRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 내가 보낸 메시지를 제외하고, 읽지 않은 메시지들을 모두 읽음 처리
        List<RoommateChattingChat> unreadMessages = chatRepository.findByRoommateChattingRoomAndMemberNotAndReadByReceiverFalse(room, me);

        List<Long> readIds = new ArrayList<>();
        unreadMessages.forEach(chat -> {
            chat.markAsRead();
            readIds.add(chat.getId());
        });

        // 읽음 처리된 메시지 ID들을 실시간으로 전송
        if (!readIds.isEmpty()) {
            String destination = "/sub/roommate/chat/read/" + roomId + "/user/" + userId;
            log.info("📖 [실시간 읽음 처리] destination: {}, readIds: {}", destination, readIds);
            messagingTemplate.convertAndSend(destination, readIds);
        }
    }

    // 사용자가 특정 채팅방에 온라인 상태인지 확인하는 메서드
    private boolean isUserOnlineInRoom(Long roomId, Long userId) {
        // RoommateWebSocketEventListener의 static 맵을 참조
        List<String> onlineUsers = com.example.appcenter_project.config.RoommateWebSocketEventListener.roommateChatRoomInUserMap.get(roomId.toString());

        if (onlineUsers == null) {
            return false;
        }

        boolean isOnline = onlineUsers.contains(userId.toString());
        log.debug("🔍 [사용자 온라인 상태 확인] roomId: {}, userId: {}, onlineUsers: {}, isOnline: {}",
                roomId, userId, onlineUsers, isOnline);

        return isOnline;
    }

    @Transactional(readOnly = true)
    public RoommateChatRoomDetailDto getRoomDetail(Long userId, Long roomId, HttpServletRequest request) {
        RoommateChattingRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ROOMMATE_CHAT_ROOM_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));

        // 접근 권한 확인
        if (!room.getGuest().getId().equals(userId) && !room.getHost().getId().equals(userId)) {
            throw new CustomException(ROOMMATE_CHAT_ROOM_FORBIDDEN);
        }

        // 1. 상대방 추출
        User partner = room.getHost().getId().equals(userId) ? room.getGuest() : room.getHost();

        String partnerProfileImageUrl = null;
        try {
            partnerProfileImageUrl = imageService.findUserImageUrlByUserId(partner.getId(), request).getFileName();
        } catch (Exception e) {
            // 이미지 없을 경우 null
        }

        // 2. 채팅 내역 조회
        List<RoommateChattingChat> chatList = chatRepository.findByRoommateChattingRoom(room);

        // 3. 안 읽은 메시지 읽음 처리 (내가 보낸 거 제외)
        List<Long> readIds = new ArrayList<>();
        chatList.stream()
                .filter(chat -> !chat.getMember().getId().equals(userId) && !chat.isReadByReceiver())
                .forEach(chat -> {
                    chat.markAsRead();
                    readIds.add(chat.getId());
                });
        if (!readIds.isEmpty()) {
            String destination = "/sub/roommate/chat/read/" + roomId + "/user/" + userId;
            log.info("📖 [채팅 조회 시 읽음 처리] destination: {}, readIds: {}", destination, readIds);
            messagingTemplate.convertAndSend(destination, readIds);
        }

        // 4. 채팅 내역 DTO 변환
        List<RoommateChatHistoryDto> chatHistory = chatList.stream()
                .map(chat -> {
                    String profileImageUrl = null;
                    try {
                        profileImageUrl = imageService.findUserImageUrlByUserId(chat.getMember().getId(), request).getFileName();
                    } catch (Exception e) {}
                    return RoommateChatHistoryDto.builder()
                            .roommateChattingRoomId(chat.getRoommateChattingRoom().getId())
                            .roommateChatId(chat.getId())
                            .userId(chat.getMember().getId())
                            .content(chat.getContent())
                            .read(chat.isReadByReceiver())
                            .createdDate(chat.getCreatedDate().toString())
                            .profileImageUrl(profileImageUrl)
                            .build();
                })
                .toList();

        // 5. 최종 통합 DTO 반환!
        return RoommateChatRoomDetailDto.builder()
                .roomId(roomId)
                .partnerId(partner.getId())
                .partnerName(partner.getName())
                .partnerProfileImageUrl(partnerProfileImageUrl)
                .chatList(chatHistory)
                .build();
    }
}

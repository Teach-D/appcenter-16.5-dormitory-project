package com.example.appcenter_project.config;

import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.repository.roommate.RoommateChattingChatRepository;
import com.example.appcenter_project.repository.roommate.RoommateChattingRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoommateWebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    private final RoommateChattingRoomRepository chatRoomRepository;
    private final RoommateChattingChatRepository chatRepository;

    // 세션 → 채팅방 ID, 유저 ID
    public static final Map<String, String> roommateChatRoomMap = new ConcurrentHashMap<>();
    public static final Map<String, String> roommateChatRoomUserMap = new ConcurrentHashMap<>();
    public static final Map<String, List<String>> roommateChatRoomInUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("Roommate WebSocket 연결됨. sessionId: {}, userId: {}", accessor.getSessionId(),
                accessor.getFirstNativeHeader("userId"));
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/sub/roommate/chat/")) {
            // ex) /sub/roommate/chat/{roomId}/user/{userId}
            String[] parts = destination.split("/");
            String roomId = parts[3];
            String userId = parts[5];

            // 세션에 유저 입장 정보 저장
            roommateChatRoomMap.put(sessionId, roomId);
            roommateChatRoomUserMap.put(sessionId, userId);

            // 채팅방 입장 유저 관리
            roommateChatRoomInUserMap
                    .computeIfAbsent(roomId, k -> new ArrayList<>())
                    .add(userId);

            // 읽지 않은 메시지 → 읽음 처리
            RoommateChattingRoom chatRoom = chatRoomRepository.findById(Long.parseLong(roomId))
                    .orElseThrow();

            List<RoommateChattingChat> unreadMessages =
                    chatRepository.findByRoommateChattingRoomAndReadByReceiverFalse(chatRoom);

            List<Long> readIds = new ArrayList<>();
            for (RoommateChattingChat chat : unreadMessages) {
                if (!chat.getMember().getId().toString().equals(userId)) {
                    chat.markAsRead();
                    readIds.add(chat.getId());
                }
            }

            // 읽음된 메시지 ID 전송
            messagingTemplate.convertAndSend(
                    "/sub/roommate/chat/read/" + roomId + "/user/" + userId,
                    readIds
            );
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();

        if (roommateChatRoomMap.containsKey(sessionId)) {
            String roomId = roommateChatRoomMap.get(sessionId);
            String userId = roommateChatRoomUserMap.get(sessionId);

            List<String> users = roommateChatRoomInUserMap.get(roomId);
            if (users != null) {
                users.remove(userId);
                if (users.isEmpty()) {
                    roommateChatRoomInUserMap.remove(roomId);
                }
            }

            roommateChatRoomMap.remove(sessionId);
            roommateChatRoomUserMap.remove(sessionId);

            log.info("Roommate 채팅방 퇴장: roomId={}, userId={}", roomId, userId);
        }
    }
}

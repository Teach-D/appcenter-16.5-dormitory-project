package com.example.appcenter_project.config;

import com.example.appcenter_project.entity.roommate.RoommateChattingChat;
import com.example.appcenter_project.entity.roommate.RoommateChattingRoom;
import com.example.appcenter_project.repository.roommate.RoommateChattingChatRepository;
import com.example.appcenter_project.repository.roommate.RoommateChattingRoomRepository;
import com.example.appcenter_project.security.jwt.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

    // 세션 → 채팅방 ID, 유저 ID
    public static final Map<String, String> roommateChatRoomMap = new ConcurrentHashMap<>();
    public static final Map<String, String> roommateChatRoomUserMap = new ConcurrentHashMap<>();
    public static final Map<String, List<String>> roommateChatRoomInUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        // sessionAttributes null 체크
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            log.warn("SessionAttributes is null for sessionId: {}", accessor.getSessionId());
            return;
        }

        String userId = (String) sessionAttributes.get("userId");
        log.info("Roommate WebSocket 연결됨. sessionId: {}, userId: {}",
                accessor.getSessionId(), userId);
    }

    @EventListener
    public void handleWebSocketSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();

        if (destination != null && destination.startsWith("/sub/roommate/chat/")) {
            // ex) /sub/roommate/chat/{roomId} 또는 /sub/roommate/chat/read/{roomId}/user/{userId}
            String[] parts = destination.split("/");

            log.info("구독 경로 분석: destination={}, parts={}", destination, Arrays.toString(parts));

            // 배열 길이 검증: ["", "sub", "roommate", "chat", "roomId"] 최소 5개
            if (parts.length < 5) {
                log.warn("Invalid destination format: {}", destination);
                return;
            }

            // parts[0] = "", parts[1] = "sub", parts[2] = "roommate", parts[3] = "chat", parts[4] = roomId
            String roomIdStr = parts[4];

            // roomId가 숫자인지 확인
            Long roomId;
            try {
                roomId = Long.parseLong(roomIdStr);
            } catch (NumberFormatException e) {
                log.warn("Invalid roomId format in destination: {}, roomIdStr: {}", destination, roomIdStr);
                return;
            }

            String userId = null;

            // sessionAttributes null 체크
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                userId = (String) sessionAttributes.get("userId");
            }

            // Authorization 헤더에서 JWT 토큰으로 사용자 ID 추출 (fallback)
            if (userId == null) {
                String authToken = accessor.getFirstNativeHeader("Authorization");
                if (authToken != null && authToken.startsWith("Bearer ")) {
                    try {
                        String token = authToken.substring(7); // "Bearer " 제거
                        userId = jwtTokenProvider.getUserId(token);
                    } catch (Exception e) {
                        log.warn("Failed to extract userId from JWT token: {}", e.getMessage());
                    }
                }
            }

            // parts 길이가 8 이상이고 "read"와 "user" 세그먼트가 있는 경우 (읽음 처리 구독)
            // /sub/roommate/chat/read/{roomId}/user/{userId}
            if (userId == null && parts.length >= 8 && "read".equals(parts[5]) && "user".equals(parts[6])) {
                userId = parts[7];
            }

            // userId가 없으면 처리 중단
            if (userId == null) {
                log.warn("UserId not found in destination: {} or session", destination);
                return;
            }

            log.info("WebSocket 구독: sessionId={}, roomId={}, userId={}, destination={}",
                    sessionId, roomId, userId, destination);

            // 읽음 처리 구독이 아닌 일반 채팅 구독인 경우만 입장 처리
            if (!destination.contains("/read/")) {
                // 세션에 유저 입장 정보 저장
                roommateChatRoomMap.put(sessionId, roomId.toString());
                roommateChatRoomUserMap.put(sessionId, userId);

                // 채팅방 입장 유저 관리
                roommateChatRoomInUserMap
                        .computeIfAbsent(roomId.toString(), k -> new ArrayList<>())
                        .add(userId);

                // 읽지 않은 메시지 → 읽음 처리
                try {
                    RoommateChattingRoom chatRoom = chatRoomRepository.findById(roomId)
                            .orElse(null);

                    if (chatRoom != null) {
                        List<RoommateChattingChat> unreadMessages =
                                chatRepository.findByRoommateChattingRoomAndReadByReceiverFalse(chatRoom);

                        List<Long> readIds = new ArrayList<>();
                        for (RoommateChattingChat chat : unreadMessages) {
                            // 내가 보낸 메시지가 아닌 것만 읽음 처리
                            if (!chat.getMember().getId().toString().equals(userId)) {
                                chat.markAsRead();
                                readIds.add(chat.getId());
                            }
                        }

                        // 읽음된 메시지 ID 전송 - 메시지를 보낸 사용자에게 알림
                        if (!readIds.isEmpty()) {
                            // 상대방 찾기
                            String otherUserId = chatRoom.getHost().getId().toString().equals(userId)
                                    ? chatRoom.getGuest().getId().toString()
                                    : chatRoom.getHost().getId().toString();

                            String readDestination = "/sub/roommate/chat/read/" + roomId + "/user/" + otherUserId;
                            messagingTemplate.convertAndSend(readDestination, readIds);
                            log.info("📖 [입장 시 읽음 처리] destination: {}, readIds: {}", readDestination, readIds);
                        }
                    }
                } catch (Exception e) {
                    log.error("읽음 처리 중 오류 발생: roomId={}, userId={}, error={}", roomId, userId, e.getMessage());
                }
            }
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

package com.example.appcenter_project.global.config;

import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenChatWebSocketEventListener {

    private final OpenChatMessageRepository openChatMessageRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;

    private static final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        MessageHeaders headers = event.getMessage().getHeaders();
        String destination = SimpMessageHeaderAccessor.getDestination(headers);
        if (destination == null || !destination.startsWith("/sub/openchat/")) return;

        String sessionId = SimpMessageHeaderAccessor.getSessionId(headers);

        String roomIdStr = destination.substring("/sub/openchat/".length());
        Long roomId;
        try {
            roomId = Long.parseLong(roomIdStr);
        } catch (NumberFormatException e) {
            return;
        }

        Map<String, Object> sessionAttributes = SimpMessageHeaderAccessor.getSessionAttributes(headers);
        if (sessionAttributes == null) return;
        String userIdStr = (String) sessionAttributes.get("userId");
        if (userIdStr == null) return;
        Long userId;
        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return;
        }

        sessionRoomMap.put(sessionId, roomId);
        sessionUserMap.put(sessionId, userId);

        openChatMessageRepository.findLatestMessageIdByRoomId(roomId)
                .ifPresent(latestId ->
                        openChatParticipantRepository.updateLastReadMessageId(roomId, userId, latestId));
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        sessionRoomMap.remove(sessionId);
        sessionUserMap.remove(sessionId);
    }
}

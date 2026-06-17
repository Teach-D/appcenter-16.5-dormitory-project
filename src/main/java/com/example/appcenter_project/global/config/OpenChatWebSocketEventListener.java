package com.example.appcenter_project.global.config;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatReadEventDto;
import com.example.appcenter_project.domain.openChat.repository.OpenChatMessageRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenChatWebSocketEventListener {

    private final OpenChatMessageRepository openChatMessageRepository;
    private final OpenChatParticipantRepository openChatParticipantRepository;
    private final OpenChatSessionRegistry sessionRegistry;
    private final SimpMessagingTemplate messagingTemplate;

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

        sessionRegistry.subscribe(sessionId, roomId, userId);

        openChatMessageRepository.findLatestMessageIdByRoomId(roomId)
                .ifPresent(latestId -> {
                    openChatParticipantRepository.updateLastReadMessageId(roomId, userId, latestId);
                    int unreadCount = calculateUnreadCount(roomId, latestId);
                    messagingTemplate.convertAndSend("/sub/openchat/" + roomId + "/read",
                            ResponseOpenChatReadEventDto.of(latestId, unreadCount));
                });
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        sessionRegistry.unsubscribe(event.getSessionId());
    }

    private int calculateUnreadCount(Long roomId, Long messageId) {
        long total = openChatParticipantRepository.countByRoomId(roomId);
        long readCount = openChatParticipantRepository.countReadByRoomIdAndMessageId(roomId, messageId);
        return (int) (total - readCount);
    }
}

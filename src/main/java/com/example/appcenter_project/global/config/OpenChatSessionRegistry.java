package com.example.appcenter_project.global.config;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenChatSessionRegistry {

    private final Map<String, Long> sessionRoomMap = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> roomSubscribers = new ConcurrentHashMap<>();

    public void subscribe(String sessionId, Long roomId, Long userId) {
        sessionRoomMap.put(sessionId, roomId);
        sessionUserMap.put(sessionId, userId);
        roomSubscribers.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public void unsubscribe(String sessionId) {
        Long roomId = sessionRoomMap.remove(sessionId);
        Long userId = sessionUserMap.remove(sessionId);
        if (roomId != null && userId != null) {
            Set<Long> subscribers = roomSubscribers.get(roomId);
            if (subscribers != null) {
                subscribers.remove(userId);
                if (subscribers.isEmpty()) {
                    roomSubscribers.remove(roomId);
                }
            }
        }
    }

    public Set<Long> getSubscriberUserIds(Long roomId) {
        Set<Long> subscribers = roomSubscribers.get(roomId);
        return subscribers != null ? Collections.unmodifiableSet(subscribers) : Collections.emptySet();
    }
}

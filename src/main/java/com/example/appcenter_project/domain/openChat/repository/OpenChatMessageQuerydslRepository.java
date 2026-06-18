package com.example.appcenter_project.domain.openChat.repository;

import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;

import java.util.List;
import java.util.Optional;

public interface OpenChatMessageQuerydslRepository {
    List<OpenChatMessage> findByRoomIdWithCursor(Long roomId, Long lastMessageId, int size);
    Optional<Long> findLatestMessageIdByRoomId(Long roomId);
    long countByRoomIdAndIdGreaterThan(Long roomId, Long lastReadMessageId);
    List<Long> findMessageIdsAfterInRoom(Long roomId, Long afterIdExclusive, Long toIdInclusive);
}

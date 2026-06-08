package com.example.appcenter_project.domain.openChat.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OpenChatParticipantQuerydslRepository {
    Map<Long, Long> countByRoomIds(List<Long> roomIds);
    Set<Long> findJoinedRoomIds(Long userId, List<Long> roomIds);
}

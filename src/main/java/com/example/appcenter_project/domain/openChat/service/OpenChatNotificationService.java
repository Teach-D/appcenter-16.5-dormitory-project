package com.example.appcenter_project.domain.openChat.service;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import com.example.appcenter_project.domain.openChat.dto.UnreadNotificationInfo;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.repository.OpenChatParticipantRepository;
import com.example.appcenter_project.domain.openChat.repository.OpenChatRoomRepository;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenChatNotificationService {

    private final OpenChatParticipantRepository participantRepository;
    private final OpenChatRoomRepository openChatRoomRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmOutboxRepository fcmOutboxRepository;

    @Transactional
    public void sendHourlyUnreadNotifications() {
        List<UnreadNotificationInfo> unreadInfos = participantRepository.findUnreadCountsForNotification();

        if (unreadInfos.isEmpty()) {
            return;
        }

        Set<Long> roomIds = unreadInfos.stream()
                .map(UnreadNotificationInfo::roomId)
                .collect(Collectors.toSet());
        Set<Long> userIds = unreadInfos.stream()
                .map(UnreadNotificationInfo::userId)
                .collect(Collectors.toSet());

        Map<Long, String> roomNameMap = openChatRoomRepository.findAllById(roomIds).stream()
                .collect(Collectors.toMap(OpenChatRoom::getId, OpenChatRoom::getName));

        Map<Long, String> userTokenMap = fcmTokenRepository.findAllByUserIdIn(new ArrayList<>(userIds)).stream()
                .collect(Collectors.toMap(
                        token -> token.getUser().getId(),
                        FcmToken::getToken,
                        (existing, replacement) -> existing
                ));

        List<FcmOutbox> outboxes = unreadInfos.stream()
                .filter(info -> userTokenMap.containsKey(info.userId()))
                .map(info -> {
                    String token = userTokenMap.get(info.userId());
                    String roomName = roomNameMap.getOrDefault(info.roomId(), "오픈채팅");
                    String body = "새 메시지 " + info.unreadCount() + "개";
                    return FcmOutbox.create(token, roomName, body);
                })
                .toList();

        if (!outboxes.isEmpty()) {
            fcmOutboxRepository.saveAll(outboxes);
            log.info("오픈채팅 시간별 알림 배치 완료: {}건 발송 예약", outboxes.size());
        }
    }
}

package com.example.appcenter_project.domain.openChat.entity;

import com.example.appcenter_project.domain.openChat.enums.ChatNotificationMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OpenChatParticipantNotificationTest {

    @Test
    @DisplayName("create(roomId, userId, joinedAt, mode) — 지정한 알림 모드로 생성")
    void factory_sets_notification_mode() {
        OpenChatParticipant p = OpenChatParticipant.create(
                1L, 10L, LocalDateTime.now(), ChatNotificationMode.BUNDLED);

        assertThat(p.getNotificationMode()).isEqualTo(ChatNotificationMode.BUNDLED);
    }
}
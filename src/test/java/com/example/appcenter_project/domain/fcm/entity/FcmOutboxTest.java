package com.example.appcenter_project.domain.fcm.entity;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FcmOutboxTest {

    @Test
    @DisplayName("routingType = NOTICE 저장 — AC-1: 공지사항 routing 저장")
    void should_set_routingType_NOTICE_when_notice_routing_provided() {
        // given
        Long announcementId = 5678L;

        // when
        FcmOutbox outbox = FcmOutbox.create("token", "제목", "내용", FcmRoutingType.NOTICE, announcementId);

        // then
        assertThat(outbox.getRoutingType()).isEqualTo(FcmRoutingType.NOTICE);
    }

    @Test
    @DisplayName("routingId = 5678 저장 — AC-1: 공지사항 announcementId 저장")
    void should_set_routingId_when_notice_routing_provided() {
        // given
        Long announcementId = 5678L;

        // when
        FcmOutbox outbox = FcmOutbox.create("token", "제목", "내용", FcmRoutingType.NOTICE, announcementId);

        // then
        assertThat(outbox.getRoutingId()).isEqualTo(5678L);
    }

    @Test
    @DisplayName("routingType = CHAT 저장 — AC-2: 채팅 즉시 알림 routing 저장")
    void should_set_routingType_CHAT_when_chat_routing_provided() {
        // given
        Long roomId = 1234L;

        // when
        FcmOutbox outbox = FcmOutbox.create("token", "채팅 제목", "채팅 내용", FcmRoutingType.CHAT, roomId);

        // then
        assertThat(outbox.getRoutingType()).isEqualTo(FcmRoutingType.CHAT);
    }

    @Test
    @DisplayName("routingId = 1234 저장 — AC-2: 채팅 즉시 알림 roomId 저장")
    void should_set_routingId_when_chat_routing_provided() {
        // given
        Long roomId = 1234L;

        // when
        FcmOutbox outbox = FcmOutbox.create("token", "채팅 제목", "채팅 내용", FcmRoutingType.CHAT, roomId);

        // then
        assertThat(outbox.getRoutingId()).isEqualTo(1234L);
    }

    @Test
    @DisplayName("routing 없는 경우 routingType = null — AC-6: 기존 동작 유지")
    void should_have_null_routingType_when_no_routing_create() {
        // given / when
        FcmOutbox outbox = FcmOutbox.create("token", "제목", "내용");

        // then
        assertThat(outbox.getRoutingType()).isNull();
    }

    @Test
    @DisplayName("routing 없는 경우 routingId = null — AC-6: 기존 동작 유지")
    void should_have_null_routingId_when_no_routing_create() {
        // given / when
        FcmOutbox outbox = FcmOutbox.create("token", "제목", "내용");

        // then
        assertThat(outbox.getRoutingId()).isNull();
    }
}

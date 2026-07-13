package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FcmRoutingTypeTest {

    @Test
    @DisplayName("threadId = notice_5678 — AC-4: NOTICE APNS thread-id 포맷")
    void should_return_notice_prefix_threadId_when_NOTICE() {
        // given
        Long routingId = 5678L;

        // when
        String threadId = FcmRoutingType.NOTICE.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("notice_5678");
    }

    @Test
    @DisplayName("threadId = chat_room_1234 — AC-5: CHAT APNS thread-id 포맷")
    void should_return_chat_room_prefix_threadId_when_CHAT() {
        // given
        Long routingId = 1234L;

        // when
        String threadId = FcmRoutingType.CHAT.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("chat_room_1234");
    }

    @Test
    @DisplayName("dataKey = noticeId — AC-4: NOTICE data 키 포맷")
    void should_return_noticeId_dataKey_when_NOTICE() {
        // given / when
        String dataKey = FcmRoutingType.NOTICE.dataKey();

        // then
        assertThat(dataKey).isEqualTo("noticeId");
    }

    @Test
    @DisplayName("dataKey = chatRoomId — AC-5: CHAT data 키 포맷")
    void should_return_chatRoomId_dataKey_when_CHAT() {
        // given / when
        String dataKey = FcmRoutingType.CHAT.dataKey();

        // then
        assertThat(dataKey).isEqualTo("chatRoomId");
    }

    @Test
    @DisplayName("dataType = NOTICE — AC-4: NOTICE data type 값")
    void should_return_NOTICE_string_dataType_when_NOTICE() {
        // given / when
        String dataType = FcmRoutingType.NOTICE.dataType();

        // then
        assertThat(dataType).isEqualTo("NOTICE");
    }

    @Test
    @DisplayName("dataType = CHAT — AC-5: CHAT data type 값")
    void should_return_CHAT_string_dataType_when_CHAT() {
        // given / when
        String dataType = FcmRoutingType.CHAT.dataType();

        // then
        assertThat(dataType).isEqualTo("CHAT");
    }
}

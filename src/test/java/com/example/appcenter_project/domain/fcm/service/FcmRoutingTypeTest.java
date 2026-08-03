package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FcmRoutingTypeTest {

    @Test
    @DisplayName("path = /chat/open/1234 — AC-1: CHAT_OPEN path 포맷")
    void should_return_chat_open_path_when_CHAT_OPEN() {
        // given
        Long routingId = 1234L;

        // when
        String path = FcmRoutingType.CHAT_OPEN.path(routingId);

        // then
        assertThat(path).isEqualTo("/chat/open/1234");
    }

    @Test
    @DisplayName("path = /chat/open/99 — AC-2: CHAT_PERSONAL path 포맷")
    void should_return_chat_personal_path_when_CHAT_PERSONAL() {
        // given
        Long routingId = 99L;

        // when
        String path = FcmRoutingType.CHAT_PERSONAL.path(routingId);

        // then
        assertThat(path).isEqualTo("/chat/open/99");
    }

    @Test
    @DisplayName("path = /announcements/7 — AC-3: ANNOUNCEMENT path 포맷")
    void should_return_announcements_path_when_ANNOUNCEMENT() {
        // given
        Long routingId = 7L;

        // when
        String path = FcmRoutingType.ANNOUNCEMENT.path(routingId);

        // then
        assertThat(path).isEqualTo("/announcements/7");
    }

    @Test
    @DisplayName("path = /complain/5 — AC-4: COMPLAINT path 포맷")
    void should_return_complain_path_when_COMPLAINT() {
        // given
        Long routingId = 5L;

        // when
        String path = FcmRoutingType.COMPLAINT.path(routingId);

        // then
        assertThat(path).isEqualTo("/complain/5");
    }

    @Test
    @DisplayName("path = /roommate/list/3 — AC-5: ROOMMATE_POST path 포맷")
    void should_return_roommate_list_path_when_ROOMMATE_POST() {
        // given
        Long routingId = 3L;

        // when
        String path = FcmRoutingType.ROOMMATE_POST.path(routingId);

        // then
        assertThat(path).isEqualTo("/roommate/list/3");
    }

    @Test
    @DisplayName("threadId = chat_room_1234 — CHAT_OPEN APNS thread-id 포맷")
    void should_return_chat_room_prefix_threadId_when_CHAT_OPEN() {
        // given
        Long routingId = 1234L;

        // when
        String threadId = FcmRoutingType.CHAT_OPEN.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("chat_room_1234");
    }

    @Test
    @DisplayName("threadId = chat_room_99 — CHAT_PERSONAL APNS thread-id 포맷")
    void should_return_chat_room_prefix_threadId_when_CHAT_PERSONAL() {
        // given
        Long routingId = 99L;

        // when
        String threadId = FcmRoutingType.CHAT_PERSONAL.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("chat_room_99");
    }

    @Test
    @DisplayName("threadId = notice — AC-3: ANNOUNCEMENT APNS thread-id 고정값")
    void should_return_fixed_notice_threadId_when_ANNOUNCEMENT() {
        // given
        Long routingId = 7L;

        // when
        String threadId = FcmRoutingType.ANNOUNCEMENT.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("notice");
    }

    @Test
    @DisplayName("threadId = complaint — COMPLAINT APNS thread-id 고정값")
    void should_return_fixed_complaint_threadId_when_COMPLAINT() {
        // given
        Long routingId = 5L;

        // when
        String threadId = FcmRoutingType.COMPLAINT.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("complaint");
    }

    @Test
    @DisplayName("threadId = roommate — ROOMMATE_POST APNS thread-id 고정값")
    void should_return_fixed_roommate_threadId_when_ROOMMATE_POST() {
        // given
        Long routingId = 3L;

        // when
        String threadId = FcmRoutingType.ROOMMATE_POST.threadId(routingId);

        // then
        assertThat(threadId).isEqualTo("roommate");
    }

    @Test
    @DisplayName("dataKey = chatRoomId — CHAT_OPEN data 키 포맷")
    void should_return_chatRoomId_dataKey_when_CHAT_OPEN() {
        // given / when
        String dataKey = FcmRoutingType.CHAT_OPEN.dataKey();

        // then
        assertThat(dataKey).isEqualTo("chatRoomId");
    }

    @Test
    @DisplayName("dataKey = chatRoomId — CHAT_PERSONAL data 키 포맷")
    void should_return_chatRoomId_dataKey_when_CHAT_PERSONAL() {
        // given / when
        String dataKey = FcmRoutingType.CHAT_PERSONAL.dataKey();

        // then
        assertThat(dataKey).isEqualTo("chatRoomId");
    }

    @Test
    @DisplayName("dataKey = noticeId — ANNOUNCEMENT data 키 포맷")
    void should_return_noticeId_dataKey_when_ANNOUNCEMENT() {
        // given / when
        String dataKey = FcmRoutingType.ANNOUNCEMENT.dataKey();

        // then
        assertThat(dataKey).isEqualTo("noticeId");
    }

    @Test
    @DisplayName("dataKey = complaintId — COMPLAINT data 키 포맷")
    void should_return_complaintId_dataKey_when_COMPLAINT() {
        // given / when
        String dataKey = FcmRoutingType.COMPLAINT.dataKey();

        // then
        assertThat(dataKey).isEqualTo("complaintId");
    }

    @Test
    @DisplayName("dataKey = roommatePostId — ROOMMATE_POST data 키 포맷")
    void should_return_roommatePostId_dataKey_when_ROOMMATE_POST() {
        // given / when
        String dataKey = FcmRoutingType.ROOMMATE_POST.dataKey();

        // then
        assertThat(dataKey).isEqualTo("roommatePostId");
    }

    @Test
    @DisplayName("dataType = CHAT_OPEN — CHAT_OPEN data type 값")
    void should_return_CHAT_OPEN_string_dataType_when_CHAT_OPEN() {
        // given / when
        String dataType = FcmRoutingType.CHAT_OPEN.dataType();

        // then
        assertThat(dataType).isEqualTo("CHAT_OPEN");
    }

    @Test
    @DisplayName("dataType = ANNOUNCEMENT — ANNOUNCEMENT data type 값")
    void should_return_ANNOUNCEMENT_string_dataType_when_ANNOUNCEMENT() {
        // given / when
        String dataType = FcmRoutingType.ANNOUNCEMENT.dataType();

        // then
        assertThat(dataType).isEqualTo("ANNOUNCEMENT");
    }
}

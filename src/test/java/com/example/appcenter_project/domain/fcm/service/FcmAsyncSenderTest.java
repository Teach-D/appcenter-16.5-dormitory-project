package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import com.example.appcenter_project.domain.fcm.fixture.FcmOutboxFixture;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import com.example.appcenter_project.domain.user.repository.FcmTokenRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FcmAsyncSenderTest {

    @Mock
    FcmTokenRepository fcmTokenRepository;

    @Mock
    FcmOutboxRepository fcmOutboxRepository;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    FirebaseMessaging firebaseMessaging;

    @InjectMocks
    FcmAsyncSender fcmAsyncSender;

    @Test
    @DisplayName("APNS thread-id = notice_5678 포함 — AC-4: 공지사항 FCM APNS 필드")
    void should_include_apns_threadId_notice_5678_when_notice_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createNoticeOutboxBatch(5678L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "공지 제목", "공지 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getApnsConfig().getAps().getThreadId()).isEqualTo("notice_5678");
    }

    @Test
    @DisplayName("Android tag = notice_5678 포함 — AC-4: 공지사항 FCM Android 필드")
    void should_include_android_tag_notice_5678_when_notice_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createNoticeOutboxBatch(5678L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "공지 제목", "공지 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getAndroidConfig().getNotification().getTag()).isEqualTo("notice_5678");
    }

    @Test
    @DisplayName("data.type = NOTICE 포함 — AC-4: 공지사항 FCM data type 필드")
    void should_include_data_type_NOTICE_when_notice_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createNoticeOutboxBatch(5678L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "공지 제목", "공지 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getData()).containsEntry("type", "NOTICE");
    }

    @Test
    @DisplayName("data.noticeId = 5678 포함 — AC-4: 공지사항 FCM data noticeId 필드")
    void should_include_data_noticeId_5678_when_notice_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createNoticeOutboxBatch(5678L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "공지 제목", "공지 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getData()).containsEntry("noticeId", "5678");
    }

    @Test
    @DisplayName("APNS thread-id = chat_room_1234 포함 — AC-5: 채팅 FCM APNS 필드")
    void should_include_apns_threadId_chat_room_1234_when_chat_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createChatOutboxBatch(1234L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "채팅 제목", "채팅 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getApnsConfig().getAps().getThreadId()).isEqualTo("chat_room_1234");
    }

    @Test
    @DisplayName("Android tag = chat_room_1234 포함 — AC-5: 채팅 FCM Android 필드")
    void should_include_android_tag_chat_room_1234_when_chat_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createChatOutboxBatch(1234L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "채팅 제목", "채팅 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getAndroidConfig().getNotification().getTag()).isEqualTo("chat_room_1234");
    }

    @Test
    @DisplayName("data.type = CHAT 포함 — AC-5: 채팅 FCM data type 필드")
    void should_include_data_type_CHAT_when_chat_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createChatOutboxBatch(1234L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "채팅 제목", "채팅 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getData()).containsEntry("type", "CHAT");
    }

    @Test
    @DisplayName("data.chatRoomId = 1234 포함 — AC-5: 채팅 FCM data chatRoomId 필드")
    void should_include_data_chatRoomId_1234_when_chat_routing() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createChatOutboxBatch(1234L, 1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "채팅 제목", "채팅 내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getData()).containsEntry("chatRoomId", "1234");
    }

    @Test
    @DisplayName("APNS config 미포함 — AC-6: routing null인 경우 APNS 필드 생략")
    void should_not_include_apns_config_when_routing_is_null() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createGenericOutboxBatch(1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "제목", "내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getApnsConfig()).isNull();
    }

    @Test
    @DisplayName("Android config 미포함 — AC-6: routing null인 경우 Android 필드 생략")
    void should_not_include_android_config_when_routing_is_null() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createGenericOutboxBatch(1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "제목", "내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getAndroidConfig()).isNull();
    }

    @Test
    @DisplayName("data 미포함 — AC-6: routing null인 경우 data 필드 생략")
    void should_not_include_data_when_routing_is_null() throws Exception {
        // given
        List<FcmOutbox> batch = FcmOutboxFixture.createGenericOutboxBatch(1);
        ArgumentCaptor<MulticastMessage> captor = ArgumentCaptor.forClass(MulticastMessage.class);
        given(firebaseMessaging.sendEachForMulticast(captor.capture())).willReturn(null);

        // when
        fcmAsyncSender.sendOutboxBatch(batch, "제목", "내용").join();

        // then
        MulticastMessage message = captor.getValue();
        assertThat(message.getData()).isEmpty();
    }
}

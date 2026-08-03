package com.example.appcenter_project.domain.fcm.service;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import com.example.appcenter_project.domain.fcm.fixture.FcmOutboxFixture;
import com.example.appcenter_project.domain.fcm.repository.FcmOutboxRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class FcmOutboxProcessorTest {

    @Mock
    FcmOutboxRepository fcmOutboxRepository;

    @Mock
    FcmAsyncSender fcmAsyncSender;

    @Mock
    RedisTemplate<String, Object> redisTemplate;

    @Mock
    TransactionTemplate transactionTemplate;

    @InjectMocks
    FcmOutboxProcessor fcmOutboxProcessor;

    @SuppressWarnings("unchecked")
    private void stubTransactionTemplateToReturnChunk(List<FcmOutbox> chunk) {
        given(transactionTemplate.execute(any(TransactionCallback.class)))
                .willAnswer(inv -> {
                    TransactionCallback<List<FcmOutbox>> callback = inv.getArgument(0);
                    return callback.doInTransaction(null);
                })
                .willReturn(List.of());
        given(fcmOutboxRepository.findChunk(any(), any(), any())).willReturn(chunk).willReturn(List.of());
    }

    @Test
    @DisplayName("sendOutboxBatch 2회 호출 — routing 다른 메시지 별도 그룹으로 분리")
    void should_call_sendOutboxBatch_twice_when_different_routing_types_exist() {
        // given
        FcmOutbox announcement1 = FcmOutboxFixture.createWithAnnouncementRouting(1L);
        FcmOutbox announcement2 = FcmOutboxFixture.createWithAnnouncementRouting(1L);
        FcmOutbox chatOpen1 = FcmOutboxFixture.createWithChatOpenRouting(2L);

        stubTransactionTemplateToReturnChunk(List.of(announcement1, announcement2, chatOpen1));
        given(fcmAsyncSender.sendOutboxBatch(anyList(), anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(new int[]{0, 0}));

        // when
        fcmOutboxProcessor.process();

        // then
        then(fcmAsyncSender).should(times(2)).sendOutboxBatch(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("routing null 그룹 분리 — routing 없는 메시지는 별도 그룹")
    void should_separate_null_routing_from_announcement_routing() {
        // given
        FcmOutbox announcement = FcmOutboxFixture.createWithAnnouncementRouting(1L);
        FcmOutbox generic = FcmOutboxFixture.createWithoutRouting();

        stubTransactionTemplateToReturnChunk(List.of(announcement, generic));
        given(fcmAsyncSender.sendOutboxBatch(anyList(), anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(new int[]{0, 0}));

        // when
        fcmOutboxProcessor.process();

        // then
        then(fcmAsyncSender).should(times(2)).sendOutboxBatch(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("동일 routing 그룹 단일 batch — 동일 routingType+routingId는 하나로 묶임")
    void should_call_sendOutboxBatch_once_when_same_routing() {
        // given
        FcmOutbox announcement1 = FcmOutboxFixture.createWithAnnouncementRouting(7L);
        FcmOutbox announcement2 = FcmOutboxFixture.createWithAnnouncementRouting(7L);

        stubTransactionTemplateToReturnChunk(List.of(announcement1, announcement2));
        given(fcmAsyncSender.sendOutboxBatch(anyList(), anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(new int[]{0, 0}));

        // when
        fcmOutboxProcessor.process();

        // then
        then(fcmAsyncSender).should(times(1)).sendOutboxBatch(anyList(), anyString(), anyString());
    }

    @Test
    @DisplayName("같은 제목 다른 routing은 별도 그룹 — routingType+routingId 복합 키 그룹화")
    void should_separate_groups_when_same_title_but_different_routing() {
        // given
        FcmOutbox announcementRoom1 = FcmOutbox.create("token-a", "공지", "내용", FcmRoutingType.ANNOUNCEMENT, 1L);
        FcmOutbox chatOpenRoom2 = FcmOutbox.create("token-b", "공지", "내용", FcmRoutingType.CHAT_OPEN, 2L);

        stubTransactionTemplateToReturnChunk(List.of(announcementRoom1, chatOpenRoom2));
        given(fcmAsyncSender.sendOutboxBatch(anyList(), anyString(), anyString()))
                .willReturn(CompletableFuture.completedFuture(new int[]{0, 0}));

        // when
        fcmOutboxProcessor.process();

        // then
        then(fcmAsyncSender).should(times(2)).sendOutboxBatch(anyList(), anyString(), anyString());
    }
}

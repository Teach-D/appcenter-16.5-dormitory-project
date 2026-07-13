package com.example.appcenter_project.domain.fcm.fixture;

import com.example.appcenter_project.domain.fcm.entity.FcmOutbox;
import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;

import java.util.List;

public class FcmOutboxFixture {

    public static FcmOutbox createWithoutRouting() {
        return FcmOutbox.create("token-generic", "제목", "내용");
    }

    public static FcmOutbox createWithNoticeRouting(Long routingId) {
        return FcmOutbox.create("token-notice", "공지 제목", "공지 내용", FcmRoutingType.NOTICE, routingId);
    }

    public static FcmOutbox createWithChatRouting(Long routingId) {
        return FcmOutbox.create("token-chat", "채팅 제목", "채팅 내용", FcmRoutingType.CHAT, routingId);
    }

    public static List<FcmOutbox> createNoticeOutboxBatch(Long routingId, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> FcmOutbox.create("token-" + i, "공지 제목", "공지 내용", FcmRoutingType.NOTICE, routingId))
                .toList();
    }

    public static List<FcmOutbox> createChatOutboxBatch(Long routingId, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> FcmOutbox.create("token-" + i, "채팅 제목", "채팅 내용", FcmRoutingType.CHAT, routingId))
                .toList();
    }

    public static List<FcmOutbox> createGenericOutboxBatch(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> FcmOutbox.create("token-" + i, "제목", "내용"))
                .toList();
    }
}

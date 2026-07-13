package com.example.appcenter_project.domain.fcm.enums;

public enum FcmRoutingType {
    NOTICE, CHAT;

    public String threadId(Long id) {
        return switch (this) {
            case NOTICE -> "notice_" + id;
            case CHAT -> "chat_room_" + id;
        };
    }

    public String dataKey() {
        return switch (this) {
            case NOTICE -> "noticeId";
            case CHAT -> "chatRoomId";
        };
    }

    public String dataType() {
        return this.name();
    }
}

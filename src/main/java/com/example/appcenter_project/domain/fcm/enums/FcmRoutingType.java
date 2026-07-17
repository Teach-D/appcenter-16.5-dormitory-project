package com.example.appcenter_project.domain.fcm.enums;

public enum FcmRoutingType {
    CHAT_OPEN, CHAT_PERSONAL, ANNOUNCEMENT, COMPLAINT, ROOMMATE_POST, NOTICE, CHAT;

    public String threadId(Long id) {
        return switch (this) {
            case CHAT_OPEN, CHAT_PERSONAL, CHAT -> "chat_room_" + id;
            case ANNOUNCEMENT, NOTICE -> "notice";
            case COMPLAINT -> "complaint";
            case ROOMMATE_POST -> "roommate";
        };
    }

    public String path(Long id) {
        return switch (this) {
            case CHAT_OPEN -> "/chat/open/" + id;
            case CHAT_PERSONAL, CHAT -> "/chat/open/" + id;
            case ANNOUNCEMENT, NOTICE -> "/announcements/" + id;
            case COMPLAINT -> "/complain/" + id;
            case ROOMMATE_POST -> "/roommate/list/" + id;
        };
    }

    public String dataKey() {
        return switch (this) {
            case CHAT_OPEN, CHAT_PERSONAL, CHAT -> "chatRoomId";
            case ANNOUNCEMENT, NOTICE -> "noticeId";
            case COMPLAINT -> "complaintId";
            case ROOMMATE_POST -> "roommatePostId";
        };
    }

    public String dataType() {
        return this.name();
    }
}

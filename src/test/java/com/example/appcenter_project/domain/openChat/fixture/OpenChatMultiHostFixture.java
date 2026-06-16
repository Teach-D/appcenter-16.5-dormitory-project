package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;

import java.util.List;

public class OpenChatMultiHostFixture {

    public static final Long ROOM_ID = 1L;
    public static final Long HOST_USER_ID = 10L;
    public static final Long PARTICIPANT_USER_ID = 20L;
    public static final Long ANOTHER_HOST_USER_ID = 30L;
    public static final Long NON_PARTICIPANT_USER_ID = 99L;
    public static final Long ADMIN_USER_ID = 1L;

    public static OpenChatRoom createRoom() {
        return OpenChatRoom.create("테스트 채팅방", "설명", OpenChatRoomScope.ALL, 100, HOST_USER_ID, null, false);
    }

    public static OpenChatRoom createOfficialRoom() {
        return OpenChatRoom.createOfficial("공식 채팅방", "공식 설명", OpenChatRoomScope.ALL, 100, ADMIN_USER_ID, null);
    }

    public static OpenChatParticipant createHostParticipant() {
        return OpenChatParticipant.create(ROOM_ID, HOST_USER_ID, true);
    }

    public static OpenChatParticipant createParticipant() {
        return OpenChatParticipant.create(ROOM_ID, PARTICIPANT_USER_ID, false);
    }

    public static OpenChatParticipant createAnotherHostParticipant() {
        return OpenChatParticipant.create(ROOM_ID, ANOTHER_HOST_USER_ID, true);
    }

    public static List<OpenChatParticipant> createParticipantsWithSoleHost() {
        return List.of(
                createHostParticipant(),
                createParticipant()
        );
    }

    public static List<OpenChatParticipant> createParticipantsWithMultipleHosts() {
        return List.of(
                createHostParticipant(),
                createAnotherHostParticipant(),
                createParticipant()
        );
    }
}

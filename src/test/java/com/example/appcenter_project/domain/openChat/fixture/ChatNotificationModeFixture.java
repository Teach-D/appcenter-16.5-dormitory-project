package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.fcm.entity.FcmToken;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;

import java.time.LocalDateTime;

public class ChatNotificationModeFixture {

    public static OpenChatRoom createRoom(Long id, String name) {
        return OpenChatRoom.createForTest(id, name);
    }

    public static OpenChatRoom createRealRoom(String name) {
        return OpenChatRoom.create(name, "설명", OpenChatRoomScope.ALL, 10, 1L, null, false);
    }

    public static OpenChatParticipant createParticipant(Long roomId, Long userId) {
        return OpenChatParticipant.create(roomId, userId, LocalDateTime.now());
    }

    public static OpenChatParticipant createHostParticipant(Long roomId, Long userId) {
        return OpenChatParticipant.create(roomId, userId, true);
    }

    public static OpenChatMessage createTextMessage(Long roomId, Long senderId) {
        return OpenChatMessage.create(roomId, senderId, "안녕하세요", OpenChatMessageType.TEXT);
    }

    public static OpenChatMessage createImageMessage(Long roomId, Long senderId) {
        return OpenChatMessage.create(roomId, senderId, "[이미지]", OpenChatMessageType.IMAGE);
    }

    public static OpenChatMessage createSystemMessage(Long roomId) {
        return OpenChatMessage.create(roomId, 0L, "홍길동님이 입장했습니다.", OpenChatMessageType.SYSTEM);
    }

    public static User createUser(Long id) {
        return User.createForTest(id, "user-" + id, DormType.DORM_1);
    }

    public static FcmToken createFcmToken(User user, String tokenValue) {
        return FcmToken.builder().user(user).token(tokenValue).build();
    }
}

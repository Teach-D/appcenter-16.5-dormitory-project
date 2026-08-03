package com.example.appcenter_project.domain.roommate.fixture;

import com.example.appcenter_project.domain.roommate.entity.RoommateChattingChat;
import com.example.appcenter_project.domain.roommate.entity.RoommateChattingRoom;
import com.example.appcenter_project.domain.user.entity.User;

import java.time.LocalDateTime;

public class RoommateChattingRoomFixture {

    public static RoommateChattingRoom createActiveRoommateRoom(Long hostUserId, Long guestUserId) {
        User host = createUserWithId(hostUserId, "호스트유저");
        User guest = createUserWithId(guestUserId, "게스트유저");
        return RoommateChattingRoom.create(host, guest);
    }

    public static RoommateChattingRoom createActiveRoommateRoomWithGuestName(
            Long hostUserId, Long guestUserId, String guestName) {
        User host = createUserWithId(hostUserId, "호스트유저");
        User guest = createUserWithId(guestUserId, guestName);
        return RoommateChattingRoom.create(host, guest);
    }

    public static RoommateChattingRoom createRoommateRoomWithHostLeft(Long hostUserId, Long guestUserId) {
        RoommateChattingRoom room = createActiveRoommateRoom(hostUserId, guestUserId);
        room.hostLeave();
        return room;
    }

    public static RoommateChattingChat createChatWithCreatedDate(
            RoommateChattingRoom room, LocalDateTime createdDate) {
        User sender = room.getHost();
        return RoommateChattingChat.create(room, sender, "테스트 메시지", createdDate);
    }

    private static User createUserWithId(Long userId, String name) {
        return User.createForTest(userId, name);
    }
}

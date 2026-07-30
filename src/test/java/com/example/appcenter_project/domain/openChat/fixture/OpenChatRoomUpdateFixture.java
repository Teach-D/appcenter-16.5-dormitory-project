package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.request.RequestUpdateOpenChatRoomDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomType;

public class OpenChatRoomUpdateFixture {

    public static OpenChatRoom createOpenRoom() {
        return OpenChatRoom.createForTest(1L, "기존 방 이름", OpenChatRoomType.OPEN);
    }

    public static OpenChatRoom createDerivedRoom() {
        return OpenChatRoom.createForTest(2L, "파생 방", OpenChatRoomType.DERIVED);
    }

    public static OpenChatRoom createPersonalRoom() {
        return OpenChatRoom.createForTest(3L, "개인 방", OpenChatRoomType.PERSONAL);
    }

    public static OpenChatRoom createOfficialRoom() {
        return OpenChatRoom.create("공식 방", "설명", OpenChatRoomScope.ALL, 100, null, null, true);
    }

    public static OpenChatParticipant createHostParticipant(Long roomId, Long userId) {
        return OpenChatParticipant.create(roomId, userId, true);
    }

    public static OpenChatParticipant createNonHostParticipant(Long roomId, Long userId) {
        return OpenChatParticipant.create(roomId, userId, false);
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequest() {
        return RequestUpdateOpenChatRoomDto.builder()
                .name("새 방 이름")
                .description("새 설명")
                .scope(OpenChatRoomScope.DORMITORY)
                .maxParticipants(20)
                .password("newpass")
                .isPublic(true)
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithNameOnly() {
        return RequestUpdateOpenChatRoomDto.builder()
                .name("이름만 변경")
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithPasswordClear() {
        return RequestUpdateOpenChatRoomDto.builder()
                .password("")
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithAllNull() {
        return RequestUpdateOpenChatRoomDto.builder().build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithMaxParticipants(int max) {
        return RequestUpdateOpenChatRoomDto.builder()
                .maxParticipants(max)
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithBlankName() {
        return RequestUpdateOpenChatRoomDto.builder()
                .name("   ")
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithLongName() {
        return RequestUpdateOpenChatRoomDto.builder()
                .name("a".repeat(31))
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithLongDescription() {
        return RequestUpdateOpenChatRoomDto.builder()
                .description("a".repeat(101))
                .build();
    }

    public static RequestUpdateOpenChatRoomDto createUpdateRequestWithLongPassword() {
        return RequestUpdateOpenChatRoomDto.builder()
                .password("a".repeat(51))
                .build();
    }
}

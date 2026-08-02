package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.request.RequestCreateDormOfficialRoomDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.DormType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class OpenChatDormOfficialRoomFixture {

    public static RequestCreateDormOfficialRoomDto createRequest() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name("1기숙사 공식 오픈채팅")
                .description("1기숙사 거주 학생들을 위한 공식 채팅방입니다.")
                .dormType(DormType.DORM_1)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithDorm(DormType dormType) {
        return RequestCreateDormOfficialRoomDto.builder()
                .name(dormType.name() + " 공식 오픈채팅")
                .description("설명")
                .dormType(dormType)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithNullName() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name(null)
                .description("설명")
                .dormType(DormType.DORM_1)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithBlankName() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name("   ")
                .description("설명")
                .dormType(DormType.DORM_1)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithLongName() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name("a".repeat(31))
                .description("설명")
                .dormType(DormType.DORM_1)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithLongDescription() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name("1기숙사 공식 오픈채팅")
                .description("a".repeat(101))
                .dormType(DormType.DORM_1)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithNullDormType() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name("1기숙사 공식 오픈채팅")
                .description("설명")
                .dormType(null)
                .build();
    }

    public static RequestCreateDormOfficialRoomDto createRequestWithNoneDormType() {
        return RequestCreateDormOfficialRoomDto.builder()
                .name("공식 오픈채팅")
                .description("설명")
                .dormType(DormType.NONE)
                .build();
    }

    public static OpenChatRoom createDormOfficialRoom(DormType dormType) {
        return OpenChatRoom.createDormOfficial(
                dormType.name() + " 공식 오픈채팅",
                "설명",
                null,
                dormType
        );
    }

    public static User createUserWithDorm(Long id, DormType dormType) {
        return User.createForTest(id, "user-" + id, dormType);
    }

    public static List<User> createUsersWithDorm(DormType dormType, int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> User.createForTest((long) i, "user-" + i, dormType))
                .toList();
    }

    public static OpenChatParticipant createParticipant(Long roomId, Long userId) {
        return OpenChatParticipant.create(roomId, userId, LocalDateTime.now());
    }

    public static Map<String, Long> createResponse(Long roomId) {
        return Map.of("roomId", roomId);
    }
}

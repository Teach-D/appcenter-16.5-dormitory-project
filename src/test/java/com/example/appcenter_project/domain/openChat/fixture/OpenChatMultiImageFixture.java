package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OpenChatMultiImageFixture {

    public static List<MultipartFile> createImageList(int count) {
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            files.add(new MockMultipartFile(
                    "images",
                    "img" + (i + 1) + ".jpg",
                    "image/jpeg",
                    ("content-" + i).getBytes()
            ));
        }
        return files;
    }

    public static List<ResponseOpenChatMessageDto> createMessageResponseList(Long roomId, Long senderId, int count) {
        List<ResponseOpenChatMessageDto> responses = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            OpenChatMessage message = OpenChatMessage.create(roomId, senderId, "", OpenChatMessageType.IMAGE);
            responses.add(ResponseOpenChatMessageDto.from(
                    message,
                    "홍길동",
                    2,
                    List.of("https://host/images/open-chat-message/msg" + (i + 1) + "/img.jpg")
            ));
        }
        return responses;
    }

    public static List<OpenChatParticipant> createParticipantList(Long roomId, List<Long> userIds) {
        return userIds.stream()
                .map(userId -> OpenChatParticipant.create(roomId, userId, LocalDateTime.now()))
                .toList();
    }

    // NOTE: ResponseSimpleParticipantDto / ResponseSimpleParticipantListDto 미구현 — 구현 에이전트가 생성 후 아래 주석 해제
    //
    // public static ResponseSimpleParticipantListDto createSimpleParticipantListDto(Long roomId) {
    //     List<ResponseSimpleParticipantDto> participants = List.of(
    //             ResponseSimpleParticipantDto.of(42L, "홍길동"),
    //             ResponseSimpleParticipantDto.of(43L, "김철수")
    //     );
    //     return ResponseSimpleParticipantListDto.of(roomId, participants);
    // }
    //
    // public static ResponseSimpleParticipantListDto createSimpleParticipantListDtoWithAdmin(Long roomId) {
    //     List<ResponseSimpleParticipantDto> participants = List.of(
    //             ResponseSimpleParticipantDto.of(42L, "홍길동"),
    //             ResponseSimpleParticipantDto.of(1L, "관리자")
    //     );
    //     return ResponseSimpleParticipantListDto.of(roomId, participants);
    // }
}

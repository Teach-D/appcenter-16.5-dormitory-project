package com.example.appcenter_project.domain.openChat.fixture;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.entity.OpenChatMessage;
import com.example.appcenter_project.domain.openChat.entity.OpenChatParticipant;
import com.example.appcenter_project.domain.openChat.entity.OpenChatRoom;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public class OpenChatImageMessageFixture {

    public static OpenChatRoom createRoom() {
        return OpenChatRoom.create("테스트 채팅방", "설명", OpenChatRoomScope.ALL, 10, 1L, null, false);
    }

    public static OpenChatParticipant createParticipant(Long roomId, Long userId) {
        return OpenChatParticipant.create(roomId, userId, LocalDateTime.now());
    }

    public static OpenChatMessage createImageMessage(Long roomId, Long senderId) {
        return OpenChatMessage.create(roomId, senderId, "", OpenChatMessageType.IMAGE);
    }

    public static MultipartFile createValidJpgFile() {
        return new MockMultipartFile("images", "test.jpg", "image/jpeg", "fake-image-content".getBytes());
    }

    public static MultipartFile createValidPngFile() {
        return new MockMultipartFile("images", "test.png", "image/png", "fake-image-content".getBytes());
    }

    public static MultipartFile createValidGifFile() {
        return new MockMultipartFile("images", "test.gif", "image/gif", "fake-image-content".getBytes());
    }

    public static MultipartFile createValidWebpFile() {
        return new MockMultipartFile("images", "test.webp", "image/webp", "fake-image-content".getBytes());
    }

    public static MultipartFile createInvalidHeicFile() {
        return new MockMultipartFile("images", "test.heic", "image/heic", "fake-image-content".getBytes());
    }

    public static MultipartFile createInvalidPdfFile() {
        return new MockMultipartFile("images", "test.pdf", "application/pdf", "fake-pdf-content".getBytes());
    }

    public static List<MultipartFile> createValidImageList() {
        return List.of(createValidJpgFile(), createValidPngFile());
    }

    public static List<MultipartFile> createSingleValidImageList() {
        return List.of(createValidJpgFile());
    }

    public static List<MultipartFile> createInvalidFormatImageList() {
        return List.of(createValidJpgFile(), createInvalidHeicFile());
    }

    public static ResponseOpenChatMessageDto createImageMessageResponse(Long messageId, Long roomId, Long senderId) {
        return ResponseOpenChatMessageDto.from(
                createImageMessage(roomId, senderId),
                "테스트유저",
                2,
                List.of("https://host/images/open_chat_message/msg" + messageId + "_uuid.jpg")
        );
    }
}

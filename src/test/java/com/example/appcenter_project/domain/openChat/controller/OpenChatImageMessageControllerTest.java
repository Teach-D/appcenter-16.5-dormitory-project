package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.common.image.repository.ImageRepository;
import com.example.appcenter_project.common.image.service.ImageService;
import com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatMessageDto;
import com.example.appcenter_project.domain.openChat.enums.OpenChatMessageType;
import com.example.appcenter_project.domain.openChat.fixture.OpenChatImageMessageFixture;
import com.example.appcenter_project.domain.openChat.service.OpenChatMessageService;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import com.example.appcenter_project.global.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpenChatMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenChatImageMessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OpenChatMessageService openChatMessageService;

    @MockBean
    private ImageService imageService;

    @MockBean
    private ImageRepository imageRepository;

    @MockBean
    private SlackErrorNotifier slackErrorNotifier;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private CustomUserDetails mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new CustomUserDetails();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private static final Long ROOM_ID = 7L;
    private static final Long USER_ID = 1L;

    @Test
    @DisplayName("이미지 메시지 전송 성공 — 201 CREATED 반환")
    void should_return_201_when_image_message_sent_successfully() throws Exception {
        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(
                OpenChatImageMessageFixture.createImageMessage(ROOM_ID, USER_ID),
                "홍길동",
                2,
                List.of("https://host/images/open_chat_message/img.jpg")
        );
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any())).willReturn(response);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("이미지 메시지 전송 성공 — 응답에 type=IMAGE 포함")
    void should_return_type_image_in_response() throws Exception {
        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(
                OpenChatImageMessageFixture.createImageMessage(ROOM_ID, USER_ID),
                "홍길동",
                2,
                List.of("https://host/images/open_chat_message/img.jpg")
        );
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any())).willReturn(response);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value(OpenChatMessageType.IMAGE.name()));
    }

    @Test
    @DisplayName("이미지 메시지 전송 성공 — 응답에 imageUrls 포함")
    void should_return_image_urls_in_response() throws Exception {
        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(
                OpenChatImageMessageFixture.createImageMessage(ROOM_ID, USER_ID),
                "홍길동",
                2,
                List.of("https://host/images/open_chat_message/img.jpg")
        );
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any())).willReturn(response);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls[0]").value("https://host/images/open_chat_message/img.jpg"));
    }

    @Test
    @DisplayName("이미지 메시지 전송 성공 — 응답에 content가 빈 문자열")
    void should_return_empty_content_for_image_message() throws Exception {
        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(
                OpenChatImageMessageFixture.createImageMessage(ROOM_ID, USER_ID),
                "홍길동",
                2,
                List.of("https://host/images/open_chat_message/img.jpg")
        );
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any())).willReturn(response);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(""));
    }

    @Test
    @DisplayName("404 반환 — 채팅방 없음")
    void should_return_404_when_room_not_found() throws Exception {
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any()))
                .willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 반환 — 비참여자 요청")
    void should_return_403_when_not_participant() throws Exception {
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any()))
                .willThrow(new CustomException(ErrorCode.OPEN_CHAT_NOT_PARTICIPANT));

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("400 반환 — 허용되지 않는 이미지 포맷")
    void should_return_400_when_invalid_image_format() throws Exception {
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any()))
                .willThrow(new CustomException(ErrorCode.IMAGE_INVALID_FORMAT));

        MockMultipartFile heicFile = new MockMultipartFile("images", "test.heic", "image/heic", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(heicFile)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — 이미지 없이 요청")
    void should_return_400_when_no_images_provided() throws Exception {
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any()))
                .willThrow(new CustomException(ErrorCode.VALIDATION_ERROR));

        MockMultipartFile emptyImage = new MockMultipartFile("images", "", "image/jpeg", new byte[0]);

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(emptyImage)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미지 메시지 전송 성공 — 여러 파일 업로드 시 201")
    void should_return_201_when_multiple_images_sent() throws Exception {
        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(
                OpenChatImageMessageFixture.createImageMessage(ROOM_ID, USER_ID),
                "홍길동",
                2,
                List.of(
                        "https://host/images/open_chat_message/img1.jpg",
                        "https://host/images/open_chat_message/img2.png"
                )
        );
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any())).willReturn(response);

        MockMultipartFile image1 = new MockMultipartFile("images", "test1.jpg", "image/jpeg", "content".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "test2.png", "image/png", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image1)
                .file(image2)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrls").isArray())
                .andExpect(jsonPath("$.imageUrls.length()").value(2));
    }

    @Test
    @DisplayName("이미지 메시지 전송 성공 — 응답에 roomId 포함")
    void should_return_room_id_in_response() throws Exception {
        ResponseOpenChatMessageDto response = ResponseOpenChatMessageDto.from(
                OpenChatImageMessageFixture.createImageMessage(ROOM_ID, USER_ID),
                "홍길동",
                2,
                List.of("https://host/images/open_chat_message/img.jpg")
        );
        given(openChatMessageService.sendImageMessage(any(), eq(ROOM_ID), any(), any())).willReturn(response);

        MockMultipartFile image = new MockMultipartFile("images", "test.jpg", "image/jpeg", "content".getBytes());

        ResultActions result = mockMvc.perform(multipart("/open-chat-rooms/{roomId}/messages/image", ROOM_ID)
                .file(image)
                .contentType(MediaType.MULTIPART_FORM_DATA));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(ROOM_ID));
    }
}

package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.fixture.OpenChatDerivedRoomLinkFixture;
import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.domain.user.entity.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenChatDerivedRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenChatDerivedRoomLinkControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    OpenChatRoomService openChatRoomService;

    @MockBean
    SlackErrorNotifier slackErrorNotifier;

    @MockBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        User user = User.createForTest(7L, "테스트유저");
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("201 반환 — BR-674 정상 요청으로 파생 방 생성 및 ROOM_LINK 메시지 전송")
    void should_return_201_when_valid_derived_room_request() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        var response = OpenChatDerivedRoomLinkFixture.createDerivedRoomCreatedResponse();
        given(openChatRoomService.createDerivedRoom(anyLong(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("roomId 포함 응답 반환 — BR-674 파생 방 생성 성공")
    void should_return_room_id_in_response_when_derived_room_created() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        var response = OpenChatDerivedRoomLinkFixture.createDerivedRoomCreatedResponse();
        given(openChatRoomService.createDerivedRoom(anyLong(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(jsonPath("$.roomId").value(42L));
    }

    @Test
    @DisplayName("400 반환 — originRoomId 누락")
    void should_return_400_when_origin_room_id_is_null() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequestWithNullOriginRoomId();

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — name 누락(null)")
    void should_return_400_when_name_is_null() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequestWithNullName();

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — name 공백 문자열")
    void should_return_400_when_name_is_blank() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequestWithBlankName();

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — maxParticipants 누락")
    void should_return_400_when_max_participants_is_null() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequestWithNullMaxParticipants();

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — isPublic 누락")
    void should_return_400_when_is_public_is_null() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequestWithNullIsPublic();

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("404 반환 — BR-674 originRoomId에 해당하는 채팅방 없음")
    void should_return_404_when_origin_room_not_found() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        given(openChatRoomService.createDerivedRoom(anyLong(), any()))
                .willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 반환 — BR-674 요청자가 originRoom 참여자가 아님")
    void should_return_404_when_requester_is_not_participant_of_origin_room() throws Exception {
        // given
        var request = OpenChatDerivedRoomLinkFixture.createRequest();
        given(openChatRoomService.createDerivedRoom(anyLong(), any()))
                .willThrow(new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));

        // when
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/derived")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound());
    }
}

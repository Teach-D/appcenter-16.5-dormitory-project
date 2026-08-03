package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.fixture.OpenChatRoomUpdateFixture;
import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenChatRoomUpdateControllerTest {

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

    // ──────────────────────────────────────────────
    // Happy Path
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("204 반환 — AC-1 OPEN 방 방장이 정상 수정 요청")
    @WithMockUser
    void should_return_204_when_host_updates_open_room() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequest();
        willDoNothing().given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("204 반환 — AC-7 name만 전달한 부분 업데이트 요청")
    @WithMockUser
    void should_return_204_when_only_name_provided() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        willDoNothing().given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("204 반환 — AC-8 password 빈 문자열로 비밀번호 해제 요청")
    @WithMockUser
    void should_return_204_when_password_clear_requested() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithPasswordClear();
        willDoNothing().given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNoContent());
    }

    // ──────────────────────────────────────────────
    // Validation (Bean Validation)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("400 반환 — name이 30자 초과인 경우 Bean Validation 실패")
    @WithMockUser
    void should_return_400_when_name_exceeds_30_characters() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithLongName();

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — description이 100자 초과인 경우 Bean Validation 실패")
    @WithMockUser
    void should_return_400_when_description_exceeds_100_characters() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithLongDescription();

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — maxParticipants가 2 미만인 경우 Bean Validation 실패")
    @WithMockUser
    void should_return_400_when_maxParticipants_less_than_2() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithMaxParticipants(1);

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — maxParticipants가 100 초과인 경우 Bean Validation 실패")
    @WithMockUser
    void should_return_400_when_maxParticipants_exceeds_100() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithMaxParticipants(101);

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — password가 50자 초과인 경우 Bean Validation 실패")
    @WithMockUser
    void should_return_400_when_password_exceeds_50_characters() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithLongPassword();

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    // ──────────────────────────────────────────────
    // Error Cases — Business Rule
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("404 반환 — AC-9 BR-700-1 존재하지 않는 roomId")
    @WithMockUser
    void should_return_404_when_room_not_found() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND))
                .given(openChatRoomService).updateRoom(anyLong(), eq(999L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 반환 — AC-4 BR-700-2 참여자가 아닌 사용자")
    @WithMockUser
    void should_return_404_when_user_is_not_participant() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND))
                .given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("403 반환 — AC-3 BR-700-2 방장이 아닌 참여자가 수정 시도")
    @WithMockUser
    void should_return_403_when_user_is_not_host() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_NOT_HOST))
                .given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403 반환 — AC-5 BR-700-3 PERSONAL 또는 OFFICIAL 방 수정 시도")
    @WithMockUser
    void should_return_403_when_room_type_is_forbidden() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithNameOnly();
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN))
                .given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("400 반환 — AC-6 BR-700-4 maxParticipants < 현재 참여자 수")
    @WithMockUser
    void should_return_400_when_maxParticipants_less_than_current_participant_count() throws Exception {
        // given
        var request = OpenChatRoomUpdateFixture.createUpdateRequestWithMaxParticipants(2);
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL))
                .given(openChatRoomService).updateRoom(anyLong(), eq(1L), any());

        // when
        ResultActions result = mockMvc.perform(patch("/open-chat-rooms/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }
}

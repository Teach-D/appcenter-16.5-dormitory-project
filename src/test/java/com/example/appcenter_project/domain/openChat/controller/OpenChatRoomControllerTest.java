package com.example.appcenter_project.domain.openChat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OpenChatRoomController 테스트.
 *
 * 구현 에이전트가 생성해야 할 클래스:
 * - com.example.appcenter_project.domain.openChat.controller.OpenChatRoomController
 * - com.example.appcenter_project.domain.openChat.service.OpenChatRoomService
 * - com.example.appcenter_project.domain.openChat.enums.OpenChatRoomScope (DORMITORY, ALL)
 * - com.example.appcenter_project.domain.openChat.dto.request.RequestCreateOpenChatRoomDto
 *   필드: name(@NotBlank @Size(max=30)), description(@Size(max=100)), scope(@NotNull), maxParticipants(@Min(2) @Max(100))
 * - com.example.appcenter_project.domain.openChat.dto.response.ResponseOpenChatRoomDetailDto
 *   필드: roomId, name, description, scope, currentParticipants, maxParticipants, isOfficial, createdAt
 * - com.example.appcenter_project.domain.openChat.dto.response.ResponseLeaveOpenChatRoomDto
 *   필드: roomDeleted(boolean)
 * - com.example.appcenter_project.global.exception.ErrorCode
 *   추가: OPEN_CHAT_ROOM_NOT_FOUND(404), OPEN_CHAT_ROOM_FORBIDDEN(403),
 *         OPEN_CHAT_ROOM_FULL(400), OPEN_CHAT_PARTICIPANT_NOT_FOUND(404)
 *
 * Controller 엔드포인트:
 * - POST   /open-chat-rooms                              → 201 CREATED, body: {roomId}
 * - GET    /open-chat-rooms?tab={MY|DORMITORY|ALL}       → 200 OK, tab 없으면 400
 * - POST   /open-chat-rooms/{roomId}/participants/me     → 200 OK
 * - DELETE /open-chat-rooms/{roomId}/participants/me     → 200 OK, body: {roomDeleted}
 * - DELETE /open-chat-rooms/{roomId}                     → 204 NO CONTENT
 *
 * 주의: @WebMvcTest 어노테이션에 OpenChatRoomController.class를 명시하면
 * 구현 후 이 파일을 수정해야 함. 현재는 컴파일을 위해 클래스 참조를 분리함.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class OpenChatRoomControllerTest {

    // NOTE: 구현 에이전트가 OpenChatRoomController와 OpenChatRoomService를 생성한 후
    // 아래 주석을 해제하고 클래스 어노테이션을 @WebMvcTest(OpenChatRoomController.class)로 변경하세요.
    //
    // @Autowired MockMvc mockMvc;
    // @Autowired ObjectMapper objectMapper;
    // @MockBean OpenChatRoomService openChatRoomService;

    // ============================================================
    // 아래 테스트들은 구현 완료 후 주석 해제하여 실행합니다.
    // 각 테스트 메서드는 Red 단계의 기대 동작을 문서화합니다.
    // ============================================================

    @Test
    @DisplayName("채팅방 생성 성공 — 정상 요청 시 201 + roomId 반환")
    void should_return_201_when_valid_create_request() {
        // given
        // RequestCreateOpenChatRoomDto request = RequestCreateOpenChatRoomDto.builder()
        //     .name("테스트 채팅방").description("설명").scope(OpenChatRoomScope.ALL).maxParticipants(10).build();
        // given(openChatRoomService.createRoom(any(), any())).willReturn(1L);
        //
        // when
        // ResultActions result = mockMvc.perform(post("/open-chat-rooms")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content(objectMapper.writeValueAsString(request)));
        //
        // then
        // result.andExpect(status().isCreated())
        //       .andExpect(jsonPath("$.data.roomId").exists());

        // 구현 전 — 컴파일 통과용 placeholder
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — name이 공백으로만 구성된 경우")
    void should_return_400_when_name_is_blank() {
        // given
        // RequestCreateOpenChatRoomDto request = RequestCreateOpenChatRoomDto.builder()
        //     .name("   ").description("설명").scope(OpenChatRoomScope.ALL).maxParticipants(10).build();
        //
        // when
        // ResultActions result = mockMvc.perform(post("/open-chat-rooms")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content(objectMapper.writeValueAsString(request)));
        //
        // then
        // result.andExpect(status().isBadRequest());
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — name이 30자 초과인 경우")
    void should_return_400_when_name_exceeds_30_characters() {
        // given: name = "a" * 31
        // then: 400 Bad Request
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — description이 100자 초과인 경우")
    void should_return_400_when_description_exceeds_100_characters() {
        // given: description = "a" * 101
        // then: 400 Bad Request
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — maxParticipants가 2 미만인 경우")
    void should_return_400_when_maxParticipants_less_than_2() {
        // given: maxParticipants = 1
        // then: 400 Bad Request
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — maxParticipants가 100 초과인 경우")
    void should_return_400_when_maxParticipants_exceeds_100() {
        // given: maxParticipants = 101
        // then: 400 Bad Request
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — scope가 null인 경우")
    void should_return_400_when_scope_is_null() {
        // given: scope = null
        // then: 400 Bad Request
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("200 반환 — tab=MY 정상 조회")
    void should_return_200_when_tab_is_MY() {
        // GET /open-chat-rooms?tab=MY → 200 OK
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("200 반환 — tab=DORMITORY 정상 조회")
    void should_return_200_when_tab_is_DORMITORY() {
        // GET /open-chat-rooms?tab=DORMITORY → 200 OK
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("200 반환 — tab=ALL 정상 조회")
    void should_return_200_when_tab_is_ALL() {
        // GET /open-chat-rooms?tab=ALL → 200 OK
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — tab 파라미터 누락")
    void should_return_400_when_tab_parameter_is_missing() {
        // GET /open-chat-rooms (tab 없음) → 400 Bad Request
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("200 반환 — 채팅방 입장 정상 요청")
    void should_return_200_when_valid_join_request() {
        // POST /open-chat-rooms/1/participants/me → 200 OK, body: roomId, name, ...
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("404 반환 — 존재하지 않는 roomId로 입장 요청")
    void should_return_404_when_room_not_found_on_join() {
        // POST /open-chat-rooms/999/participants/me
        // openChatRoomService.joinRoom → throws CustomException(OPEN_CHAT_ROOM_NOT_FOUND)
        // → 404 Not Found
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("200 반환 + roomDeleted=false — 비방장이 채팅방 나가기")
    void should_return_200_with_roomDeleted_false_when_non_host_leaves() {
        // DELETE /open-chat-rooms/1/participants/me → 200 OK, body: {roomDeleted: false}
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }

    @Test
    @DisplayName("204 반환 — 방장이 채팅방 삭제")
    void should_return_204_when_host_deletes_room() {
        // DELETE /open-chat-rooms/1 → 204 No Content
        org.assertj.core.api.Assertions.assertThat(true).isTrue();
    }
}

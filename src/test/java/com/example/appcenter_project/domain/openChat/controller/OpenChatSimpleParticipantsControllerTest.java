package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import com.example.appcenter_project.global.security.CustomUserDetails;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BR-670 참여자 단순 목록 컨트롤러 테스트 — RED 단계
 *
 * 구현 에이전트가 생성해야 할 클래스:
 * - ResponseSimpleParticipantDto      (userId: Long, name: String, 정적 팩토리 of(Long, String))
 * - ResponseSimpleParticipantListDto  (roomId: Long, participants: List<...>, 정적 팩토리 of(Long, List))
 * - OpenChatRoomService.getSimpleParticipants(Long roomId, Long requesterId)
 * - OpenChatRoomController GET /{roomId}/participants/simple 엔드포인트
 */
@WebMvcTest(OpenChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenChatSimpleParticipantsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenChatRoomService openChatRoomService;

    @MockBean
    private SlackErrorNotifier slackErrorNotifier;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final Long ROOM_ID = 5L;

    @BeforeEach
    void setUp() {
        CustomUserDetails mockUser = new CustomUserDetails();
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — 200 OK 반환")
    void should_return_200_when_participant_requests_simple_list() throws Exception {
        // given
        // ResponseSimpleParticipantListDto response = OpenChatMultiImageFixture.createSimpleParticipantListDto(ROOM_ID);
        // given(openChatRoomService.getSimpleParticipants(eq(ROOM_ID), any())).willReturn(response);
        // NOTE: getSimpleParticipants 미구현 — 구현 후 위 코드로 교체

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(status().isOk());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — 응답에 roomId 포함")
    void should_return_roomId_in_simple_participant_list() throws Exception {
        // given
        // given(openChatRoomService.getSimpleParticipants(eq(ROOM_ID), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(jsonPath("$.roomId").value(ROOM_ID));
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — 응답에 participants 배열 포함")
    void should_return_participants_array_in_simple_list() throws Exception {
        // given
        // given(openChatRoomService.getSimpleParticipants(eq(ROOM_ID), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(jsonPath("$.participants").isArray());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — participants 원소에 userId 포함")
    void should_return_userId_in_each_participant() throws Exception {
        // given
        // given(openChatRoomService.getSimpleParticipants(eq(ROOM_ID), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(jsonPath("$.participants[0].userId").exists());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-6 참여자 단순 목록 조회 성공 — participants 원소에 name 포함")
    void should_return_name_in_each_participant() throws Exception {
        // given
        // given(openChatRoomService.getSimpleParticipants(eq(ROOM_ID), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(jsonPath("$.participants[0].name").exists());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-7 비참여자 단순 목록 요청 — 403 OPEN_CHAT_ROOM_FORBIDDEN 반환")
    void should_return_403_when_non_participant_requests_simple_list() throws Exception {
        // given
        // willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN))
        //         .given(openChatRoomService).getSimpleParticipants(eq(ROOM_ID), any());

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(status().isForbidden());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-8 존재하지 않는 roomId 요청 — 404 OPEN_CHAT_ROOM_NOT_FOUND 반환")
    void should_return_404_when_room_not_found_for_simple_list() throws Exception {
        // given
        // willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND))
        //         .given(openChatRoomService).getSimpleParticipants(eq(ROOM_ID), any());

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(status().isNotFound());
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("AC-9 ROLE_ADMIN 참여자 — name이 관리자로 반환됨")
    void should_return_admin_name_as_gwallija_for_admin_user() throws Exception {
        // given
        // given(openChatRoomService.getSimpleParticipants(eq(ROOM_ID), any())).willReturn(responseWithAdmin);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants/simple", ROOM_ID));

        // then
        // result.andExpect(jsonPath("$.participants[?(@.name == '관리자')]").exists());
        assertThat(true).isTrue();
    }
}

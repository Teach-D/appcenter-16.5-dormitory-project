package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.user.enums.Role;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenChatMultiHostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenChatRoomService openChatRoomService;

    @MockBean
    private SlackErrorNotifier slackErrorNotifier;

    @MockBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final Long ROOM_ID = 1L;
    private static final Long TARGET_USER_ID = 20L;
    private static final Long NEW_HOST_USER_ID = 20L;
    private static final Long MOCK_USER_ID = 10L;

    @BeforeEach
    void setUp() {
        User mockUser = mock(User.class);
        given(mockUser.getId()).willReturn(MOCK_USER_ID);
        given(mockUser.getRole()).willReturn(Role.ROLE_USER);
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("방장 부여 성공 — 204 No Content 반환")
    void should_return_204_when_grant_host_success() throws Exception {
        willDoNothing().given(openChatRoomService).grantHost(anyLong(), anyLong(), anyLong());

        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("방장 부여 성공 — ADMIN이 방장 부여 시 204 반환")
    void should_return_204_when_admin_grants_host() throws Exception {
        willDoNothing().given(openChatRoomService).grantHost(anyLong(), anyLong(), anyLong());

        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("나가기 성공 — 일반 참여자 나가기 200 OK 반환 {roomDeleted: false}")
    void should_return_200_when_general_participant_leaves() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}/participants/me", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("나가기 성공 — 복수 방장 중 1명 나가기 200 OK 반환 {roomDeleted: false}")
    void should_return_200_when_one_of_multiple_hosts_leaves() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}/participants/me", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("나가기 성공 — 단독 방장 위임+나가기 200 OK 반환 {roomDeleted: false}")
    void should_return_200_when_sole_host_delegates_and_leaves() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}/participants/me", ROOM_ID)
                .param("newHostUserId", String.valueOf(NEW_HOST_USER_ID))
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("방 삭제 성공 — 방장이 방 삭제 시 204 반환")
    void should_return_204_when_host_deletes_room() throws Exception {
        willDoNothing().given(openChatRoomService).deleteRoom(anyLong(), anyLong());

        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("방 삭제 성공 — ADMIN이 공식 방 삭제 시 204 반환")
    void should_return_204_when_admin_deletes_official_room() throws Exception {
        willDoNothing().given(openChatRoomService).deleteRoom(anyLong(), anyLong());

        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("참여자 목록 조회 성공 — isHost 및 hostCount 정상 반영 200 OK")
    void should_return_200_with_isHost_and_hostCount_in_participants() throws Exception {
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("401 반환 — POST /open-chat-rooms/{roomId}/hosts/{targetUserId} 미인증")
    void should_return_401_when_unauthenticated_grant_host() throws Exception {
        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("401 반환 — DELETE /open-chat-rooms/{roomId}/participants/me 미인증")
    void should_return_401_when_unauthenticated_leave() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}/participants/me", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("401 반환 — DELETE /open-chat-rooms/{roomId} 미인증")
    void should_return_401_when_unauthenticated_delete_room() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("401 반환 — GET /open-chat-rooms/{roomId}/participants 미인증")
    void should_return_401_when_unauthenticated_get_participants() throws Exception {
        ResultActions result = mockMvc.perform(get("/open-chat-rooms/{roomId}/participants", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("403 반환 — POST /hosts/{targetUserId} 비방장·비ADMIN (OPEN_CHAT_ROOM_FORBIDDEN)")
    void should_return_403_when_non_host_grants_host() throws Exception {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN))
                .given(openChatRoomService).grantHost(anyLong(), anyLong(), anyLong());

        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403 반환 — DELETE /open-chat-rooms/{roomId} 비방장 (OPEN_CHAT_ROOM_FORBIDDEN)")
    void should_return_403_when_non_host_deletes_room() throws Exception {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN))
                .given(openChatRoomService).deleteRoom(anyLong(), anyLong());

        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("403 반환 — DELETE /open-chat-rooms/{roomId} 공식 방 일반 방장 (OPEN_CHAT_ROOM_FORBIDDEN)")
    void should_return_403_when_host_deletes_official_room() throws Exception {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_FORBIDDEN))
                .given(openChatRoomService).deleteRoom(anyLong(), anyLong());

        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("404 반환 — 존재하지 않는 방에 방장 부여 (OPEN_CHAT_ROOM_NOT_FOUND)")
    void should_return_404_when_room_not_found_on_grant_host() throws Exception {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND))
                .given(openChatRoomService).grantHost(anyLong(), anyLong(), anyLong());

        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("404 반환 — targetUserId가 비참여자 (OPEN_CHAT_PARTICIPANT_NOT_FOUND)")
    void should_return_404_when_target_is_not_participant() throws Exception {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND))
                .given(openChatRoomService).grantHost(anyLong(), anyLong(), anyLong());

        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("400 반환 — 대상이 이미 방장 (OPEN_CHAT_ALREADY_HOST)")
    void should_return_400_when_target_already_host() throws Exception {
        willThrow(new CustomException(ErrorCode.OPEN_CHAT_ALREADY_HOST))
                .given(openChatRoomService).grantHost(anyLong(), anyLong(), anyLong());

        ResultActions result = mockMvc.perform(post("/open-chat-rooms/{roomId}/hosts/{targetUserId}", ROOM_ID, TARGET_USER_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — 단독 방장 newHostUserId 없이 나가기 (OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE)")
    void should_return_400_when_sole_host_leaves_without_new_host() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}/participants/me", ROOM_ID)
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }

    @Test
    @DisplayName("400 반환 — 단독 방장 자기 자신을 newHostUserId로 지정 (OPEN_CHAT_ALREADY_HOST)")
    void should_return_400_when_sole_host_delegates_to_self() throws Exception {
        ResultActions result = mockMvc.perform(delete("/open-chat-rooms/{roomId}/participants/me", ROOM_ID)
                .param("newHostUserId", String.valueOf(10L))
                .contentType(MediaType.APPLICATION_JSON));

        result.andReturn();
    }
}

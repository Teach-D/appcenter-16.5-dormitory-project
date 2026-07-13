package com.example.appcenter_project.domain.notification.controller;

import com.example.appcenter_project.domain.fcm.enums.FcmRoutingType;
import com.example.appcenter_project.domain.notification.dto.request.RequestNotificationReadDto;
import com.example.appcenter_project.domain.notification.dto.response.ResponseNotificationReadDto;
import com.example.appcenter_project.domain.notification.service.NotificationReadService;
import com.example.appcenter_project.domain.notification.service.NotificationService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationReadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private NotificationReadService notificationReadService;

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

    // =========================================================================
    // AC-1·4: 200 OK — 정상 요청
    // =========================================================================

    @Test
    @DisplayName("AC-1: 200 반환 — NOTICE 타입 정상 요청")
    void should_return_200_when_NOTICE_type_valid() throws Exception {
        RequestNotificationReadDto request = new RequestNotificationReadDto(FcmRoutingType.NOTICE, "5678");
        ResponseNotificationReadDto response = ResponseNotificationReadDto.success();

        given(notificationReadService.markAsRead(nullable(Long.class), eq(FcmRoutingType.NOTICE), eq(5678L)))
                .willReturn(response);

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true))
              .andExpect(jsonPath("$.message").value("성공적으로 읽음 처리되었습니다."));
    }

    @Test
    @DisplayName("AC-4: 200 반환 — CHAT 타입 정상 요청")
    void should_return_200_when_CHAT_type_valid() throws Exception {
        RequestNotificationReadDto request = new RequestNotificationReadDto(FcmRoutingType.CHAT, "1234");
        ResponseNotificationReadDto response = ResponseNotificationReadDto.success();

        given(notificationReadService.markAsRead(nullable(Long.class), eq(FcmRoutingType.CHAT), eq(1234L)))
                .willReturn(response);

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // AC-2: NOTICE — 대상 없어도 200 OK (멱등성)
    // =========================================================================

    @Test
    @DisplayName("AC-2: 200 반환 — NOTICE 타입, 대상 UserNotification 없어도 성공")
    void should_return_200_when_NOTICE_userNotification_not_found() throws Exception {
        RequestNotificationReadDto request = new RequestNotificationReadDto(FcmRoutingType.NOTICE, "999");
        ResponseNotificationReadDto response = ResponseNotificationReadDto.success();

        given(notificationReadService.markAsRead(nullable(Long.class), eq(FcmRoutingType.NOTICE), eq(999L)))
                .willReturn(response);

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.success").value(true));
    }

    // =========================================================================
    // AC-3: targetId 파싱 실패 → 400
    // =========================================================================

    @Test
    @DisplayName("AC-3: 400 반환 — targetId가 숫자가 아닌 문자열")
    void should_return_400_when_targetId_is_not_numeric() throws Exception {
        Map<String, String> body = Map.of("type", "NOTICE", "targetId", "abc");

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));

        result.andExpect(status().isBadRequest());
    }

    // =========================================================================
    // AC-5: CHAT — 방에 메시지 없어도 200 OK
    // =========================================================================

    @Test
    @DisplayName("AC-5: 200 반환 — CHAT 타입, 방에 메시지 없어도 성공")
    void should_return_200_when_CHAT_room_has_no_messages() throws Exception {
        RequestNotificationReadDto request = new RequestNotificationReadDto(FcmRoutingType.CHAT, "1234");
        ResponseNotificationReadDto response = ResponseNotificationReadDto.success();

        given(notificationReadService.markAsRead(nullable(Long.class), eq(FcmRoutingType.CHAT), eq(1234L)))
                .willReturn(response);

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk());
    }

    // =========================================================================
    // AC-6: CHAT — 참여자 아닌 경우 → 서비스 예외 전파
    // =========================================================================

    @Test
    @DisplayName("AC-6: 403 반환 — CHAT 타입, 채팅방 참여자 아님 (OPEN_CHAT_NOT_PARTICIPANT)")
    void should_return_403_when_CHAT_user_is_not_participant() throws Exception {
        RequestNotificationReadDto request = new RequestNotificationReadDto(FcmRoutingType.CHAT, "1234");

        willThrow(new CustomException(ErrorCode.OPEN_CHAT_NOT_PARTICIPANT))
                .given(notificationReadService).markAsRead(nullable(Long.class), eq(FcmRoutingType.CHAT), eq(1234L));

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isForbidden());
    }

    // =========================================================================
    // AC-7: 잘못된 type 값 → 400
    // =========================================================================

    @Test
    @DisplayName("AC-7: 400 반환 — 잘못된 type 값 ('ROOMMATE')")
    void should_return_400_when_type_is_invalid() throws Exception {
        Map<String, String> body = Map.of("type", "ROOMMATE", "targetId", "123");

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("AC-7: 400 반환 — type 필드 누락")
    void should_return_400_when_type_is_null() throws Exception {
        Map<String, String> body = Map.of("targetId", "123");

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — targetId 필드 누락")
    void should_return_400_when_targetId_is_blank() throws Exception {
        Map<String, String> body = Map.of("type", "NOTICE");

        ResultActions result = mockMvc.perform(
                patch("/notifications/read")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)));

        result.andExpect(status().isBadRequest());
    }
}

package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseNotificationModeDto;
import com.example.appcenter_project.domain.openChat.enums.ChatNotificationMode;
import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.domain.user.entity.User;
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
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class OpenChatNotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean
    OpenChatRoomService openChatRoomService;
    @MockitoBean SlackErrorNotifier slackErrorNotifier;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

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
    @DisplayName("200 + mode 반환 — GET 알림 모드 조회")
    void should_return_200_with_mode_when_get_notification() throws Exception {
        given(openChatRoomService.getNotificationMode(anyLong(), eq(1L)))
                .willReturn(ResponseNotificationModeDto.of(ChatNotificationMode.BUNDLED));

        mockMvc.perform(get("/open-chat-rooms/1/participants/me/notification")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("BUNDLED"));
    }
}
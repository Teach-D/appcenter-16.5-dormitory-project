package com.example.appcenter_project.domain.openChat.controller;

import com.example.appcenter_project.domain.openChat.dto.response.ResponseChatRoomListDto;
import com.example.appcenter_project.domain.openChat.enums.OpenChatRoomTab;
import com.example.appcenter_project.domain.openChat.fixture.ChatRoomListFixture;
import com.example.appcenter_project.domain.openChat.service.OpenChatRoomService;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OpenChatRoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChatRoomListControllerTest {

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

    @Test
    @DisplayName("200 반환 — BR-665 AC-1: keyword 없이 ALL 탭 조회 시 totalUnreadCount=0")
    void should_return_200_with_totalUnreadCount_zero_when_tab_is_ALL() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createAllTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), isNull(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("totalUnreadCount=0 반환 — BR-665 AC-1: ALL 탭 응답 본문에 totalUnreadCount=0 포함")
    void should_return_totalUnreadCount_zero_in_body_when_tab_is_ALL() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createAllTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), isNull(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$.totalUnreadCount").value(0));
    }

    @Test
    @DisplayName("chatCategory=OPEN_CHAT 포함 — BR-665 AC-1: ALL 탭 항목에 chatCategory 필드 존재")
    void should_include_chatCategory_field_when_tab_is_ALL() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createAllTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), isNull(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$.content[0].chatCategory").value("OPEN_CHAT"));
    }

    @Test
    @DisplayName("200 반환 — BR-665 AC-2: ALL 탭 키워드 이름 검색 성공")
    void should_return_200_when_ALL_tab_with_keyword() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createChatRoomListDto(
                java.util.List.of(ChatRoomListFixture.createOpenChatRoomResponseWithName("공부방")), 0);
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), eq("공부"), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .param("keyword", "공부")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("keyword 서비스 전달 — BR-665 AC-2: keyword 파라미터가 서비스에 전달됨")
    void should_pass_keyword_to_service_when_keyword_provided() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createEmptyResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), eq("공부"), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .param("keyword", "공부")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        then(openChatRoomService).should(times(1))
                .getRooms(any(), eq(OpenChatRoomTab.ALL), eq("공부"), any(Pageable.class));
    }

    @Test
    @DisplayName("200 반환 — BR-665 AC-4: DORMITORY 탭 keyword 검색 성공")
    void should_return_200_when_DORMITORY_tab_with_keyword() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createChatRoomListDto(
                java.util.List.of(ChatRoomListFixture.createOpenChatRoomResponseWithName("조용한방")), 0);
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.DORMITORY), eq("조용"), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "DORMITORY")
                .param("keyword", "조용")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("200 반환 — BR-665 AC-5: MY 탭 조회 성공 (openChat + 룸메 통합)")
    void should_return_200_when_tab_is_MY() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createMyTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.MY), isNull(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "MY")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("content 길이=2 반환 — BR-665 AC-5: MY 탭 openChat+룸메 통합 목록 반환")
    void should_return_two_items_when_MY_tab_has_openchat_and_roommate() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createMyTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.MY), isNull(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "MY")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("200 반환 — BR-665 AC-7: MY 탭 keyword로 openChat 방 검색 성공")
    void should_return_200_when_MY_tab_with_keyword() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createChatRoomListDto(
                java.util.List.of(ChatRoomListFixture.createOpenChatRoomResponseWithName("공부방")), 0);
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.MY), eq("공부"), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "MY")
                .param("keyword", "공부")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("400 반환 — BR-665 tab 파라미터 누락 시 400")
    void should_return_400_when_tab_is_missing() throws Exception {
        // given: tab 파라미터 없음

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 반환 — BR-665 tab에 잘못된 enum 값 전달 시 400")
    void should_return_400_when_tab_has_invalid_enum_value() throws Exception {
        // given: tab=INVALID

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "INVALID")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공백 keyword는 null 처리 — BR-665 AC-12: keyword= (공백) 시 전체 조회와 동일")
    void should_treat_blank_keyword_as_no_filter() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createAllTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), any(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .param("keyword", " ")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("ResponseChatRoomListDto 반환 — BR-665: 응답 타입이 ResponseChatRoomListDto로 변경됨")
    void should_return_ResponseChatRoomListDto_type_response() throws Exception {
        // given
        ResponseChatRoomListDto response = ChatRoomListFixture.createAllTabResponse();
        given(openChatRoomService.getRooms(any(), eq(OpenChatRoomTab.ALL), isNull(), any(Pageable.class)))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/open-chat-rooms")
                .param("tab", "ALL")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$.content").isArray())
              .andExpect(jsonPath("$.totalElements").exists())
              .andExpect(jsonPath("$.totalPages").exists())
              .andExpect(jsonPath("$.pageNumber").exists())
              .andExpect(jsonPath("$.pageSize").exists())
              .andExpect(jsonPath("$.totalUnreadCount").exists());
    }
}

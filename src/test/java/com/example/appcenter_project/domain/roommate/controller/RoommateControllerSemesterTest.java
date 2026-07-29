package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.dto.response.ResponseRoommatePostDto;
import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.fixture.RoommateSemesterFixture;
import com.example.appcenter_project.domain.roommate.service.MyRoommateService;
import com.example.appcenter_project.domain.roommate.service.RoommateQueryService;
import com.example.appcenter_project.domain.roommate.service.RoommateService;
import com.example.appcenter_project.global.exception.SlackErrorNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoommateController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoommateControllerSemesterTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    RoommateService roommateService;

    @MockitoBean
    MyRoommateService myRoommateService;

    @MockitoBean
    RoommateQueryService roommateQueryService;

    @MockitoBean
    SlackErrorNotifier slackErrorNotifier;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    @DisplayName("201 반환 — AC-1 1·2월 생성 시 응답 본문에 semester = 1 포함")
    @WithMockUser
    void should_return_semester1_in_response_when_post_in_first_semester() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(jsonPath("$.semester").value(1));
    }

    @Test
    @DisplayName("201 반환 — AC-1 1·2월 생성 시 응답 본문에 year 포함")
    @WithMockUser
    void should_return_year_in_response_when_post_in_first_semester() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(jsonPath("$.year").value(2026));
    }

    @Test
    @DisplayName("201 반환 — AC-2 7·8월 생성 시 응답 본문에 semester = 2 포함")
    @WithMockUser
    void should_return_semester2_in_response_when_post_in_second_semester() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.SECOND);
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(jsonPath("$.semester").value(2));
    }

    @Test
    @DisplayName("201 반환 — AC-3 그 외 월 생성 시 응답 본문 year = null")
    @WithMockUser
    void should_return_null_year_in_response_when_post_in_off_semester_month() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(null, (SemesterType) null);
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(jsonPath("$.year").isEmpty());
    }

    @Test
    @DisplayName("201 반환 — AC-3 그 외 월 생성 시 응답 본문 semester = null")
    @WithMockUser
    void should_return_null_semester_in_response_when_post_in_off_semester_month() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(null, (SemesterType) null);
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(jsonPath("$.semester").isEmpty());
    }

    @Test
    @DisplayName("200 반환 — AC-4 게시글 단일 조회 응답에 year 필드 포함")
    void should_include_year_field_in_single_board_response() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.getRoommateBoardDetail(anyLong(), any(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/roommates/1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$.year").value(2026));
    }

    @Test
    @DisplayName("200 반환 — AC-4 게시글 단일 조회 응답에 semester 필드 포함")
    void should_include_semester_field_in_single_board_response() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.getRoommateBoardDetail(anyLong(), any(), any())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(get("/roommates/1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$.semester").value(1));
    }

    @Test
    @DisplayName("200 반환 — AC-4 게시글 목록 조회 응답 각 항목에 year 필드 포함")
    void should_include_year_field_in_board_list_response() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.getRoommateBoardList(any(), any(), any())).willReturn(List.of(response));

        // when
        ResultActions result = mockMvc.perform(get("/roommates/list")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$[0].year").value(2026));
    }

    @Test
    @DisplayName("200 반환 — AC-4 게시글 목록 조회 응답 각 항목에 semester 필드 포함")
    void should_include_semester_field_in_board_list_response() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.getRoommateBoardList(any(), any(), any())).willReturn(List.of(response));

        // when
        ResultActions result = mockMvc.perform(get("/roommates/list")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(jsonPath("$[0].semester").value(1));
    }

    @Test
    @DisplayName("200 반환 — semester=1 필터 시 해당 학기 게시글만 반환")
    void should_return_filtered_list_when_semester_param_given() throws Exception {
        // given
        ResponseRoommatePostDto response = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        given(roommateService.getRoommateBoardList(any(), any(), eq(1))).willReturn(List.of(response));

        // when
        ResultActions result = mockMvc.perform(get("/roommates/list")
                .param("semester", "1")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$[0].semester").value(1));
    }

    @Test
    @DisplayName("200 반환 — semester 미입력 시 전체 게시글 반환")
    void should_return_all_when_no_semester_param() throws Exception {
        // given
        ResponseRoommatePostDto r1 = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.FIRST);
        ResponseRoommatePostDto r2 = RoommateSemesterFixture.createResponseWithSemester(2026, SemesterType.SUMMER_VACATION);
        given(roommateService.getRoommateBoardList(any(), any(), eq(null))).willReturn(List.of(r1, r2));

        // when
        ResultActions result = mockMvc.perform(get("/roommates/list")
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("404 반환 — BR-686 인증된 유저 DB 미존재 시 게시글 생성 실패")
    @WithMockUser
    void should_return_404_when_user_not_found_on_create() throws Exception {
        // given
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong()))
                .willThrow(new com.example.appcenter_project.global.exception.CustomException(
                        com.example.appcenter_project.global.exception.ErrorCode.ROOMMATE_USER_NOT_FOUND));

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isNotFound());
    }
}
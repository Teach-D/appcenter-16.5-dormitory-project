package com.example.appcenter_project.domain.roommate.controller;

import com.example.appcenter_project.domain.roommate.enums.SemesterType;
import com.example.appcenter_project.domain.roommate.fixture.RoommateMultiSemesterFixture;
import com.example.appcenter_project.domain.roommate.service.MyRoommateService;
import com.example.appcenter_project.domain.roommate.service.RoommateQueryService;
import com.example.appcenter_project.domain.roommate.service.RoommateService;
import com.example.appcenter_project.global.exception.CustomException;
import com.example.appcenter_project.global.exception.ErrorCode;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoommateController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoommateMultiSemesterControllerTest {

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
    @DisplayName("201 반환 — AC-1 현재 학기에 게시글 없는 유저 POST /roommates 정상 생성")
    @WithMockUser
    void should_return_201_when_no_existing_board_in_current_semester() throws Exception {
        // given
        var response = RoommateMultiSemesterFixture.createBoardResponse(42L, 2026, SemesterType.FIRST);
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong())).willReturn(response);

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isCreated());
    }

    @Test
    @DisplayName("409 반환 — AC-2 BR-710 현재 학기에 이미 게시글이 있는 유저 POST /roommates")
    @WithMockUser
    void should_return_409_when_board_already_exists_in_current_semester() throws Exception {
        // given
        given(roommateService.createRoommateCheckListandBoard(any(), anyLong()))
                .willThrow(new CustomException(ErrorCode.ROOMMATE_BOARD_ALREADY_EXISTS));

        // when
        ResultActions result = mockMvc.perform(post("/roommates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isConflict());
    }

    @Test
    @DisplayName("200 반환 — AC-4 PUT /roommates/{boardId} 소유자가 수정 요청")
    @WithMockUser
    void should_return_200_when_owner_updates_board_by_boardId() throws Exception {
        // given
        var response = RoommateMultiSemesterFixture.createBoardResponse(10L, 2026, SemesterType.FIRST);
        given(roommateService.updateRoommateChecklistAndBoard(any(), anyLong(), anyLong(), any()))
                .willReturn(response);

        // when
        ResultActions result = mockMvc.perform(put("/roommates/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isOk());
    }

    @Test
    @DisplayName("403 반환 — AC-5 PUT /roommates/{boardId} 소유자 불일치")
    @WithMockUser
    void should_return_403_when_non_owner_updates_board_by_boardId() throws Exception {
        // given
        given(roommateService.updateRoommateChecklistAndBoard(any(), anyLong(), anyLong(), any()))
                .willThrow(new CustomException(ErrorCode.ROOMMATE_UPDATE_NOT_ALLOWED));

        // when
        ResultActions result = mockMvc.perform(put("/roommates/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("404 반환 — AC-4 PUT /roommates/{boardId} boardId 없음")
    @WithMockUser
    void should_return_404_when_board_not_found_on_update() throws Exception {
        // given
        given(roommateService.updateRoommateChecklistAndBoard(any(), anyLong(), anyLong(), any()))
                .willThrow(new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND));

        // when
        ResultActions result = mockMvc.perform(put("/roommates/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("204 반환 — AC-6 DELETE /roommates/{boardId} 소유자가 삭제 요청")
    @WithMockUser
    void should_return_204_when_owner_deletes_board_by_boardId() throws Exception {
        // given
        willDoNothing().given(roommateService).deleteRoommateBoard(anyLong(), anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/roommates/10"));

        // then
        result.andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("403 반환 — AC-6 DELETE /roommates/{boardId} 소유자 불일치")
    @WithMockUser
    void should_return_403_when_non_owner_deletes_board_by_boardId() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.ROOMMATE_DELETE_NOT_ALLOWED))
                .given(roommateService).deleteRoommateBoard(anyLong(), anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/roommates/10"));

        // then
        result.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("404 반환 — AC-6 DELETE /roommates/{boardId} boardId 없음")
    @WithMockUser
    void should_return_404_when_board_not_found_on_delete() throws Exception {
        // given
        willThrow(new CustomException(ErrorCode.ROOMMATE_BOARD_NOT_FOUND))
                .given(roommateService).deleteRoommateBoard(anyLong(), anyLong());

        // when
        ResultActions result = mockMvc.perform(delete("/roommates/999"));

        // then
        result.andExpect(status().isNotFound());
    }
}

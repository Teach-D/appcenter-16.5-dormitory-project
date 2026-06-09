package com.example.appcenter_project.domain.studentIdDisclosure.controller;

import com.example.appcenter_project.domain.studentIdDisclosure.dto.request.RequestCreateDisclosureDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureAcceptDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureSendDto;
import com.example.appcenter_project.domain.studentIdDisclosure.dto.response.ResponseDisclosureStatusDto;
import com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture;
import com.example.appcenter_project.domain.studentIdDisclosure.service.StudentIdDisclosureRequestService;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture.REQUEST_ID;
import static com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture.REQUESTER_STUDENT_NUMBER;
import static com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture.ROOM_ID;
import static com.example.appcenter_project.domain.studentIdDisclosure.fixture.StudentIdDisclosureFixture.TARGET_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@WebMvcTest(StudentIdDisclosureController.class)
@AutoConfigureMockMvc(addFilters = false)
class StudentIdDisclosureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentIdDisclosureRequestService disclosureService;

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
    // [1] POST /student-id-disclosures — 학번 공개 요청 발송
    // =========================================================================

    @Test
    @DisplayName("201 반환 — POST /student-id-disclosures 정상 요청")
    void should_return_201_when_sendRequest_valid() throws Exception {
        RequestCreateDisclosureDto request = StudentIdDisclosureFixture.createSendRequestDto();
        ResponseDisclosureSendDto response = StudentIdDisclosureFixture.createSendResponse();
        given(disclosureService.sendRequest(nullable(Long.class), any())).willReturn(response);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/student-id-disclosures")

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

        result.andExpect(MockMvcResultMatchers.status().isCreated())
              .andExpect(MockMvcResultMatchers.jsonPath("$.requestId").value(REQUEST_ID));
    }

    // =========================================================================
    // [2] DELETE /student-id-disclosures/{requestId} — 요청 취소
    // =========================================================================

    @Test
    @DisplayName("204 반환 — DELETE /student-id-disclosures/{requestId} 취소 성공")
    void should_return_200_when_cancel_valid() throws Exception {
        willDoNothing().given(disclosureService).cancel(nullable(Long.class), eq(REQUEST_ID));

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.delete("/student-id-disclosures/{requestId}", REQUEST_ID)

                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    // =========================================================================
    // [3] POST /student-id-disclosures/{requestId}/accept — 요청 수락
    // =========================================================================

    @Test
    @DisplayName("200 반환 — POST /student-id-disclosures/{requestId}/accept 수락 성공")
    void should_return_200_when_accept_valid() throws Exception {
        ResponseDisclosureAcceptDto response = StudentIdDisclosureFixture.createAcceptResponse();
        given(disclosureService.accept(nullable(Long.class), eq(REQUEST_ID))).willReturn(response);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.post("/student-id-disclosures/{requestId}/accept", REQUEST_ID)

                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.requesterStudentNumber").value(REQUESTER_STUDENT_NUMBER));
    }

    // =========================================================================
    // [4] GET /student-id-disclosures/status — 상태 조회
    // =========================================================================

    @Test
    @DisplayName("200 반환 — GET /student-id-disclosures/status 정상 조회")
    void should_return_200_when_getStatus_valid() throws Exception {
        ResponseDisclosureStatusDto response = StudentIdDisclosureFixture.createNoneStatusResponse();
        given(disclosureService.getStatus(nullable(Long.class), eq(ROOM_ID), eq(TARGET_ID))).willReturn(response);

        ResultActions result = mockMvc.perform(
                MockMvcRequestBuilders.get("/student-id-disclosures/status")

                        .param("roomId", String.valueOf(ROOM_ID))
                        .param("targetId", String.valueOf(TARGET_ID))
                        .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(MockMvcResultMatchers.status().isOk())
              .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NONE"));
    }
}

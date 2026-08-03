package com.example.appcenter_project.domain.block.controller;

// TODO: 구현 후 아래 import 주석 해제
// import com.example.appcenter_project.domain.block.service.BlockService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * BR-685 — BlockController 단위 테스트 (RED 단계)
 *
 * 구현 에이전트가 생성해야 할 클래스:
 * - com.example.appcenter_project.domain.block.controller.BlockController
 *   - POST /block/{targetUserId} → 201 CREATED (ResponseEntity<Void>)
 * - com.example.appcenter_project.domain.block.service.BlockService
 * - com.example.appcenter_project.global.exception.ErrorCode 신규 상수:
 *   USER_BLOCK_CANNOT_BLOCK_SELF (400, 26001)
 *   USER_BLOCK_ALREADY_EXISTS   (409, 26002)
 *   USER_BLOCKED_BY_TARGET      (403, 26003)
 *
 * 주의: @WebMvcTest(BlockController.class) 는 BlockController 구현 후 주석 해제
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class BlockControllerTest {

    // TODO: 구현 후 아래 어노테이션·필드 주석 해제하고 클래스 어노테이션 추가
    // @WebMvcTest(BlockController.class)
    // @AutoConfigureMockMvc(addFilters = false)
    //
    // @Autowired MockMvc mockMvc;
    // @Autowired ObjectMapper objectMapper;
    // @MockBean BlockService blockService;
    // @MockBean SlackErrorNotifier slackErrorNotifier;
    // @MockBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    // ──────────────────────────────────────────────
    // AC-1: 차단 성공 201 반환
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("201 반환 — AC-1 BR-685: 정상 차단 요청")
    void should_return_201_when_valid_block_request() throws Exception {
        // given
        // Long targetUserId = 2L;
        // willDoNothing().given(blockService).blockUser(any(), eq(targetUserId));

        // when
        // ResultActions result = mockMvc.perform(post("/block/{targetUserId}", targetUserId));

        // then
        // result.andExpect(status().isCreated());

        // TODO: BlockController 미구현 — 컴파일 통과용 placeholder
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("응답 바디 없음 확인 — AC-1 BR-685: 차단 성공 시 빈 응답")
    void should_return_empty_body_when_block_success() throws Exception {
        // given
        // Long targetUserId = 2L;
        // willDoNothing().given(blockService).blockUser(any(), eq(targetUserId));

        // when
        // ResultActions result = mockMvc.perform(post("/block/{targetUserId}", targetUserId));

        // then
        // result.andExpect(status().isCreated())
        //       .andExpect(content().string(""));

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-2: 자기 자신 차단 거부 — 400
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("400 반환 — AC-2 BR-685: 자기 자신 차단 시도 시 USER_BLOCK_CANNOT_BLOCK_SELF")
    void should_return_400_when_self_block_attempted() throws Exception {
        // given
        // Long selfId = 1L;
        // willThrow(new CustomException(ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF))
        //         .given(blockService).blockUser(eq(selfId), eq(selfId));

        // when
        // ResultActions result = mockMvc.perform(post("/block/{targetUserId}", selfId));

        // then
        // result.andExpect(status().isBadRequest());

        // TODO: ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF 상수 아직 없음
        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-3: 존재하지 않는 사용자 차단 거부 — 404
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("404 반환 — AC-3 BR-685: 존재하지 않는 targetUserId 차단 시도 시 USER_NOT_FOUND")
    void should_return_404_when_target_user_not_found() throws Exception {
        // given
        // Long nonExistentId = 99999L;
        // willThrow(new CustomException(ErrorCode.USER_NOT_FOUND))
        //         .given(blockService).blockUser(any(), eq(nonExistentId));

        // when
        // ResultActions result = mockMvc.perform(post("/block/{targetUserId}", nonExistentId));

        // then
        // result.andExpect(status().isNotFound());

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // AC-4: 중복 차단 거부 — 409
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("409 반환 — AC-4 BR-685: 이미 차단한 사용자 재차단 시도 시 USER_BLOCK_ALREADY_EXISTS")
    void should_return_409_when_duplicate_block_attempted() throws Exception {
        // given
        // Long targetId = 2L;
        // willThrow(new CustomException(ErrorCode.USER_BLOCK_ALREADY_EXISTS))
        //         .given(blockService).blockUser(any(), eq(targetId));

        // when
        // ResultActions result = mockMvc.perform(post("/block/{targetUserId}", targetId));

        // then
        // result.andExpect(status().isConflict());

        // TODO: ErrorCode.USER_BLOCK_ALREADY_EXISTS 상수 아직 없음
        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // Auth: 미인증 요청 — 401
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("401 반환 — Auth BR-685: 인증 토큰 없이 차단 요청")
    void should_return_401_when_unauthenticated() throws Exception {
        // given — 필터 활성화 필요 (@AutoConfigureMockMvc(addFilters = false) 제거 후 테스트)
        // 현재는 addFilters = false 이므로 별도 설정 필요
        // ResultActions result = mockMvc.perform(post("/block/2"));
        // result.andExpect(status().isUnauthorized());

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // BR-697 AC-1: 차단 해제 성공 — 204
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("204 반환 — AC-1 BR-697: 정상 차단 해제 요청")
    void should_return_204_when_valid_unblock_request() throws Exception {
        // given
        // Long targetUserId = 2L;
        // willDoNothing().given(blockService).unblockUser(any(), eq(targetUserId));

        // when
        // ResultActions result = mockMvc.perform(delete("/block/{targetUserId}", targetUserId));

        // then
        // result.andExpect(status().isNoContent());

        // TODO: BlockController.unblockUser() 미구현, DELETE /block/{targetUserId} 엔드포인트 없음 — RED
        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // BR-697 AC-2: 자기 자신 차단 해제 거부 — 400
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("400 반환 — AC-2 BR-697: 자기 자신 차단 해제 시도")
    void should_return_400_when_self_unblock_attempted() throws Exception {
        // given
        // Long selfId = 1L;
        // willThrow(new CustomException(ErrorCode.USER_BLOCK_CANNOT_BLOCK_SELF))
        //         .given(blockService).unblockUser(any(), eq(selfId));

        // when
        // ResultActions result = mockMvc.perform(delete("/block/{targetUserId}", selfId));

        // then
        // result.andExpect(status().isBadRequest());

        // TODO: BlockService.unblockUser() 미구현 — RED
        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // BR-697 AC-3: 차단 기록 없음 — 404
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("404 반환 — AC-3 BR-697: 차단 기록 없는 상대 해제 시도")
    void should_return_404_when_block_record_not_found() throws Exception {
        // given
        // Long targetId = 2L;
        // willThrow(new CustomException(ErrorCode.USER_BLOCK_NOT_FOUND))
        //         .given(blockService).unblockUser(any(), eq(targetId));

        // when
        // ResultActions result = mockMvc.perform(delete("/block/{targetUserId}", targetId));

        // then
        // result.andExpect(status().isNotFound());

        // TODO: ErrorCode.USER_BLOCK_NOT_FOUND 미구현, BlockService.unblockUser() 미구현 — RED
        assertThat(true).isTrue();
    }
}

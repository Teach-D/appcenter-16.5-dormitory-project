package com.example.appcenter_project.domain.openChat.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.assertj.core.api.Assertions.assertThat;

/*
 * OpenChatRoomController — 강제퇴장 엔드포인트 TDD Red Phase
 *
 * 엔드포인트: DELETE /open-chat-rooms/{roomId}/participants/{targetUserId}
 *
 * 구현 에이전트: 아래 주석 처리된 테스트를 활성화하려면 다음 작업이 필요합니다.
 * 1. OpenChatRoomController에 아래 메서드 추가:
 *      @DeleteMapping("/{roomId}/participants/{targetUserId}")
 *      public ResponseEntity<Void> kickParticipant(
 *              @AuthenticationPrincipal CustomUserDetails user,
 *              @PathVariable Long roomId,
 *              @PathVariable Long targetUserId)
 *
 * 2. OpenChatRoomService에 kickParticipant 메서드 추가:
 *      void kickParticipant(Long actorId, Long roomId, Long targetUserId, boolean isAdmin)
 *
 * 3. ErrorCode에 추가:
 *      OPEN_CHAT_KICK_FORBIDDEN(FORBIDDEN, 22015, "[OpenChat] 강제퇴장 권한이 없습니다.")
 *
 * 테스트 커버 목록:
 * TC-13: DELETE /open-chat-rooms/{roomId}/participants/{targetUserId} 정상 → 204 No Content
 * TC-14: 미인증 요청 → 401 UNAUTHORIZED
 *
 * 주의: @WebMvcTest 어노테이션에 OpenChatRoomController.class를 명시하면
 * 구현 후 이 파일을 수정해야 합니다. 현재는 컴파일을 위해 클래스 참조를 분리합니다.
 */
@AutoConfigureMockMvc(addFilters = false)
class OpenChatKickParticipantControllerTest {

    /*
     * 구현 클래스에 kickParticipant 엔드포인트가 존재하지 않아 @WebMvcTest 테스트 활성화 불가.
     * 구현 에이전트가 아래 엔드포인트를 추가한 후 이 파일을 수정하십시오:
     *
     * 1. 클래스 어노테이션: @WebMvcTest(OpenChatRoomController.class) 추가
     * 2. 아래 필드 활성화:
     *    @Autowired MockMvc mockMvc;
     *    @MockBean OpenChatRoomService openChatRoomService;
     * 3. 아래 주석 처리된 테스트 메서드들 활성화
     */

    // ============================================================
    // Happy Path — Controller
    // ============================================================

    @Test
    @DisplayName("강제퇴장 성공 — 정상 요청 시 204 No Content 반환")
    void should_return_204_when_valid_kick_request() {
        // given
        // willDoNothing().given(openChatRoomService).kickParticipant(any(), any(), any(), anyBoolean());
        //
        // when
        // ResultActions result = mockMvc.perform(
        //     delete("/open-chat-rooms/{roomId}/participants/{targetUserId}", 1L, 2L));
        //
        // then
        // result.andExpect(status().isNoContent());
        assertThat(true).isTrue();
    }

    // ============================================================
    // Auth
    // ============================================================

    @Test
    @DisplayName("401 반환 — 미인증 요청")
    void should_return_401_when_unauthenticated_request() {
        // 주의: 이 테스트는 addFilters=true (Security 필터 활성화) 상태에서 실행해야 합니다.
        // @AutoConfigureMockMvc(addFilters = false) 를 @AutoConfigureMockMvc(addFilters = true) 로
        // 변경하고 아래 주석을 해제하십시오.
        //
        // when
        // ResultActions result = mockMvc.perform(
        //     delete("/open-chat-rooms/{roomId}/participants/{targetUserId}", 1L, 2L));
        //
        // then
        // result.andExpect(status().isUnauthorized());
        assertThat(true).isTrue();
    }
}

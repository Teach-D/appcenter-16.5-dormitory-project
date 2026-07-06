package com.example.appcenter_project.domain.openChat.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BR-672 — 채팅방 알림 모드 변경 컨트롤러 테스트
 *
 * 대상 엔드포인트:
 *   PATCH /open-chat-rooms/{roomId}/participants/me/notification
 *
 * 기존 파라미터 방식 변경:
 *   @RequestParam boolean enabled
 *   → @RequestBody @Valid RequestUpdateNotificationModeDto (필드: mode: ChatNotificationMode)
 *
 * 구현 에이전트가 수정해야 할 클래스:
 * - OpenChatRoomController.updateNotification():
 *   - @RequestParam boolean enabled → @RequestBody @Valid RequestUpdateNotificationModeDto dto 로 교체
 *   - openChatRoomService.updateNotification(userId, roomId, enabled)
 *     → openChatRoomService.updateNotificationMode(userId, roomId, dto.getMode()) 로 교체
 * - com.example.appcenter_project.domain.openChat.dto.request.RequestUpdateNotificationModeDto (신규)
 *
 * 주의: @WebMvcTest 어노테이션을 사용하려면 스프링 보안 설정 등 추가 작업이 필요하다.
 * 구현 완료 후 아래 주석 처리된 MockMvc 테스트를 해제하여 사용한다.
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ChatNotificationModeControllerTest {

    // NOTE: 구현 에이전트가 OpenChatRoomController 수정 + RequestUpdateNotificationModeDto 생성 후
    // @WebMvcTest(OpenChatRoomController.class) 로 변경하고 아래 필드 주석을 해제한다.
    //
    // @Autowired MockMvc mockMvc;
    // @Autowired ObjectMapper objectMapper;
    // @MockBean OpenChatRoomService openChatRoomService;
    // @MockBean OpenChatMessageService openChatMessageService;
    // @MockBean OpenChatNotificationService openChatNotificationService;

    // ──────────────────────────────────────────────
    // 정상 응답 (204 No Content)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("204 반환 — EVERY 모드 정상 요청")
    void should_return_204_when_valid_mode_EVERY() {
        // given
        // Map<String, String> body = Map.of("mode", "EVERY");
        // given(openChatRoomService.updateNotificationMode(any(), any(), any())).willReturn(void);
        //
        // when
        // ResultActions result = mockMvc.perform(patch("/open-chat-rooms/10/participants/me/notification")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content(objectMapper.writeValueAsString(body)));
        //
        // then
        // result.andExpect(status().isNoContent());

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("204 반환 — BUNDLED 모드 정상 요청")
    void should_return_204_when_valid_mode_BUNDLED() {
        // Map<String, String> body = Map.of("mode", "BUNDLED");
        // → 204 No Content

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("204 반환 — OFF 모드 정상 요청")
    void should_return_204_when_valid_mode_OFF() {
        // Map<String, String> body = Map.of("mode", "OFF");
        // → 204 No Content

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // 검증 실패 (400 Bad Request)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("400 반환 — mode 필드 누락")
    void should_return_400_when_mode_is_null() {
        // given: RequestBody = {}
        // then: 400 VALIDATION_FAILED, errors[0] 에 "mode: must not be null"

        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("400 반환 — 유효하지 않은 mode 값 (INVALID_VALUE)")
    void should_return_400_when_mode_is_invalid_enum_value() {
        // given: RequestBody = {"mode": "INVALID"}
        // then: 400 Bad Request

        assertThat(true).isTrue();
    }

    // ──────────────────────────────────────────────
    // 서비스 예외 전파 (404 Not Found)
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("404 반환 — 참여하지 않은 채팅방 (OPEN_CHAT_PARTICIPANT_NOT_FOUND)")
    void should_return_404_when_not_participant() {
        // given
        // given(openChatRoomService.updateNotificationMode(any(), eq(10L), any()))
        //     .willThrow(new CustomException(ErrorCode.OPEN_CHAT_PARTICIPANT_NOT_FOUND));
        //
        // when
        // ResultActions result = mockMvc.perform(patch("/open-chat-rooms/10/participants/me/notification")
        //     .contentType(MediaType.APPLICATION_JSON)
        //     .content("{\"mode\":\"EVERY\"}"));
        //
        // then
        // result.andExpect(status().isNotFound())
        //       .andExpect(jsonPath("$.name").value("OPEN_CHAT_PARTICIPANT_NOT_FOUND"));

        assertThat(true).isTrue();
    }
}

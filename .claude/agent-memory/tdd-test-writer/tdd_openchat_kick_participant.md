---
name: openChat 강제퇴장 TDD
description: 강제퇴장 기능 14개 케이스 BR 커버리지, 파일 위치, 신규 ErrorCode
type: project
---

## 대상 기능

`DELETE /open-chat-rooms/{roomId}/participants/{targetUserId}` — 강제퇴장

## 테스트 파일

- `src/test/java/.../openChat/service/OpenChatKickParticipantServiceTest.java` (12개)
- `src/test/java/.../openChat/controller/OpenChatKickParticipantControllerTest.java` (2개)

## BR 커버리지 (총 14개)

| TC | 테스트 메서드명 | 구분 |
|----|----------------|------|
| TC-01 | should_delete_participant_when_host_kicks_normal_participant | Happy Path |
| TC-02 | should_delete_participant_when_admin_kicks_normal_participant | Happy Path |
| TC-03 | should_transfer_host_when_admin_kicks_host_with_remaining_participants | Happy Path |
| TC-04 | should_delete_room_when_admin_kicks_last_host_in_non_official_room | Happy Path |
| TC-05 | should_keep_room_when_admin_kicks_last_host_in_official_room | Happy Path |
| TC-06 | should_send_system_message_when_participant_is_kicked | Happy Path |
| TC-07 | should_throw_CustomException_when_non_host_user_attempts_kick | Business Rule |
| TC-08 | should_throw_CustomException_when_host_attempts_self_kick | Business Rule |
| TC-09 | should_throw_CustomException_when_host_attempts_to_kick_another_host | Business Rule |
| TC-10 | should_throw_CustomException_when_room_not_found | Error |
| TC-11 | should_throw_CustomException_when_target_is_not_participant | Error |
| TC-12 | should_throw_CustomException_when_actor_is_not_participant_of_room | Error |
| TC-13 | should_return_204_when_valid_kick_request | Controller Happy Path |
| TC-14 | should_return_401_when_unauthenticated_request | Controller Auth |

## 신규 ErrorCode (구현 에이전트 필요)

```java
OPEN_CHAT_KICK_FORBIDDEN(FORBIDDEN, 22015, "[OpenChat] 강제퇴장 권한이 없습니다.")
```

## 신규 서비스 메서드 시그니처

```java
void kickParticipant(Long actorId, Long roomId, Long targetUserId, boolean isAdmin)
```
- `isAdmin=true`: ADMIN 역할 — 모든 방에서 퇴장 가능, 방장도 퇴장 가능
- `isAdmin=false`: USER 역할 — actorId == room.hostUserId 이어야만 퇴장 가능, targetUserId != hostUserId 이어야만 가능

## 신규 컨트롤러 엔드포인트

```java
@DeleteMapping("/{roomId}/participants/{targetUserId}")
public ResponseEntity<Void> kickParticipant(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long roomId,
        @PathVariable Long targetUserId)
```

## 작성 패턴 (이번 작업 특이사항)

- 기존 `OpenChatRoomControllerTest`는 `@WebMvcTest` 없이 순수 클래스만 선언 (구현 전 상태)
- 강제퇴장 테스트도 동일하게 placeholder + 주석 처리 패턴 적용
- 컨트롤러 테스트에서 TC-14(401)는 `addFilters=true` 로 전환해야 실제 검증 가능

**왜:** kickParticipant 메서드가 존재하지 않아 직접 호출하면 컴파일 오류 발생
**적용 방법:** 구현 에이전트가 메서드를 추가한 후 주석 해제

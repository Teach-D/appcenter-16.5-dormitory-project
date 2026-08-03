# BR-697 사용자 차단 해제 기능

## 기능 요약

인증된 사용자가 자신이 차단한 특정 상대방을 차단 해제한다.
해제 후 해당 상대방은 다시 PERSONAL 채팅방 생성 및 메시지 전송이 가능해진다.
관련 차단 기능: BR-685

---

## 동작 명세

### 차단 해제 (`DELETE /block/{targetUserId}`)

**정상 흐름**
1. 인증된 사용자(A)가 `targetUserId`(B)를 전송
2. A ≠ B 검증
3. `UserBlock(blockerId=A, blockedId=B)` 존재 여부 검증
4. 해당 `UserBlock` 레코드 삭제
5. HTTP 204 NO_CONTENT (빈 응답)

---

## 도메인 데이터

### UserBlock (기존 엔티티 재사용)

| 필드 | 타입 | 제약 |
|---|---|---|
| id | Long | PK, auto-generated |
| blockerId | Long | 차단한 사용자 ID, not null |
| blockedId | Long | 차단당한 사용자 ID, not null |
| createdDate | LocalDateTime | BaseTimeEntity |

### UserBlockRepository (기존 레포지토리 확장)

- `deleteByBlockerIdAndBlockedId(Long blockerId, Long blockedId): void` — 차단 레코드 삭제

### BlockService (기존 서비스 확장)

- `unblockUser(Long requesterId, Long targetId)` — 차단 해제

---

## 비즈니스 규칙 / 제약

- 차단 해제는 본인이 차단한 상대에 대해서만 가능 (blockerId = 요청자)
- 차단 해제 후 기존 PERSONAL 채팅방에서 상대방이 메시지 전송이 다시 허용된다 (별도 로직 없음 — 차단 레코드가 없으면 검증 통과)
- 자기 자신은 차단 해제 대상이 될 수 없다

---

## 예외 · 경계 상황

| 상황 | 응답 |
|---|---|
| 자기 자신을 차단 해제 시도 | 400 `USER_BLOCK_CANNOT_BLOCK_SELF` |
| 차단 기록이 없는 상대 해제 시도 | 404 `USER_BLOCK_NOT_FOUND` |

---

## 비목표 (Non-goals)

- 차단 목록 조회 API
- 차단 해제 시 상대방에게 알림 발송
- targetUser 존재 여부 검증 (차단 레코드 존재 검증으로 충분)
- 인증/로깅/캐싱

---

## 수용 기준 (Acceptance Criteria)

### 차단 해제 API

**AC-1** 정상 해제
- Given 인증된 사용자 A, A가 B를 차단한 상태
- When `DELETE /block/{B.id}`
- Then HTTP 204, `UserBlock(blockerId=A, blockedId=B)` 삭제됨

**AC-2** 자기 자신 차단 해제 거부
- Given 인증된 사용자 A
- When `DELETE /block/{A.id}`
- Then HTTP 400 `USER_BLOCK_CANNOT_BLOCK_SELF`

**AC-3** 차단 기록 없는 상대 해제 거부
- Given 인증된 사용자 A, A가 B를 차단하지 않은 상태
- When `DELETE /block/{B.id}`
- Then HTTP 404 `USER_BLOCK_NOT_FOUND`

**AC-4** 차단 해제 후 PERSONAL 채팅방 생성 가능
- Given A가 B를 차단 해제한 상태
- When B가 `POST /open-chat-rooms/personal` with `targetUserId=A`
- Then HTTP 201 (정상 생성)

### 신규 ErrorCode

| 코드명 | 상태 | 번호 | 메시지 |
|---|---|---|---|
| `USER_BLOCK_NOT_FOUND` | 404 NOT_FOUND | 26004 | [Block] 차단 기록이 존재하지 않습니다. |

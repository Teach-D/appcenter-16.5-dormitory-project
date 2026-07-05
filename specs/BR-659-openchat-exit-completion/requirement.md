# BR-659 — 오픈채팅 퇴장 기능 미구현 항목 완성

## 기능 요약

기존 오픈채팅 퇴장(자진/강제) API에서 누락된 항목을 완성한다.  
① 강제퇴장 사유(enum) 파라미터 추가 ② 운영 로그(logger) 기록 ③ 방장 단독 자진 퇴장 시 방 하드 삭제 ④ 관리자가 방장을 강퇴할 때 새 방장 지정 필수 + 후속 처리.

---

## 동작 명세

### 1. 강제퇴장 사유 파라미터 추가 (`kickParticipant`)

- **입력**: `reason: KickReason` (요청 쿼리 파라미터, 필수)
- **처리**: 기존 권한 체크 및 participant 삭제 로직은 동일. reason 값을 운영 로그에 포함.
- **출력**: 변경 없음 (204 No Content)

### 2. 관리자가 방장을 강퇴할 때 신규 파라미터 (`kickParticipant`)

- **입력**: `newHostUserId: Long` (요청 쿼리 파라미터, 조건부 필수)
- **처리 흐름**:
  1. ADMIN이 방장(`isHost=true`)을 강퇴 요청
  2. 방에 다른 참여자가 있으면:
     - `newHostUserId` 없으면 → `OPEN_CHAT_NEW_HOST_REQUIRED` 에러
     - `newHostUserId` 있으면 → 해당 참여자에게 `grantHost()` 후 target participant 삭제
  3. 방에 다른 참여자가 없으면 (방장 혼자):
     - `newHostUserId` 무시, participant + room 하드 삭제
- **출력**: 204 No Content

### 3. 방장 단독 자진 퇴장 시 방 하드 삭제 (`leaveRoom`)

- **현행**: 방장 혼자 남았을 때 `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` 예외 발생
- **변경**: 비공식(user-created) 방에서 방장이 마지막 참여자이면 participant + room 하드 삭제 후 `roomDeleted: true` 반환
- **처리 흐름**:
  1. `newHostUserId == null` && `self.isHost() == true` 진입
  2. `lockedParticipants.size() == 1` (자신만 남음) && `room.isOfficial() == false`
     → `openChatParticipantRepository.deleteAll(lockedParticipants)` + `openChatRoomRepository.delete(room)`
     → `ResponseLeaveOpenChatRoomDto(roomDeleted: true)` 반환
  3. 공식 방(`isOfficial == true`)에서 마지막 참여자 퇴장:
     → 기존 예외(`OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE`) 유지
  4. 방장이 혼자지만 다른 방장이 있는 경우(multi-host 구조):
     → 기존 흐름 유지 (방장 수 > 1이면 자유 퇴장)

### 4. 운영 로그 기록

퇴장 처리 성공 직후, 아래 정보를 `logger.info()`로 기록한다.

| 필드 | 내용 |
|------|------|
| exitType | VOLUNTARY / HOST_KICK / ADMIN_KICK |
| roomId | 방 ID |
| targetUserId | 퇴장 대상 ID |
| actorId | 실행자 ID (자진 퇴장 시 targetUserId와 동일) |
| reason | KickReason enum 값 (자진 퇴장 시 null) |
| processedAt | `Instant.now()` |

적용 위치: `leaveRoom`, `kickParticipant` 처리 완료 시점.

---

## 도메인 데이터

### KickReason (신규 enum)

```
SPAM              // 도배/광고
ABUSE             // 욕설/비방
IMPERSONATION     // 사칭
REPORT_ACCUMULATED // 신고 누적
OTHER             // 기타
```

위치: `domain/openChat/enums/KickReason.java`

### 변경되는 기존 데이터

- `OpenChatParticipant`: 변경 없음 (강퇴 상태 필드 추가 없음 — 재입장 차단은 이번 범위 밖)
- `OpenChatRoom`: 변경 없음 (status 필드 추가 없음 — 방 종료는 하드 삭제로 처리)
- `ResponseLeaveOpenChatRoomDto`: `roomDeleted` 필드 이미 존재 — 그대로 활용

---

## 비즈니스 규칙 / 제약

### kickParticipant 변경 규칙

1. `reason`은 모든 강제퇴장 요청(방장·관리자 불문)에서 필수값이다.
2. `newHostUserId`는 ADMIN이 방장을 강퇴할 때만 의미 있다. 방장이 일반 참여자를 강퇴할 때 `newHostUserId`가 전달되어도 무시한다.
3. ADMIN이 방장을 강퇴할 때:
   - 다른 참여자가 있으면 `newHostUserId` 필수
   - `newHostUserId`가 방 미참여자이면 → `OPEN_CHAT_PARTICIPANT_NOT_FOUND`
   - `newHostUserId`가 이미 방장이면 → `OPEN_CHAT_ALREADY_HOST`
   - 다른 참여자가 없으면 `newHostUserId` 불필요, 방 삭제

### leaveRoom 변경 규칙

4. 비공식 방에서 마지막 참여자(== 방장)가 자진 퇴장하면 방을 하드 삭제한다.
5. 공식 방(`isOfficial=true`)은 참여자가 없어도 방이 유지되어야 하므로 기존 예외를 유지한다.
6. multi-host 구조(방장이 둘 이상)에서 방장 중 한 명이 퇴장하는 것은 영향 없음(기존 흐름).

---

## 예외 · 경계 상황

| 상황 | 기대 동작 |
|------|-----------|
| `reason` 없이 강제퇴장 요청 | 400 BAD_REQUEST |
| ADMIN이 방장 강퇴 시 `newHostUserId` 누락 + 다른 참여자 있음 | 400 BAD_REQUEST (`OPEN_CHAT_NEW_HOST_REQUIRED`) |
| ADMIN이 방장 강퇴 시 `newHostUserId`가 방 미참여자 | 404 (`OPEN_CHAT_PARTICIPANT_NOT_FOUND`) |
| ADMIN이 방장 강퇴 시 `newHostUserId`가 이미 방장 | 409 (`OPEN_CHAT_ALREADY_HOST`) |
| 비공식 방 마지막 방장 자진 퇴장 | 200 + `roomDeleted: true` |
| 공식 방 마지막 방장 자진 퇴장 | 403 (`OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE`) |
| `newHostUserId` 없이 leaveRoom 요청 (방장 + 다른 참여자 있음) | 기존 흐름 (`OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE`) |

---

## 비목표 (Non-goals)

- **강퇴 후 재입장 차단**: `OpenChatParticipant`에 banned 상태 필드 추가 안 함
- **DB 운영 로그 테이블**: `open_chat_exit_log` 엔티티 생성 안 함. `logger.info()` 만 사용
- **방 status 필드**: `OpenChatRoom`에 OPEN/CLOSED 상태 enum 추가 안 함. 종료는 하드 삭제
- **WebSocket 구독 강제 해제**: 서버 측 STOMP 세션 강제 종료 로직 추가 안 함
- **강퇴 사유 자유입력(String)**: enum 선택지만 지원
- **기존 `kickParticipant` 권한 체크 변경**: 방장·ADMIN 권한 조건은 현행 유지

---

## 수용 기준 (Acceptance Criteria)

### KickReason enum

- **Given** `KickReason` enum이 존재할 때 **When** 각 상수(SPAM, ABUSE, IMPERSONATION, REPORT_ACCUMULATED, OTHER)를 조회 **Then** 컴파일 오류 없이 반환된다

### kickParticipant — reason 필수

- **Given** 방장이 일반 참여자를 강퇴할 때 **When** `reason=SPAM` 포함 요청 **Then** 204 반환, 시스템 메시지 발송
- **Given** 강제퇴장 요청에 **When** `reason` 없으면 **Then** 400 반환

### kickParticipant — ADMIN이 방장 강퇴 (다른 참여자 있음)

- **Given** 방에 방장A + 일반참여자B, ADMIN이 방장A를 강퇴 요청 **When** `newHostUserId=B, reason=ABUSE` **Then** B에게 방장 권한 부여, A participant 삭제, 시스템 메시지 발송
- **Given** 방에 방장A + 일반참여자B, ADMIN이 방장A를 강퇴 요청 **When** `newHostUserId` 없음 **Then** `OPEN_CHAT_NEW_HOST_REQUIRED` 에러

### kickParticipant — ADMIN이 방장 강퇴 (마지막 참여자)

- **Given** 방에 방장A만 존재, ADMIN이 방장A를 강퇴 요청 **When** `reason=OTHER` **Then** participant 삭제 + room 삭제

### leaveRoom — 비공식 방 마지막 방장 자진 퇴장

- **Given** 비공식 방에 방장A 혼자 **When** `leaveRoom(roomId, A, null)` **Then** participant + room 모두 삭제, `roomDeleted: true` 반환
- **Given** 비공식 방에 방장A + 참여자B **When** `leaveRoom(roomId, A, null)` **Then** `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` (기존 동작 유지)

### leaveRoom — 공식 방 마지막 방장 자진 퇴장

- **Given** 공식 방에 방장A 혼자 **When** `leaveRoom(roomId, A, null)` **Then** `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` (방 유지)

### 운영 로그

- **Given** 강제퇴장 성공 시 **When** 처리 완료 **Then** `logger.info()`에 exitType/roomId/targetUserId/actorId/reason/processedAt 포함
- **Given** 자진 퇴장 성공 시 **When** 처리 완료 **Then** `logger.info()`에 exitType=VOLUNTARY/roomId/targetUserId/processedAt 포함

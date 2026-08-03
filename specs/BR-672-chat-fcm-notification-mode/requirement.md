# BR-672 채팅 FCM 푸시 알림 모드 설정

## 기능 요약

채팅 참여자가 채팅방별로 FCM 푸시 알림 수신 방식(즉시·묶음·끄기)을 설정한다.
설정에 따라 메시지 수신 시 즉시 FCM을 발송하거나, 1시간 단위로 집계해서 발송하거나, 발송하지 않는다.

## 동작 명세

### 설정 변경
- `PATCH /open-chat-rooms/{roomId}/participants/me/notification`
- 기존 `@RequestParam boolean enabled` → `@RequestBody { "mode": "EVERY" | "BUNDLED" | "OFF" }` 로 교체
- 본인이 참여한 채팅방에 대해서만 변경 가능, 변경 즉시 반영
- 응답: 204 No Content

### EVERY 모드 (즉시 발송)
1. 메시지가 저장된다 (`sendMessage`, `sendImageMessage`)
2. 해당 채팅방 참여자 중 모드가 EVERY이고 WebSocket 세션에 없는 사용자를 조회한다
3. 각 사용자의 FCM 토큰으로 FcmOutbox를 적재한다
4. FCM title: 채팅방 이름, body: 메시지 내용 (IMAGE면 "[이미지]", SYSTEM 메시지는 대상 아님)

### BUNDLED 모드 (묶음 발송)
- 기존 `OpenChatNotificationScheduler` / `OpenChatNotificationService` 사용
- `findUnreadCountsForNotification()` 쿼리 조건을 `notificationEnabled = true` → `notificationMode = BUNDLED` 로 교체
- 1시간 내 안읽은 메시지가 없으면 발송 안 함
- FCM title: 채팅방 이름, body: "새 메시지 n개"

### OFF 모드
- FCM 발송 없음. 설정 저장만 한다.

## 도메인 데이터

**`OpenChatParticipant` 변경**

| 필드 | 변경 전 | 변경 후 |
|------|---------|---------|
| `notificationEnabled` (boolean) | 제거 | — |
| `notificationMode` (ChatNotificationMode) | 없음 | 추가 |

**`ChatNotificationMode` 새 enum**
- `EVERY` — 메시지마다 즉시 FCM
- `BUNDLED` — 1시간 묶음 FCM
- `OFF` — FCM 없음

**DB 마이그레이션**
- `notification_enabled = true` → `notification_mode = 'EVERY'`
- `notification_enabled = false` → `notification_mode = 'OFF'`
- 컬럼 교체 후 기존 `notification_enabled` 컬럼 삭제

**기본값**: 채팅방 입장(join) 시 `EVERY`

## 비즈니스 규칙 / 제약

- 본인이 참여한 채팅방만 설정 변경 가능 (참여자 미존재 시 `OPEN_CHAT_NOT_PARTICIPANT`)
- WebSocket 세션에 연결 중인 사용자(OpenChatSessionRegistry 기준)는 EVERY 모드여도 FCM 발송 안 함 — 실시간으로 보고 있기 때문
- SYSTEM 메시지는 EVERY FCM 대상에서 제외
- FCM 토큰이 없는 사용자는 발송 건너뜀
- BUNDLED 묶음 FCM의 body는 기존 "새 메시지 n개" 형식 유지 + chatRoomId를 data payload에 포함

## 예외 · 경계 상황

| 상황 | 기대 동작 |
|------|----------|
| 참여하지 않은 채팅방에 설정 변경 요청 | 400 / `OPEN_CHAT_NOT_PARTICIPANT` |
| FCM 토큰 없는 사용자 | 발송 건너뜀 (예외 발생 안 함) |
| BUNDLED 모드인데 1시간 내 새 메시지 없음 | FCM Outbox 미적재 |
| 동일 모드로 재설정 요청 | 정상 처리 (멱등) |
| EVERY 모드인데 발신자 본인 | FCM 발송 안 함 (메시지 저장 시 본인은 lastReadMessageId 갱신되어 제외됨) |

## 비목표 (Non-goals)

- 전역(계정 단위) 알림 설정 — 채팅방별 설정만
- 인앱 알림(in-app notification) 시스템
- EVERY 모드 FCM 중복 방지(dedup) — FcmOutbox 적재 패턴으로 충분
- 개인방/단체방 별도 로직 분리 — 동일하게 적용
- 알림 이력 저장·조회

## 수용 기준 (Acceptance Criteria)

1. **Given** 참여자 A가 채팅방 X에서 EVERY 모드
   **When** 다른 참여자가 메시지를 보냄
   **Then** A가 WebSocket 비연결 상태이면 FcmOutbox에 A의 토큰으로 즉시 적재됨

2. **Given** 참여자 A가 채팅방 X에서 EVERY 모드 + WebSocket 연결 중
   **When** 다른 참여자가 메시지를 보냄
   **Then** A에게 FcmOutbox 미적재 (실시간 수신 중)

3. **Given** 참여자 A가 채팅방 X에서 BUNDLED 모드, 1시간 내 안읽은 메시지 n개 있음
   **When** hourly scheduler 실행
   **Then** A의 토큰으로 "새 메시지 n개" FcmOutbox 적재됨

4. **Given** 참여자 A가 채팅방 X에서 BUNDLED 모드, 1시간 내 새 메시지 없음
   **When** hourly scheduler 실행
   **Then** A에게 FcmOutbox 미적재

5. **Given** 참여자 A가 채팅방 X에서 OFF 모드
   **When** 메시지 발송 또는 scheduler 실행
   **Then** A에게 FcmOutbox 미적재

6. **Given** 채팅방에 새로 입장
   **When** 입장 즉시
   **Then** 알림 모드 기본값은 EVERY

7. **Given** 참여하지 않은 채팅방 roomId로 알림 모드 변경 요청
   **When** PATCH /open-chat-rooms/{roomId}/participants/me/notification
   **Then** 오류 반환

8. **Given** SYSTEM 메시지 발송
   **When** EVERY 모드 참여자 존재
   **Then** SYSTEM 메시지는 즉시 FCM 대상 아님

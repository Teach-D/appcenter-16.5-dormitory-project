## 기능 요약

사용자가 공지사항 상세 화면 또는 채팅방 상세 화면에 진입했을 때 클라이언트(네이티브)가 호출하는 읽음 처리 API. `type`에 따라 공지 알림 읽음 상태(`UserNotification.isRead`) 또는 채팅방 미읽음 카운트(`OpenChatParticipant.lastReadMessageId`)를 업데이트한다.

---

## 동작 명세

### 엔드포인트

```
PATCH /notifications/read
Content-Type: application/json
Authorization: Bearer {JWT}
```

### 요청

```json
// NOTICE 타입
{ "type": "NOTICE", "targetId": "5678" }

// CHAT 타입
{ "type": "CHAT", "targetId": "1234" }
```

- `type`: `"NOTICE"` 또는 `"CHAT"` (FcmRoutingType 값과 동일)
- `targetId`: 공지사항 PK 또는 채팅방 PK (String → 내부에서 Long으로 파싱)

### 응답

```json
// 200 OK
{ "success": true, "message": "성공적으로 읽음 처리되었습니다." }
```

### 처리 흐름

**NOTICE 타입**

1. JWT에서 인증된 userId 추출
2. `UserNotification` 중 `user.id = userId` AND `notification.boardId = targetId(Long)` AND `notification.apiType = ANNOUNCEMENT`인 레코드 조회
3. 해당 레코드의 `isRead = true`로 변경 (레코드가 없으면 아무것도 하지 않고 성공 반환)
4. 200 OK 반환

**CHAT 타입**

1. JWT에서 인증된 userId 추출
2. `OpenChatParticipant` 조회 (`roomId = targetId`, `userId = userId`) — 미참여면 예외
3. 해당 방의 최신 메시지 ID 조회 (`findLatestMessageIdByRoomId`)
4. 최신 메시지가 있으면 `OpenChatParticipant.lastReadMessageId`를 해당 ID로 업데이트
5. 방에 메시지가 없으면 업데이트 없이 성공 반환
6. 200 OK 반환

---

## 도메인 데이터

### NOTICE 처리 대상

| 엔티티 | 필드 | 비고 |
|---|---|---|
| `Notification` | `boardId` | 공지사항 PK (announcementId) |
| `Notification` | `apiType` | `ANNOUNCEMENT` 고정 |
| `UserNotification` | `isRead` | `false` → `true` |

### CHAT 처리 대상

| 엔티티 | 필드 | 비고 |
|---|---|---|
| `OpenChatParticipant` | `lastReadMessageId` | null 또는 이전 값 → 최신 메시지 ID |

---

## 비즈니스 규칙 / 제약

- 인증 필수: JWT에서 userId를 추출한다. 비인증 요청은 Spring Security에서 차단.
- `type`은 대소문자 구분 없이 `NOTICE` / `CHAT`만 허용. 그 외 값은 400 반환.
- `targetId`는 양수 정수로 파싱 가능해야 한다. 파싱 실패 시 400 반환.
- NOTICE: 대상 `UserNotification`이 없어도 에러 없이 성공 처리 (멱등성).
- CHAT: 해당 방의 참여자가 아닌 경우 예외 (`OPEN_CHAT_NOT_PARTICIPANT`).
- 배지 카운트 재계산은 이번 범위에서 구현하지 않는다 (Non-goal).

---

## 예외 · 경계 상황

| 상황 | 기대 동작 |
|---|---|
| 잘못된 `type` 값 (예: `"ROOMMATE"`) | 400 BAD_REQUEST |
| `targetId`가 숫자가 아닌 문자열 | 400 BAD_REQUEST |
| NOTICE: 대상 UserNotification 없음 | 200 OK (아무것도 하지 않음) |
| CHAT: 해당 방 참여자 아님 | 404 (OPEN_CHAT_NOT_PARTICIPANT) |
| CHAT: 방에 메시지가 없음 | 200 OK (lastReadMessageId 변경 없음) |

---

## 비목표 (Non-goals)

- 배지 카운트(Badge Count) 재계산 및 FCM 발송
- CHAT 타입 요청 시 `UserNotification` 레코드 읽음 처리 (lastReadMessageId 업데이트만)
- `FcmOutbox` 상태 변경
- 공지사항 상세 조회 API (별도 도메인)
- 채팅방 메시지 조회 API (기존 WebSocket/REST 흐름 유지)

---

## 수용 기준 (Acceptance Criteria)

### NOTICE 타입

- **AC-1** Given 인증된 사용자, When `type=NOTICE, targetId=5` 요청, Then 해당 사용자의 `UserNotification` 중 `boardId=5 AND apiType=ANNOUNCEMENT`인 레코드의 `isRead`가 `true`로 변경된다.
- **AC-2** Given 인증된 사용자, When `type=NOTICE, targetId=999` 요청(해당 알림 없음), Then 예외 없이 200 OK 반환.
- **AC-3** Given `type=NOTICE`이지만 `targetId`가 숫자가 아닌 경우, Then 400 반환.

### CHAT 타입

- **AC-4** Given 인증된 사용자이고 채팅방 참여자, When `type=CHAT, targetId=1` 요청, Then `OpenChatParticipant.lastReadMessageId`가 해당 방의 최신 메시지 ID로 업데이트된다.
- **AC-5** Given 인증된 사용자이고 채팅방 참여자, 방에 메시지가 없을 때, When `type=CHAT, targetId=1` 요청, Then 200 OK 반환 (lastReadMessageId 변경 없음).
- **AC-6** Given 인증된 사용자이지만 해당 채팅방 참여자가 아님, When `type=CHAT, targetId=1` 요청, Then 404 (OPEN_CHAT_NOT_PARTICIPANT) 반환.

### 공통

- **AC-7** Given 잘못된 `type` 값, When 요청, Then 400 BAD_REQUEST 반환.
- **AC-8** Given 비인증 요청, When 요청, Then 401 반환.

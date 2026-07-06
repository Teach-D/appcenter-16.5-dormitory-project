# BR-674 — 단체 채팅방 생성 시 원본 톡방 링크 메시지 API 명세서

> Base URL: `/open-chat-rooms`

---

## 파생 채팅방 생성

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/open-chat-rooms/derived` |
| **인증** | Bearer Token (JWT) 필요 |
| **설명** | 파생 채팅방(DERIVED)을 생성하고, `originRoomId`로 지정한 원본 톡방에 `ROOM_LINK` 타입 메시지를 WebSocket으로 브로드캐스트한다. |

### Request

#### Request Body
Content-Type: `application/json`

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `originRoomId` | `Long` | ✅ | `@NotNull` | 링크 메시지를 전송할 원본 톡방 ID |
| `name` | `String` | ✅ | `@NotBlank`, 1–30자 | 파생 방 이름 |
| `description` | `String` | ❌ | 최대 100자 | 파생 방 설명 |
| `maxParticipants` | `Integer` | ✅ | `@NotNull`, 2–100 | 최대 참여 인원 |
| `isPublic` | `Boolean` | ✅ | `@NotNull` | 공개 여부 |
| `password` | `String` | ❌ | 최대 50자 | 비공개 방 비밀번호 |

```json
{
  "originRoomId": 1,
  "name": "토론방",
  "description": "자유롭게 토론해요",
  "maxParticipants": 30,
  "isPublic": true,
  "password": null
}
```

### Response

#### 성공 응답 — `201 Created`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 생성된 파생 채팅방 ID |

```json
{
  "roomId": 42
}
```

#### 에러 응답

| 상태 코드 | 에러 코드 | 발생 조건 |
|-----------|-----------|-----------|
| `400 Bad Request` | — | `originRoomId` · `name` · `maxParticipants` · `isPublic` 누락, 또는 길이/범위 위반 |
| `404 Not Found` | `OPEN_CHAT_ROOM_NOT_FOUND` (22001) | `originRoomId`에 해당하는 채팅방 없음 |
| `404 Not Found` | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` (22004) | 요청자가 `originRoom` 참여자가 아님 |
| `401 Unauthorized` | — | JWT 토큰 없음 또는 만료 |

에러 응답 형식:
```json
{
  "code": 22001,
  "name": "OPEN_CHAT_ROOM_NOT_FOUND",
  "message": "[OpenChat] 채팅방을 찾을 수 없습니다.",
  "errors": null
}
```

---

## WebSocket 브로드캐스트 — ROOM_LINK 메시지

> 파생 방 생성 성공 시, 아래 메시지가 `/sub/openchat/{originRoomId}` 토픽으로 자동 전송된다.
> 클라이언트는 별도 API 호출 없이 WebSocket 구독으로 수신한다.

### 수신 토픽

```
/sub/openchat/{originRoomId}
```

### 메시지 페이로드

| 필드 | 타입 | 설명 |
|------|------|------|
| `messageId` | `Long` | 저장된 메시지 ID |
| `roomId` | `Long` | originRoomId (메시지가 속한 방) |
| `senderId` | `Long` | 파생 방을 생성한 유저 ID |
| `senderNickname` | `String` | 생성자 닉네임 |
| `content` | `String` | JSON 문자열 (하단 참고) |
| `type` | `String` | `"ROOM_LINK"` |
| `imageUrls` | `Array` | `[]` (항상 빈 배열) |
| `unreadCount` | `Integer` | 현재 미읽음 수 |
| `createdAt` | `String (ISO 8601)` | 메시지 생성 시각 |
| `linkedRoomId` | `Long` | 생성된 파생 방 ID |
| `linkedRoomName` | `String` | 파생 방 이름 |
| `linkedRoomDescription` | `String \| null` | 파생 방 설명 (없으면 null) |
| `linkedRoomMaxParticipants` | `Integer` | 파생 방 최대 인원 |

```json
{
  "messageId": 105,
  "roomId": 1,
  "senderId": 7,
  "senderNickname": "김철수",
  "content": "{\"derivedRoomId\":42,\"roomName\":\"토론방\",\"description\":\"자유롭게 토론해요\",\"maxParticipants\":30}",
  "type": "ROOM_LINK",
  "imageUrls": [],
  "unreadCount": 3,
  "createdAt": "2026-07-06T10:30:00",
  "linkedRoomId": 42,
  "linkedRoomName": "토론방",
  "linkedRoomDescription": "자유롭게 토론해요",
  "linkedRoomMaxParticipants": 30
}
```

> `linkedRoom*` 필드는 `type == "ROOM_LINK"`일 때만 non-null. TEXT / IMAGE / SYSTEM 메시지에서는 null.

---

## 채팅 메시지 목록 조회 (영향 범위)

> `GET /open-chat-rooms/{roomId}/messages` — 기존 API. ROOM_LINK 타입 메시지 조회 시 `linkedRoom*` 필드가 추가됨.

기존 응답 구조 유지, `ResponseOpenChatMessageDto`에 아래 필드만 추가:

| 필드 | 타입 | 설명 |
|------|------|------|
| `linkedRoomId` | `Long \| null` | ROOM_LINK 타입에만 값 |
| `linkedRoomName` | `String \| null` | ROOM_LINK 타입에만 값 |
| `linkedRoomDescription` | `String \| null` | ROOM_LINK 타입에만 값 |
| `linkedRoomMaxParticipants` | `Integer \| null` | ROOM_LINK 타입에만 값 |

---

## 추론 항목

> 코드에서 명시적으로 확인되지 않아 추론한 항목입니다.

- `originRoomId` 참여자 검증 오류의 HTTP 상태: 설계 문서에서는 403으로 기술했으나 기존 `OPEN_CHAT_PARTICIPANT_NOT_FOUND` ErrorCode가 `NOT_FOUND(404)`로 정의되어 있어 **404**로 확정. 구현 시 재확인 필요.
- `400 Bad Request` 에러 응답 형식: Spring `@Valid` 검증 실패 시 GlobalExceptionHandler 처리 결과로 추론.
- `createdAt` 시간대: 서버 로컬 시간(`LocalDateTime`) 반환으로 추론.

# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`
> 기능: 오픈채팅 읽음 처리 (Read Receipt)

---

## 1. 공통 정보

### WebSocket 연결

| 항목 | 값 |
|------|-----|
| STOMP 엔드포인트 | `ws://{host}/ws-stomp` (순수 WebSocket) |
| SockJS 엔드포인트 | `ws://{host}/ws-stomp-sockjs` |
| 발신 prefix | `/pub` |
| 구독 prefix | `/sub` |
| 인증 방식 | WebSocket 연결 시 `Authorization: Bearer {accessToken}` 헤더 전달 |

### Base URL (HTTP REST)
`/open-chat-rooms`

### 인증 방식
- HTTP: `Authorization: Bearer {accessToken}`
- WebSocket: 연결(CONNECT) 시 헤더에 토큰 전달 → 서버가 세션 속성에 `userId` 저장

### 신규 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| (없음) | — | 이번 기능은 DB 스키마 변경 없음. 신규 에러 코드 불필요 |

> 기존 에러 코드 유지: `OPEN_CHAT_ROOM_NOT_FOUND`, `OPEN_CHAT_NOT_PARTICIPANT`

---

## 2. API 목록

---

### [NEW] READ 이벤트 구독

**`SUBSCRIBE /sub/openchat/{roomId}/read`**

| 항목 | 내용 |
|------|------|
| 설명 | 특정 채팅방의 읽음 상태 변화를 실시간으로 수신한다 |
| 인증 | WebSocket 연결 인증 필수 |
| 권한 | 해당 방 참여자 |
| 방향 | 서버 → 클라이언트 (단방향 수신) |

**수신 Payload:**

```json
{
  "messageId": "Long | 읽음 상태가 변경된 메시지 ID",
  "unreadCount": "int | 해당 메시지의 현재 미읽음 참여자 수"
}
```

**발행 트리거:**

| 트리거 | 설명 |
|--------|------|
| WebSocket 메시지 전송 (`/pub/openchat/socketchat`) | 새 메시지 저장 + 구독자 읽음 처리 완료 후 |
| `GET /open-chat-rooms/{roomId}/messages` 호출 | 호출한 사용자의 읽음 처리 완료 후 |
| `POST /open-chat-rooms/{roomId}/messages/image` 호출 | 이미지 메시지 저장 + 구독자 읽음 처리 완료 후 |
| 클라이언트가 `/sub/openchat/{roomId}` 구독 (WebSocket subscribe) | 최신 메시지 읽음 처리 완료 후 |

**클라이언트 처리 방식 (batch read 의미론):**

> `getMessages()` 호출 시 READ 이벤트는 **조회된 메시지 중 가장 최신 `messageId` 단건**으로 발행된다.
> 클라이언트는 이 이벤트를 수신하면 **`id ≤ messageId`인 모든 메시지의 `unreadCount`를 1 감소**시켜야 한다.
> (단, `unreadCount`가 이미 0인 메시지는 변경하지 않음)

**예시 시나리오:**

```
A, B 구독 중 / C 오프라인

A가 메시지 전송 (id=50):
  → 서버: A, B의 lastReadMessageId = 50 업데이트
  → /sub/openchat/{roomId}/read: {messageId: 50, unreadCount: 1}  (C 미읽음)

C가 getMessages() 호출:
  → 서버: C의 lastReadMessageId = 50 업데이트
  → /sub/openchat/{roomId}/read: {messageId: 50, unreadCount: 0}  (전원 읽음)
  → A, B: id ≤ 50인 모든 메시지 unreadCount 1 감소 처리
```

---

### [CHANGED] WebSocket 메시지 전송

**`SEND /pub/openchat/socketchat`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방에 텍스트 메시지를 전송한다. 현재 구독 중인 사용자는 자동으로 읽음 처리된다 |
| 인증 | WebSocket 연결 인증 필수 |
| 권한 | 해당 방 참여자 |

**Request Payload:**

```json
{
  "roomId": "Long | 필수 | 채팅방 ID",
  "content": "String | 필수 | 메시지 내용"
}
```

**브로드캐스트 — `/sub/openchat/{roomId}`:**

```json
{
  "messageId": "Long | 저장된 메시지 ID",
  "roomId": "Long | 채팅방 ID",
  "senderId": "Long | 발신자 userId",
  "senderNickname": "String | 발신자 닉네임",
  "content": "String | 메시지 내용",
  "type": "TEXT",
  "imageUrls": [],
  "unreadCount": "int | 현재 미읽음 참여자 수 (구독자는 자동 읽음 처리된 후 계산)",
  "createdAt": "String | ISO 8601"
}
```

**브로드캐스트 — `/sub/openchat/{roomId}/read`:**

```json
{
  "messageId": "Long | 방금 저장된 메시지 ID",
  "unreadCount": "int | 구독자 자동 읽음 처리 후 계산된 미읽음 수"
}
```

**비즈니스 로직 요약 (변경된 부분):**
1. 메시지 DB 저장
2. `OpenChatSessionRegistry.getSubscriberUserIds(roomId)`로 현재 구독자 userId Set 조회 (BR-01)
3. 구독자 전원(발신자 포함)의 `lastReadMessageId`를 해당 메시지 ID로 **JPQL bulk update** (ADR-02)
4. `calculateUnreadCount(roomId, messageId)` 호출 — 구독자는 이미 읽음 처리돼 제외됨 (BR-03)
5. `/sub/openchat/{roomId}`로 메시지 브로드캐스트
6. `/sub/openchat/{roomId}/read`로 READ 이벤트 전파 (BR-02)

**에러 케이스:**

> WebSocket 특성상 예외를 클라이언트에게 직접 반환하지 않고 메시지 전송을 무시한다.

| 상황 | 처리 |
|------|------|
| roomId에 해당하는 방 없음 | 무시 (early return) |
| 요청자가 해당 방 참여자 아님 | 무시 (early return) |
| 세션에 userId 없음 | 무시 (early return) |

---

### [CHANGED] 메시지 목록 조회 (오프라인 읽음 처리)

**`GET /open-chat-rooms/{roomId}/messages`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방 메시지를 커서 기반으로 조회한다. 조회 시 최신 메시지 ID로 읽음 처리되고 READ 이벤트가 전파된다 |
| 인증 | 필요 |
| 권한 | `USER`, `ADMIN`, `DORMITORY` (해당 방 참여자) |
| 멱등성 | 비멱등 (읽음 상태 변경 발생) |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 채팅방 ID |

**Query Parameters:**

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `lastMessageId` | Long | N | null | 커서 ID. null이면 최신부터 조회 |
| `size` | int | N | 30 | 조회 메시지 수 |

**Response (성공): `200 OK`**

```json
{
  "messages": [
    {
      "messageId": "Long",
      "roomId": "Long",
      "senderId": "Long",
      "senderNickname": "String | SYSTEM 메시지는 null",
      "content": "String",
      "type": "TEXT | IMAGE | SYSTEM",
      "imageUrls": ["String | 이미지 URL 목록 (IMAGE 타입만)"],
      "unreadCount": "int | 조회 시점 미읽음 수",
      "createdAt": "String | ISO 8601"
    }
  ],
  "hasNext": "boolean",
  "nextCursor": "Long | null이면 더 이상 없음"
}
```

**비즈니스 로직 요약 (변경된 부분):**
1. 참여자 검증 후 커서 기반 메시지 조회 (기존 유지)
2. 조회된 메시지 중 `latestId = max(messageId)` 계산
3. `latestId`가 존재하면 요청자의 `lastReadMessageId` 업데이트 (기존 유지)
4. **[신규]** `/sub/openchat/{roomId}/read`로 READ 이벤트 전파 `{messageId: latestId, unreadCount}` (BR-05)
5. 각 메시지의 `unreadCount`는 업데이트 후 계산된 값을 반환

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| roomId에 해당하는 방 없음 | 404 | `OPEN_CHAT_ROOM_NOT_FOUND` | 채팅방을 찾을 수 없습니다 |
| 요청자가 해당 방 참여자 아님 | 403 | `OPEN_CHAT_NOT_PARTICIPANT` | 참여하지 않은 채팅방입니다 |

**사이드 이펙트:**
- `OpenChatReadUpdated` 이벤트: `/sub/openchat/{roomId}/read`로 latestMessageId와 새 unreadCount 전파
- 방에 현재 구독자가 없으면 READ 이벤트를 발행해도 수신자 없음 (정상 동작)

**엣지 케이스:**
- [ ] 조회된 메시지가 0건이면 읽음 처리 및 READ 이벤트 전파 생략

---

### [CHANGED] 이미지 메시지 전송

**`POST /open-chat-rooms/{roomId}/messages/image`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방에 이미지 메시지를 전송한다. 텍스트 메시지와 동일하게 구독자 자동 읽음 처리 및 READ 이벤트 전파 |
| 인증 | 필요 |
| 권한 | `USER`, `ADMIN`, `DORMITORY` (해당 방 참여자) |
| Content-Type | `multipart/form-data` |
| 멱등성 | 비멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 채팅방 ID |

**Request Body (multipart):**

| 파트 이름 | 타입 | 필수 | 설명 |
|-----------|------|------|------|
| `images` | MultipartFile[] | Y | 이미지 파일 목록 (최대 5장, 각 10MB 이하) |

**Validation Rules:**
- 이미지 수: 1~5장
- 허용 확장자: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp`
- 허용 MIME: `image/jpeg`, `image/png`, `image/gif`, `image/webp`
- 파일 크기: 장당 10MB 이하

**Response (성공): `201 Created`**

```json
{
  "messageId": "Long",
  "roomId": "Long",
  "senderId": "Long",
  "senderNickname": "String",
  "content": "",
  "type": "IMAGE",
  "imageUrls": ["String | 업로드된 이미지 URL 목록"],
  "unreadCount": "int | 구독자 읽음 처리 후 계산된 미읽음 수",
  "createdAt": "String | ISO 8601"
}
```

**비즈니스 로직 요약 (변경된 부분):**
1. 이미지 유효성 검증 + 저장 (기존 유지)
2. `OpenChatSessionRegistry.getSubscriberUserIds(roomId)`로 구독자 조회 (BR-01)
3. 구독자 전원 `lastReadMessageId` bulk update
4. `unreadCount` 계산 후 `/sub/openchat/{roomId}` 브로드캐스트
5. **[신규]** `/sub/openchat/{roomId}/read`로 READ 이벤트 전파 (BR-02)

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| roomId에 해당하는 방 없음 | 404 | `OPEN_CHAT_ROOM_NOT_FOUND` | 채팅방을 찾을 수 없습니다 |
| 요청자가 참여자 아님 | 403 | `OPEN_CHAT_NOT_PARTICIPANT` | 참여하지 않은 채팅방입니다 |
| 이미지 없음 | 400 | `OPEN_CHAT_IMAGE_EMPTY` | 이미지를 첨부해주세요 |
| 이미지 5장 초과 | 400 | `OPEN_CHAT_IMAGE_COUNT_EXCEEDED` | 이미지는 최대 5장까지 전송 가능합니다 |
| 허용되지 않는 파일 형식 | 400 | `IMAGE_INVALID_FORMAT` | 지원하지 않는 이미지 형식입니다 |

---

### [CONFIRMED] 채팅방 목록 조회 — unreadCount 포함

**`GET /open-chat-rooms`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방 목록을 조회한다. 참여 중인 방은 `unreadCount` 뱃지를 포함한다 |
| 인증 | 필요 |
| 권한 | `USER`, `ADMIN`, `DORMITORY` |
| 멱등성 | 멱등 |

**Query Parameters:**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `tab` | `OpenChatRoomTab` | Y | 탭 구분 (ALL, MY, DORMITORY 등) |
| `page`, `size`, `sort` | — | N | 페이지네이션 |

**Response (성공): `200 OK`**

```json
{
  "content": [
    {
      "roomId": "Long",
      "name": "String",
      "description": "String",
      "scope": "OpenChatRoomScope",
      "currentParticipants": "int",
      "maxParticipants": "int",
      "isJoined": "boolean",
      "lastMessageAt": "String | ISO 8601",
      "lastMessage": "String",
      "unreadCount": "int | 미참여 방은 0. 참여 중인 방은 lastReadMessageId 이후 메시지 수 (BR-08)"
    }
  ],
  "totalElements": "Long",
  "totalPages": "int",
  "currentPage": "int",
  "hasNext": "boolean"
}
```

**비즈니스 로직 요약:**
1. 참여 중인 방(`isJoined = true`): `countByRoomIdAndIdGreaterThan(roomId, lastReadMessageId)`로 `unreadCount` 계산 (BR-08)
2. 미참여 방(`isJoined = false`): `unreadCount = 0`

> 이 API는 기존에 `unreadCount` 필드가 있었으나 미참여 방에서 항상 0이었음. 읽음 처리 기능 도입으로 참여 중인 방에서 정확한 값을 반환함.

---

### [IMPLICIT] WebSocket 구독 시 자동 읽음 처리

**`SUBSCRIBE /sub/openchat/{roomId}`**

> 이 구독 행위는 별도 API 호출 없이 서버에서 `SessionSubscribeEvent`로 감지하여 자동 처리된다.

| 처리 내용 | 설명 |
|-----------|------|
| `OpenChatSessionRegistry` 등록 | `subscribe(sessionId, roomId, userId)` 호출 |
| `lastReadMessageId` 업데이트 | 해당 방의 최신 메시지 ID로 갱신 (BR-06) |
| READ 이벤트 전파 | `/sub/openchat/{roomId}/read`로 `{messageId: latestId, unreadCount}` 발행 |

**`DISCONNECT` 시 처리:**

| 처리 내용 | 설명 |
|-----------|------|
| `OpenChatSessionRegistry` 제거 | `unsubscribe(sessionId)` 호출 |

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API / 트리거 | 발행 토픽 | 페이로드 | 조건 |
|-------------|-----------|---------|------|
| WebSocket 메시지 전송 | `/sub/openchat/{roomId}/read` | `{messageId, unreadCount}` | 항상 |
| WebSocket 이미지 전송 | `/sub/openchat/{roomId}/read` | `{messageId, unreadCount}` | 항상 |
| `GET /messages` 호출 | `/sub/openchat/{roomId}/read` | `{messageId: latestId, unreadCount}` | 메시지가 1건 이상일 때 |
| `/sub/openchat/{roomId}` 구독 | `/sub/openchat/{roomId}/read` | `{messageId: latestId, unreadCount}` | 방에 메시지가 1건 이상일 때 |

---

## 4. API 간 의존 관계

- `SUBSCRIBE /sub/openchat/{roomId}/read` 구독은 `/sub/openchat/{roomId}` 구독과 동시에 해야 한다 — READ 이벤트만 구독해서는 메시지를 받을 수 없음
- `GET /messages` 호출은 `POST /{roomId}/participants/me` (채팅방 입장) 이후에만 가능

---

## 5. 보안 체크리스트

- [x] 모든 HTTP 쓰기 API에 JWT 인증 적용
- [x] WebSocket 연결 시 `WebSocketAuthInterceptor`로 토큰 검증
- [x] `getMessages()`: 참여자 여부 검증으로 타인 방 메시지 조회 차단
- [x] READ 이벤트는 서버가 발행하는 단방향 이벤트 — 클라이언트가 위조 불가
- [x] `OpenChatSessionRegistry`는 서버 내부 상태 — 외부 노출 없음
- [ ] READ 이벤트 전파 시 민감 정보(userId) 포함 여부 확인 — 현재 `{messageId, unreadCount}`만 전파, userId 미포함 (안전)

---

## 6. 최종 검토

- [x] `/sub/openchat/{roomId}/read` 신규 토픽 명세 완료
- [x] batch read의 "up-to 의미론" 클라이언트 처리 방식 명시
- [x] 구독자 bulk update가 READ 이벤트 전파 전에 완료됨을 흐름에서 보장
- [x] SYSTEM 메시지도 동일한 읽음 처리 대상임을 서비스 로직에 명시 (BR-04)
- [x] 메시지 없는 방 구독 시 READ 이벤트 생략 엣지케이스 처리 명시
- [ ] 하위 호환성: 기존 클라이언트가 `/sub/openchat/{roomId}/read`를 구독하지 않아도 동작 가능 (추가 구독이므로 breaking change 없음)

---

## 7. TBD

- [ ] `OpenChatSessionRegistry` 인터페이스 분리 여부 — 멀티 서버 전환 시 Redis pub/sub으로 교체 가능하도록 설계할지 (현재 단일 서버 in-memory)
- [ ] 이미지 메시지 HTTP 응답에서도 `/sub/openchat/{roomId}/read` READ 이벤트가 이미 전파되므로, 클라이언트가 HTTP 응답과 WebSocket 이벤트를 중복 수신하는 경우 처리 방식 명시 필요
- [ ] `lastReadMessageId`가 NULL인 참여자(한 번도 읽지 않은 사용자)에 대한 `unreadCount` 계산 정확성 검증 — 현재 `isNotNull()` 조건 적용 중

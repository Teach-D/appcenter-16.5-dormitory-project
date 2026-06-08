# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`

---

## 1. 공통 정보

### Base URL
`/open-chat-rooms`

### 인증 방식
- 헤더: `Authorization: Bearer {accessToken}`
- 미인증 시: `401 UNAUTHORIZED`

### 공통 응답 형식

**성공:**
```json
{
  "success": true,
  "data": {},
  "message": null
}
```

**실패:**
```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

### 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| UNAUTHORIZED | 401 | 인증 토큰 없음 또는 만료 |
| FORBIDDEN | 403 | 권한 없음 (공개 범위 불일치, 비방장 삭제 시도 등) |
| NOT_FOUND | 404 | 채팅방 없음 |
| VALIDATION_ERROR | 400 | 요청값 검증 실패 |
| OPEN_CHAT_ROOM_FULL | 400 | 최대 인원 초과 |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |

### 페이지네이션

**Query Parameters:**

| 이름 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| page | number | 0 | 페이지 번호 (0-based) |
| size | number | 20 | 페이지 크기 (max: 100) |

**Response 공통 필드:**
```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "currentPage": 0,
  "hasNext": false
}
```

---

## 2. API 목록

---

### 2-1. 채팅방 생성

**`POST /open-chat-rooms`**

| 항목 | 내용 |
|------|------|
| 설명 | 새 오픈 채팅방을 생성하고 생성자를 방장으로 등록한다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 비멱등 |

**Request Body:**
```json
{
  "name": "string | 방 이름 | 필수",
  "description": "string | 방 설명 | 선택",
  "scope": "DORMITORY | ALL | 공개 범위 | 필수",
  "maxParticipants": "number | 최대 인원 | 필수"
}
```

**Validation Rules:**
- `name`: 1~30자, 공백만으로 구성 불가, null 불가
- `description`: 최대 100자, null 허용
- `scope`: `DORMITORY` 또는 `ALL` 중 하나, null 불가
- `maxParticipants`: 2 이상 100 이하, null 불가

**Response (성공 — 201 CREATED):**
```json
{
  "success": true,
  "data": {
    "roomId": "number | 생성된 채팅방 ID"
  }
}
```

**비즈니스 로직 요약:**
1. 요청자 Role이 USER인지 검증 (BR-01)
2. `scope = DORMITORY`이면 요청자의 `dormType`을 `creatorDormitory`로 저장, `scope = ALL`이면 `creatorDormitory = NULL`
3. `OpenChatRoom` 생성 + `hostUserId = 요청자 ID` 설정
4. 요청자를 첫 번째 `OpenChatParticipant`로 등록 (`joinedAt = now`)
5. 생성된 `roomId` 반환

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| name이 없거나 30자 초과 | 400 | VALIDATION_ERROR | 방 이름은 1~30자여야 합니다 |
| scope가 유효하지 않은 값 | 400 | VALIDATION_ERROR | 유효하지 않은 공개 범위입니다 |
| maxParticipants가 2 미만 또는 100 초과 | 400 | VALIDATION_ERROR | 최대 인원은 2~100명이어야 합니다 |

**사이드 이펙트 / 도메인 이벤트:**
- `RoomCreated`: 방 생성 완료 시 발행 (Phase 2 — FCM 연동 시 사용)

---

### 2-2. 채팅방 목록 조회 (3탭)

**`GET /open-chat-rooms`**

| 항목 | 내용 |
|------|------|
| 설명 | 탭 파라미터에 따라 내 방 / 내 기숙사 / 전체 목록을 조회한다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 멱등 |

**Query Parameters:**

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| tab | string | Y | - | `MY` / `DORMITORY` / `ALL` |
| page | number | N | 0 | 페이지 번호 |
| size | number | N | 20 | 페이지 크기 |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "roomId": "number",
        "name": "string | 방 이름",
        "description": "string | null",
        "scope": "DORMITORY | ALL",
        "currentParticipants": "number | 현재 참여 인원",
        "maxParticipants": "number | 최대 인원",
        "isJoined": "boolean | 요청자가 이미 참여 중인지 여부",
        "lastMessageAt": "string | ISO 8601 | null (메시지 없는 경우)",
        "lastMessagePreview": "string | 최근 메시지 미리보기 (최대 30자) | null (Phase 1에서 항상 null)"
      }
    ],
    "totalElements": "number",
    "totalPages": "number",
    "currentPage": "number",
    "hasNext": "boolean"
  }
}
```

**비즈니스 로직 요약:**

- `tab=MY`: 요청자가 `OpenChatParticipant`로 등록된 방만 조회
- `tab=DORMITORY`:
  1. 요청자 `dormType`이 `NONE`이면 빈 목록 반환 (BR-02)
  2. `scope = DORMITORY` AND `creatorDormitory = 요청자 dormType`인 방 조회
- `tab=ALL`: `scope = ALL`인 방 전체 조회
- 모든 탭에서 `isJoined` 필드: 요청자가 해당 방의 참여자인지 여부
- `lastMessagePreview`: Phase 1에서 항상 `null` 반환 (Phase 2에서 `OpenChatMessage` 연동)

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| tab이 누락되거나 유효하지 않은 값 | 400 | VALIDATION_ERROR | tab은 MY, DORMITORY, ALL 중 하나여야 합니다 |

**엣지 케이스:**
- [ ] `tab=DORMITORY`, 요청자 `dormType=NONE` → 빈 목록(`content: []`) 반환, 에러 아님
- [ ] `tab=MY`이고 참여한 방이 없으면 빈 목록 반환

---

### 2-3. 채팅방 입장

**`POST /open-chat-rooms/{roomId}/participants/me`**

| 항목 | 내용 |
|------|------|
| 설명 | 지정한 채팅방에 입장한다. 이미 참여 중이면 멱등 처리한다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 멱등 (이미 참여 중인 경우) |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 입장할 채팅방 ID |

**Request Body:** 없음

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "roomId": "number",
    "name": "string",
    "description": "string | null",
    "scope": "DORMITORY | ALL",
    "currentParticipants": "number",
    "maxParticipants": "number",
    "isOfficial": "boolean",
    "createdAt": "string | ISO 8601"
  }
}
```

**비즈니스 로직 요약:**
1. 채팅방 존재 여부 확인 — 없으면 404
2. 이미 참여 중(`OpenChatParticipant` 존재)이면 방 정보 그대로 반환 (멱등, BR-05)
3. `scope = DORMITORY`이면 요청자 `dormType == creatorDormitory` 검증 (BR-03) — 불일치 시 403
4. `OpenChatRoom`에 비관적 락 획득 (ADR-03)
5. `currentParticipants >= maxParticipants`이면 400 반환 (BR-04)
6. `OpenChatParticipant` 신규 등록 (`joinedAt = now`, `notificationEnabled = true` 기본값)
7. 방 상세 정보 반환

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 존재하지 않는 roomId | 404 | NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| scope=DORMITORY + dormType 불일치 | 403 | FORBIDDEN | 해당 기숙사 전용 채팅방입니다 |
| 최대 인원 초과 | 400 | OPEN_CHAT_ROOM_FULL | 최대 인원에 도달한 채팅방입니다 |

**동시성 & 멱등성:**
- 비관적 락 (`findByIdWithLock`) 으로 동시 입장 시 인원 초과 완전 차단
- 이미 참여 중인 경우 추가 DB 쓰기 없이 정상 응답

**사이드 이펙트 / 도메인 이벤트:**
- `ParticipantJoined`: 신규 입장 완료 시 발행 (Phase 2 — 방장에게 FCM 알림)

**엣지 케이스:**
- [ ] 이미 참여 중인 방에 재입장 → 200 OK + 방 정보 반환 (멱등)
- [ ] `scope=DORMITORY` + 요청자 `dormType=NONE` → 403 FORBIDDEN

---

### 2-4. 채팅방 나가기

**`DELETE /open-chat-rooms/{roomId}/participants/me`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방에서 나간다. 방장이 나가면 방장 이전 또는 방 삭제가 수행된다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 비멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 나갈 채팅방 ID |

**Request Body:** 없음

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "roomDeleted": "boolean | 방이 삭제되었으면 true"
  }
}
```

**비즈니스 로직 요약:**
1. 채팅방 존재 여부 확인 — 없으면 404
2. 요청자가 해당 방의 참여자인지 확인 — 아니면 404
3. `OpenChatParticipant` 삭제 (요청자 행 제거)
4. 요청자가 방장(`room.hostUserId == 요청자 ID`)이면 방장 이전 로직 수행 (BR-06):
   - 남은 참여자 중 `joinedAt ASC` 기준 가장 오래된 참여자에게 `hostUserId` 이전
   - 남은 참여자가 0명이면:
     - `is_official = FALSE` → 방 삭제, `roomDeleted = true` 반환
     - `is_official = TRUE` → 삭제 없이 `roomDeleted = false` 반환 (BR-07)
5. 방장이 아니면 참여자 제거만 수행

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 존재하지 않는 roomId | 404 | NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 참여 중이지 않은 방 나가기 시도 | 404 | NOT_FOUND | 참여하지 않은 채팅방입니다 |

**사이드 이펙트 / 도메인 이벤트:**
- `HostTransferred`: 방장 이전 완료 시 발행 (Phase 2 — 신규 방장에게 FCM 알림)
- `RoomDeleted`: 방 삭제 시 발행 (Phase 2 — 연관 메시지 정리)

**엣지 케이스:**
- [ ] 마지막 참여자(방장)가 나가기 + `is_official=FALSE` → 방 삭제, `roomDeleted=true`
- [ ] 마지막 참여자(방장)가 나가기 + `is_official=TRUE` → 방장 없는 공식 방 유지, `roomDeleted=false`
- [ ] 방장이 아닌 참여자 나가기 → 참여자 제거만 수행, `roomDeleted=false`

---

### 2-5. 채팅방 삭제

**`DELETE /open-chat-rooms/{roomId}`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방을 강제 삭제한다. 방장 또는 ADMIN만 가능. is_official 방은 ADMIN만 삭제 가능 |
| 인증 | 필요 |
| 권한 | USER (방장) / ADMIN |
| 멱등성 | 비멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 삭제할 채팅방 ID |

**Request Body:** 없음

**Response (성공 — 204 NO CONTENT):**
```json
(body 없음)
```

**비즈니스 로직 요약:**
1. 채팅방 존재 여부 확인 — 없으면 404
2. 요청자가 ADMIN이면 삭제 진행 (is_official 방 포함)
3. 요청자가 USER이면:
   - `room.hostUserId == 요청자 ID` 검증 (BR-08) — 불일치 시 403
   - `is_official = TRUE`이면 403 (USER는 공식 방 삭제 불가)
4. 방에 속한 `OpenChatParticipant` 전체 삭제 후 `OpenChatRoom` 삭제

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 존재하지 않는 roomId | 404 | NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 방장이 아닌 USER의 삭제 시도 | 403 | FORBIDDEN | 채팅방 삭제 권한이 없습니다 |
| USER가 is_official 방 삭제 시도 | 403 | FORBIDDEN | 공식 채팅방은 삭제할 수 없습니다 |

**사이드 이펙트 / 도메인 이벤트:**
- `RoomDeleted`: 삭제 완료 시 발행 (Phase 2 — 참여자 FCM 알림, 메시지 정리)

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|------------|-----------|-----------|
| 채팅방 생성 | `RoomCreated` | (Phase 2) FCM | 생성 알림 |
| 채팅방 입장 | `ParticipantJoined` | (Phase 2) FCM | 방장에게 입장 알림 |
| 채팅방 나가기 | `HostTransferred` | (Phase 2) FCM | 신규 방장에게 알림 |
| 채팅방 나가기 | `RoomDeleted` | (Phase 2) 메시지 정리 | 연관 메시지 삭제 |
| 채팅방 삭제 | `RoomDeleted` | (Phase 2) FCM, 메시지 정리 | 참여자 알림 + 메시지 삭제 |

Phase 1에서 모든 이벤트는 발행하지 않음. 인터페이스 정의만 TBD.

---

## 4. API 간 의존 관계

- `채팅방 입장(2-3)` 호출 전 `채팅방 생성(2-1)` 또는 Flyway 초기 데이터로 방이 존재해야 함
- `채팅방 나가기(2-4)`는 `채팅방 입장(2-3)` 이후에만 유의미 (비참여자 호출 시 404)
- `채팅방 삭제(2-5)`는 `채팅방 나가기(2-4)`와 별개 — 삭제는 강제 제거, 나가기는 자발적 탈퇴

---

## 5. 보안 체크리스트

- [x] 모든 쓰기 API에 인증(JWT) 적용
- [x] `scope=DORMITORY` 방 입장 시 dormType 소유권 검증 (BR-03)
- [x] 방 삭제 시 방장 소유권 검증 (BR-08)
- [x] `is_official` 방 USER 삭제 차단
- [ ] 요청 body 크기 제한 (name, description 길이 제한으로 간접 제어)
- [ ] Rate Limit: 방 생성 API (단기간 대량 방 생성 방지) — Phase 2 검토

---

## 6. 최종 검토

- [x] ambiguous endpoint 없음 (탭 구분 명확, 나가기/삭제 분리)
- [x] 숨겨진 동시성 위험 식별 완료 (입장 비관적 락)
- [x] 누락된 인가 검사 없음
- [x] Phase 1에서 `lastMessagePreview` null 반환으로 최종 일관성 처리 명시
- [x] 멱등 입장(BR-05) 명시 완료

---

## 7. TBD (Phase 1)

- [x] 방 목록 정렬 기준: `lastMessageAt DESC` (Phase 2에서 확정)
- [ ] 방 검색 API (이름 키워드): Phase 1 포함 여부
- [ ] `notification_enabled` ON/OFF 변경 API: Phase 1 포함 여부
  - 포함 시: `PATCH /open-chat-rooms/{roomId}/participants/me/notification`
- [ ] `is_official` 방 hostUserId NULL 처리: 첫 입장자 자동 방장 지정 vs NULL 허용 (ADR 결정 필요)

---

---

# Phase 2 — 실시간 채팅 API (WebSocket + STOMP)

> 기반 요구사항: `docs/requirements.md` § Phase 2
> 기반 도메인 모델: `docs/domain-model.md` § Phase 2

---

## 8. WebSocket 연결 정보

### 연결 엔드포인트

| 항목 | 값 |
|------|-----|
| WebSocket URL | `ws://{host}/ws-stomp` |
| SockJS URL (대체) | `http://{host}/ws-stomp-sockjs` |
| 프로토콜 | STOMP 1.2 |
| 인증 방식 | STOMP CONNECT 헤더 `Authorization: Bearer {accessToken}` |

### STOMP 프레임 구조

**CONNECT:**
```
CONNECT
Authorization: Bearer {accessToken}
accept-version:1.2
heart-beat:10000,10000

^@
```

**SUBSCRIBE (채팅방 구독):**
```
SUBSCRIBE
id:sub-0
destination:/sub/openchat/{roomId}

^@
```

**SEND (메시지 발행):**
```
SEND
destination:/pub/openchat/socketchat
content-type:application/json

{"roomId":1,"content":"안녕하세요"}
^@
```

### 인증 실패 시 동작
- STOMP CONNECT 단계에서 JWT 검증 실패 → WebSocket 연결 거부 (기존 `WebSocketAuthInterceptor` 동작)
- 세션에 `userId`가 없는 상태에서 메시지 발행 → 서버에서 처리 중단 (클라이언트는 응답 없음)

---

## 9. Phase 2 API 목록

---

### 9-1. 메시지 발행 (WebSocket STOMP)

**`SEND /pub/openchat/socketchat`** (`@MessageMapping`)

| 항목 | 내용 |
|------|------|
| 설명 | 오픈채팅방에 텍스트 메시지를 발행한다. DB에 저장 후 해당 방 구독자 전체에 브로드캐스트 |
| 인증 | 필요 (STOMP 세션 내 JWT) |
| 권한 | USER (해당 방의 참여자) |
| 멱등성 | 비멱등 |

**STOMP SEND Payload:**
```json
{
  "roomId": "number | 채팅방 ID | 필수",
  "content": "string | 메시지 내용 | 필수"
}
```

**Validation Rules:**
- `roomId`: null 불가
- `content`: 1자 이상, null·공백만으로 구성 불가

**브로드캐스트 목적지:** `/sub/openchat/{roomId}`

**브로드캐스트 페이로드 (ResponseOpenChatMessageDto):**
```json
{
  "messageId": "number | 메시지 ID",
  "roomId": "number | 채팅방 ID",
  "senderId": "number | 발신자 유저 ID (SYSTEM=0)",
  "senderNickname": "string | 발신자 닉네임",
  "content": "string | 메시지 내용",
  "type": "TEXT | SYSTEM",
  "unreadCount": "number | 현재 읽지 않은 참여자 수",
  "createdAt": "string | ISO 8601"
}
```

**비즈니스 로직 요약:**
1. STOMP 세션에서 `userId` 추출 (BR-P2-01)
2. `OpenChatParticipant` 존재 여부로 발행자 참여자 검증 (BR-P2-02, INV-MSG-01) — 비참여자면 처리 중단
3. `OpenChatMessage(type=TEXT)` DB 저장
4. `OpenChatRoom.lastMessage` (최대 500자 truncate), `lastMessageAt` 갱신 (BR-P2-04, INV-MSG-03)
5. 발신자의 `OpenChatParticipant.lastReadMessageId` = 저장된 메시지 ID로 갱신 (BR-P2-06)
6. `unreadCount` = 방 총 참여자 수 − `lastReadMessageId ≥ messageId`인 참여자 수 계산 (ADR-04)
7. `/sub/openchat/{roomId}`로 브로드캐스트 (BR-P2-03)

**에러 케이스:**

| 상황 | 처리 방식 |
|------|-----------|
| 세션에 userId 없음 (미인증) | 처리 중단, 클라이언트 응답 없음 |
| 참여자가 아닌 사용자 발행 | 처리 중단, 클라이언트 응답 없음 |
| 존재하지 않는 roomId | 처리 중단, 클라이언트 응답 없음 |

**사이드 이펙트:**
- `MessageSent`: 브로드캐스트 완료 후 (Phase 3 FCM 연동 시 오프라인 참여자 알림)

**엣지 케이스:**
- [ ] 동시에 여러 메시지 발행 시 ID 순서 보장 → DB auto-increment로 보장
- [ ] 방 삭제 직후 발행 시도 → roomId 존재 검증 실패, 처리 중단

---

### 9-2. WebSocket 구독 이벤트 (서버 자동 처리)

**`SUBSCRIBE /sub/openchat/{roomId}`** (클라이언트 구독 선언, 서버 자동 처리)

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방 채널을 구독한다. 서버는 구독 이벤트를 감지하여 lastReadMessageId를 자동 갱신 |
| 인증 | 필요 (STOMP 세션) |

**서버 자동 처리 (SessionSubscribeEvent):**
1. 구독 destination에서 roomId 파싱
2. 세션에서 userId 추출
3. 해당 참여자의 `lastReadMessageId`를 방의 최신 메시지 ID로 갱신 (BR-P2-05)
   - 메시지가 없으면 갱신 생략

**엣지 케이스:**
- [ ] 방에 아직 메시지가 없으면 갱신 생략 (lastReadMessageId = NULL 유지)
- [ ] prefix `/sub/openchat/` 외 경로는 `OpenChatWebSocketEventListener`에서 무시

---

### 9-3. 채팅 내역 조회

**`GET /open-chat-rooms/{roomId}/messages`**

| 항목 | 내용 |
|------|------|
| 설명 | 커서 기반으로 채팅 내역을 조회한다. 조회 후 해당 참여자의 읽음 상태가 갱신된다 |
| 인증 | 필요 |
| 권한 | USER (해당 방의 참여자) |
| 멱등성 | 비멱등 (읽음 상태 갱신 발생) |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 조회할 채팅방 ID |

**Query Parameters:**

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| lastMessageId | Long | N | null | 커서 ID. 미전달 시 최신 30건 반환 |
| size | number | N | 30 | 페이지 크기 (max: 50) |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "messages": [
      {
        "messageId": "number",
        "roomId": "number",
        "senderId": "number | SYSTEM=0",
        "senderNickname": "string | SYSTEM 메시지는 null",
        "content": "string",
        "type": "TEXT | SYSTEM",
        "unreadCount": "number | 읽지 않은 참여자 수",
        "createdAt": "string | ISO 8601"
      }
    ],
    "hasNext": "boolean | 이전 메시지 더 있는지 여부",
    "nextCursor": "number | 다음 조회 시 lastMessageId 값 | null (hasNext=false)"
  }
}
```

**비즈니스 로직 요약:**
1. 요청자가 해당 방의 `OpenChatParticipant`인지 검증 (BR-P2-11) — 비참여자면 403
2. `lastMessageId` 미전달: 방의 최신 메시지 30건 조회 (ID DESC limit 30 → 오름차순 정렬 후 반환)
3. `lastMessageId` 전달: `id < lastMessageId`인 메시지 중 최신 `size`건 조회 (BR-P2-09)
4. 각 메시지의 `unreadCount` = 방 참여자 수 − `lastReadMessageId ≥ message.id`인 참여자 수 (ADR-04)
5. 조회자의 `OpenChatParticipant.lastReadMessageId` = 응답 메시지 중 최대 ID로 갱신 (BR-P2-09)
6. `hasNext`: 조회된 건수 == `size`이면 true (이전 메시지 존재 가능성)

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 존재하지 않는 roomId | 404 | NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 참여하지 않은 방 조회 시도 | 403 | FORBIDDEN | 채팅 내역 조회 권한이 없습니다 |

**동시성 & 멱등성:**
- `lastReadMessageId` 갱신은 단일 참여자 행 UPDATE — 동시성 이슈 없음

**엣지 케이스:**
- [ ] 방에 메시지가 없으면 `messages: []`, `hasNext: false` 반환
- [ ] `lastMessageId`가 실제 존재하지 않는 값이면 해당 ID 미만의 최신 `size`건 반환

---

### 9-4. 채팅방 목록 조회 — MY 탭 응답 변경 (Phase 2)

**`GET /open-chat-rooms?tab=MY`** (기존 2-2 확장)

Phase 2에서 MY 탭 응답에 `lastMessage`, `lastMessageAt`, `unreadCount` 필드가 추가됩니다.

**Response 변경 사항 (MY 탭 한정):**
```json
{
  "content": [
    {
      "roomId": "number",
      "name": "string",
      "description": "string | null",
      "scope": "DORMITORY | ALL",
      "currentParticipants": "number",
      "maxParticipants": "number",
      "isJoined": "boolean",
      "lastMessage": "string | 최근 메시지 내용 (최대 500자) | null (메시지 없는 경우)",
      "lastMessageAt": "string | ISO 8601 | null",
      "unreadCount": "number | 읽지 않은 메시지 수 (내 lastReadMessageId 기준)"
    }
  ]
}
```

**정렬 기준 변경:**
- Phase 1: 정렬 기준 미확정
- Phase 2: `lastMessageAt DESC` (메시지 없는 방은 `createdAt DESC`로 하단 정렬) (BR-P2-10)

**비즈니스 로직 추가:**
- `unreadCount` = 방의 메시지 중 `id > 내 lastReadMessageId`인 건수 (BR-P2-10, ADR-04)
- `lastReadMessageId = NULL`이면 해당 방의 전체 메시지 수를 `unreadCount`로 반환

---

### 9-5. 시스템 메시지 (자동 생성, API 없음)

입장/퇴장 시 서버 내부에서 자동으로 SYSTEM 메시지를 생성하고 브로드캐스트합니다.

**트리거 조건:**

| 이벤트 | 트리거 시점 | 메시지 내용 | senderId |
|--------|-----------|-----------|----------|
| 입장 | `joinRoom()` 완료 후 | `{닉네임}님이 입장했습니다` | 0 (시스템) |
| 퇴장 | `leaveRoom()` 완료 후 | `{닉네임}님이 퇴장했습니다` | 0 (시스템) |

**브로드캐스트 페이로드 (동일 형식, type=SYSTEM):**
```json
{
  "messageId": "number",
  "roomId": "number",
  "senderId": 0,
  "senderNickname": null,
  "content": "홍길동님이 입장했습니다",
  "type": "SYSTEM",
  "unreadCount": "number",
  "createdAt": "string | ISO 8601"
}
```

**트랜잭션 전략:** 입장/퇴장 트랜잭션과 별개 트랜잭션으로 저장 (ADR-06)
- 입장/퇴장 성공 후 시스템 메시지 저장 실패 시 → 입장/퇴장 자체는 롤백되지 않음

---

## 10. Phase 2 도메인 이벤트 & 사이드 이펙트 요약

| API / 트리거 | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-------------|-----------|-----------|-----------|
| 메시지 발행 (9-1) | `MessageSent` | `SimpMessagingTemplate` | `/sub/openchat/{roomId}` 브로드캐스트 |
| 채팅방 입장 (2-3) | `ParticipantJoined` | `OpenChatMessageService` | SYSTEM 메시지 저장 + 브로드캐스트 |
| 채팅방 나가기 (2-4) | `ParticipantLeft` | `OpenChatMessageService` | SYSTEM 메시지 저장 + 브로드캐스트 |
| 구독 이벤트 (9-2) | (내부) | `OpenChatWebSocketEventListener` | `lastReadMessageId` 갱신 |

---

## 11. Phase 2 API 간 의존 관계

- 메시지 발행(9-1) 전 WebSocket CONNECT 및 채팅방 구독(9-2) 선행 필요
- 채팅 내역 조회(9-3)는 채팅방 입장(2-3) 완료 후 가능 (참여자 검증)
- MY 탭 목록(9-4)의 `unreadCount`는 채팅 내역 조회(9-3) 또는 구독(9-2) 시 자동 갱신

---

## 12. Phase 2 보안 체크리스트

- [x] WebSocket CONNECT 시 JWT 검증 (`WebSocketAuthInterceptor` 재사용)
- [x] 메시지 발행 시 발행자 = 해당 방 참여자 검증 (INV-MSG-01)
- [x] 채팅 내역 조회 시 참여자 검증 (BR-P2-11)
- [x] SYSTEM 메시지 senderId=0 고정 (클라이언트 위조 불가 — 서버 내부 생성) (ADR-05)
- [ ] Rate Limit: 단기간 대량 메시지 발행 방지 — Phase 3 검토

---

## 13. Phase 2 TBD

- [ ] STOMP ERROR 프레임 스펙: 클라이언트 발행 오류 시 어떤 형식으로 에러를 전달할지
- [ ] `unreadCount` 계산에서 SYSTEM 메시지 포함 여부 (현재: 포함)
- [ ] FCM 오프라인 알림 발송 조건 및 형식 (Phase 3)
- [ ] 이미지 메시지 지원 시 `type=IMAGE` + 이미지 URL 필드 추가 (Phase 3)

# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`
> 기능: 오픈 채팅 이미지 전송

---

## 1. 공통 정보

### Base URL
`/open-chat-rooms`

### 인증 방식
- 헤더: `Authorization: Bearer {accessToken}`
- 미인증 시: `401 UNAUTHORIZED`

### 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `UNAUTHORIZED` | 401 | 인증 토큰 없음 또는 만료 |
| `OPEN_CHAT_NOT_PARTICIPANT` | 403 | 채팅방 비참여자 |
| `OPEN_CHAT_ROOM_NOT_FOUND` | 404 | 채팅방 없음 |
| `IMAGE_INVALID_FORMAT` | 400 | 허용되지 않는 이미지 포맷 |
| `IMAGE_SAVE_FAIL` | 500 | 이미지 저장 실패 |

---

## 2. API 목록

---

### 이미지 메시지 전송

**`POST /open-chat-rooms/{roomId}/messages/image`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방에 이미지를 전송한다. 저장 완료 후 WebSocket으로 전체 참여자에게 브로드캐스트된다. |
| 인증 | 필요 (JWT) |
| 권한 | `USER` — 채팅방 참여자(`OpenChatParticipant`)만 허용 |
| Content-Type | `multipart/form-data` |
| 멱등성 | 비멱등 (호출마다 새 메시지 생성) |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 이미지를 전송할 채팅방 ID |

**Request Body (multipart/form-data):**

| 필드명 | 타입 | 필수 | 설명 |
|--------|------|------|------|
| `images` | `List<MultipartFile>` | Y | 전송할 이미지 파일 목록 (1개 이상) |

**Validation Rules:**
- `images`: 1개 이상 필수
- 허용 포맷: `jpg`, `jpeg`, `png`, `gif`, `webp` (확장자·MIME 타입 모두 검증)
- 허용되지 않는 포맷(`heic`, `pdf` 등) 포함 시 전체 요청 거부

**Response (성공, HTTP 201 Created):**

```json
{
  "messageId": 1042,
  "roomId": 7,
  "senderId": 23,
  "senderNickname": "홍길동",
  "content": "",
  "type": "IMAGE",
  "imageUrls": [
    "https://host/images/open_chat_message/msg1042_uuid1.jpg",
    "https://host/images/open_chat_message/msg1042_uuid2.png"
  ],
  "unreadCount": 3,
  "createdAt": "2026-06-15T10:30:00"
}
```

**비즈니스 로직 요약:**

1. `roomId`로 채팅방 존재 여부 확인 → 없으면 404
2. 요청자가 해당 채팅방의 `OpenChatParticipant`인지 확인 (BR-01 / INV-01) → 아니면 403
3. 모든 파일의 포맷 검증 (BR-02 / INV-02): jpg, jpeg, png, gif, webp 이외 → 400
4. 파일 수 검증 (BR-03 / INV-03): 0개 → 400
5. `OpenChatMessage(type=IMAGE, content="")` DB 저장 → `messageId` 확보
6. 이미지 파일 디스크 저장 (`ImageType.OPEN_CHAT_MESSAGE`, `entityId=messageId`) (BR-04 / INV-04)
7. 저장된 이미지 URL 목록 조회 → `imageUrls` 구성
8. `OpenChatRoom.updateLastMessage("[이미지]", createdAt)` 호출
9. 발신자 `OpenChatParticipant.lastReadMessageId` 갱신
10. `unreadCount` 계산 (전체 참여자 수 - 읽은 참여자 수)
11. WebSocket `/sub/openchat/{roomId}` 브로드캐스트 (`imageUrls` 포함)

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 채팅방이 존재하지 않음 | 404 | `OPEN_CHAT_ROOM_NOT_FOUND` | 존재하지 않는 채팅방입니다 |
| 비참여자 요청 | 403 | `OPEN_CHAT_NOT_PARTICIPANT` | 채팅방 참여자가 아닙니다 |
| 허용되지 않는 이미지 포맷 | 400 | `IMAGE_INVALID_FORMAT` | 허용되지 않는 이미지 형식입니다 |
| 이미지 없이 요청 | 400 | `VALIDATION_ERROR` | 이미지를 1개 이상 첨부해주세요 |
| 디스크/DB 저장 실패 | 500 | `IMAGE_SAVE_FAIL` | 이미지 저장에 실패했습니다 |

**동시성 & 멱등성:**
- 동일 사용자가 동시에 여러 이미지를 전송해도 각각 독립적인 메시지로 생성되므로 별도 락 불필요

**사이드 이펙트 / 도메인 이벤트:**
- `ImageMessageSent`: 저장 완료 후 WebSocket `/sub/openchat/{roomId}` 브로드캐스트
- `OpenChatRoom.lastMessage` = `"[이미지]"` 로 갱신

**엣지 케이스:**
- [ ] 여러 파일 중 일부만 포맷 위반 시: 전체 요청 거부 (partial success 없음)
- [ ] 파일 크기 초과 시: Spring multipart 설정으로 처리 (413 응답, 서버 레벨 처리)

---

### 채팅 메시지 목록 조회 (기존 엔드포인트 — 응답 DTO 변경)

**`GET /open-chat-rooms/{roomId}/messages`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방 메시지를 커서 기반 페이지네이션으로 조회한다. IMAGE 타입 메시지에는 `imageUrls` 필드가 포함된다. |
| 인증 | 필요 (JWT) |
| 권한 | `USER` — 채팅방 참여자만 허용 |
| 멱등성 | 멱등 |

**Query Parameters:**

| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `lastMessageId` | Long | N | - | 이 ID보다 이전 메시지 조회 (커서) |
| `size` | int | N | 30 | 조회할 메시지 수 |

**Response (성공, HTTP 200 OK):**

```json
{
  "messages": [
    {
      "messageId": 1042,
      "roomId": 7,
      "senderId": 23,
      "senderNickname": "홍길동",
      "content": "",
      "type": "IMAGE",
      "imageUrls": [
        "https://host/images/open_chat_message/msg1042_uuid1.jpg"
      ],
      "unreadCount": 0,
      "createdAt": "2026-06-15T10:30:00"
    },
    {
      "messageId": 1041,
      "roomId": 7,
      "senderId": 22,
      "senderNickname": "김철수",
      "content": "안녕하세요",
      "type": "TEXT",
      "imageUrls": [],
      "unreadCount": 1,
      "createdAt": "2026-06-15T10:29:00"
    }
  ],
  "hasNext": true,
  "nextCursor": 1040
}
```

**변경 사항:**
- `ResponseOpenChatMessageDto`에 `imageUrls: List<String>` 필드 추가
- `TEXT`, `SYSTEM` 타입은 `imageUrls = []` (빈 배열)
- `IMAGE` 타입은 `Image` 테이블에서 `entityId=messageId`, `imageType=OPEN_CHAT_MESSAGE`로 조회한 URL 목록

**N+1 주의:**
- 메시지 목록 조회 시 IMAGE 타입 메시지마다 개별 Image 조회를 하면 N+1 발생
- `ImageRepository.findByImageTypeAndEntityIdIn(OPEN_CHAT_MESSAGE, messageIds)` 배치 조회 후 messageId로 그루핑하여 매핑

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|------------|-----------|-----------|
| `POST /messages/image` | `ImageMessageSent` | `SimpMessagingTemplate` | WebSocket `/sub/openchat/{roomId}` 브로드캐스트 |

---

## 4. API 간 의존 관계

- 이미지 전송(`POST /messages/image`) 전 채팅방 참여(`POST /open-chat-rooms/{roomId}/participants`) 선행 필요
- 이미지 전송은 텍스트 전송(WebSocket)과 독립적으로 동작하며 경쟁 조건 없음

---

## 5. 보안 체크리스트

- [x] 모든 쓰기 API에 JWT 인증 적용
- [x] 비참여자 접근 차단 (`OpenChatParticipant` 소유권 검증)
- [x] 파일 포맷 검증으로 악성 파일 업로드 방지 (확장자 + MIME 타입)
- [ ] 파일 크기 제한 명시적 설정 필요 (현재 Spring 기본값 의존)
- [ ] Rate Limit: 이미지 업로드 남용 방지 고려 (TBD)

---

## 6. 최종 검토

- [x] 신규 엔드포인트 1개, 기존 응답 DTO 변경 1개로 최소 변경
- [x] 동시성 위험 없음 확인
- [x] IMAGE 타입 메시지 N+1 위험 식별 및 해결 방안 명시
- [x] `OpenChatMessage.content NOT NULL` 제약 — `""` 저장으로 처리
- [x] WebSocket 브로드캐스트는 HTTP 트랜잭션 커밋 후 수행

---

## 7. TBD

- [ ] 메시지당 최대 이미지 첨부 개수 제한 (예: 5장 초과 시 400)
- [ ] 파일당 최대 크기 명시적 설정 (예: 10MB)
- [ ] Rate Limit 적용 여부 (예: 1분당 20회)
- [ ] 메시지 삭제 기능 구현 시 IMAGE 타입 연쇄 삭제 API 설계

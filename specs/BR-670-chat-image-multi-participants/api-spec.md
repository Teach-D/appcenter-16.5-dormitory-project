# BR-670 API 명세서 — 채팅방 이미지 다중 전송 및 참여자 단순 목록

> Base URL: `http://localhost:8080`

---

## 1. 이미지 메시지 전송 (다중 이미지 → 개별 메시지)

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/open-chat-rooms/{roomId}/messages/image` |
| **인증** | Bearer Token (JWT) 필수 |
| **설명** | 채팅방에 이미지 1~5장을 전송한다. 이미지 1장당 메시지 1개가 생성되고, 각 메시지마다 WebSocket으로 브로드캐스트된다. |

> **동작 변경**: 기존에는 N장 → 메시지 1개(imageUrls 배열). 이번 변경으로 N장 → 메시지 N개(각 imageUrls에 URL 1건).

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 이미지를 전송할 채팅방 ID |

#### Request Body

Content-Type: `multipart/form-data`

| 파트 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `images` | `MultipartFile[]` | ✅ | 1~5장, 장당 최대 10 MB, jpg/jpeg/png/gif/webp | 전송할 이미지 파일 목록 |

### Response

#### 성공 응답 — `201 Created`

이미지 N장 전송 시 크기 N인 배열을 반환한다. 배열 순서 = 전송 순서.

| 필드 | 타입 | 설명 |
|------|------|------|
| `[].messageId` | `Long` | 생성된 메시지 ID |
| `[].roomId` | `Long` | 채팅방 ID |
| `[].senderId` | `Long` | 발신자 User ID |
| `[].senderNickname` | `String` | 발신자 이름 |
| `[].content` | `String` | 항상 `""` (이미지 메시지) |
| `[].type` | `String` | 항상 `"IMAGE"` |
| `[].imageUrls` | `String[]` | 해당 메시지에 연결된 이미지 URL (1건) |
| `[].unreadCount` | `Int` | 현재 미열람 참여자 수 |
| `[].createdAt` | `String (ISO 8601)` | 메시지 생성 시각 |

```json
[
  {
    "messageId": 101,
    "roomId": 5,
    "senderId": 42,
    "senderNickname": "홍길동",
    "content": "",
    "type": "IMAGE",
    "imageUrls": ["http://localhost:8080/images/open-chat-message/101/photo1.jpg"],
    "unreadCount": 3,
    "createdAt": "2026-07-05T12:00:00"
  },
  {
    "messageId": 102,
    "roomId": 5,
    "senderId": 42,
    "senderNickname": "홍길동",
    "content": "",
    "type": "IMAGE",
    "imageUrls": ["http://localhost:8080/images/open-chat-message/102/photo2.jpg"],
    "unreadCount": 3,
    "createdAt": "2026-07-05T12:00:00"
  }
]
```

#### WebSocket 브로드캐스트

이미지 N장 전송 시 `/sub/openchat/{roomId}` 토픽으로 N회 브로드캐스트된다.  
각 메시지 payload는 위 성공 응답의 단일 원소와 동일한 `ResponseOpenChatMessageDto` 구조이다.

#### 에러 응답

에러 응답 공통 형식:
```json
{
  "code": 22014,
  "name": "OPEN_CHAT_IMAGE_COUNT_EXCEEDED",
  "message": "[OpenChat] 이미지는 최대 5장까지 전송 가능합니다.",
  "errors": null
}
```

| 상태 코드 | `code` | `name` | 발생 조건 |
|-----------|--------|--------|-----------|
| `400 Bad Request` | `22013` | `OPEN_CHAT_IMAGE_EMPTY` | `images` 파트 없음 또는 빈 리스트 |
| `400 Bad Request` | `22014` | `OPEN_CHAT_IMAGE_COUNT_EXCEEDED` | 이미지 6장 이상 |
| `400 Bad Request` | `6005` | `IMAGE_INVALID_FORMAT` | 파일 크기 10 MB 초과, 허용되지 않는 확장자·MIME 타입 |
| `403 Forbidden` | `22005` | `OPEN_CHAT_NOT_PARTICIPANT` | 요청자가 해당 채팅방 참여자 아님 |
| `404 Not Found` | `22001` | `OPEN_CHAT_ROOM_NOT_FOUND` | 채팅방 없음 |

---

## 2. 채팅방 참여자 단순 목록 조회

| 항목 | 내용 |
|------|------|
| **메서드** | `GET` |
| **경로** | `/open-chat-rooms/{roomId}/participants/simple` |
| **인증** | Bearer Token (JWT) 필수 |
| **설명** | 채팅방 참여자의 userId와 이름만 반환한다. 기존 `/participants` 와 달리 isHost·isAdmin·joinedAt을 포함하지 않는 경량 API이다. |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 조회할 채팅방 ID |

### Response

#### 성공 응답 — `200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 채팅방 ID |
| `participants` | `Object[]` | 참여자 목록 |
| `participants[].userId` | `Long` | 참여자 User ID |
| `participants[].name` | `String` | 참여자 이름. `ROLE_ADMIN` 유저는 `"관리자"` 고정 |

```json
{
  "roomId": 5,
  "participants": [
    { "userId": 42, "name": "홍길동" },
    { "userId": 43, "name": "김철수" },
    { "userId": 1,  "name": "관리자" }
  ]
}
```

#### 에러 응답

| 상태 코드 | `code` | `name` | 발생 조건 |
|-----------|--------|--------|-----------|
| `403 Forbidden` | `22002` | `OPEN_CHAT_ROOM_FORBIDDEN` | 요청자가 해당 채팅방 참여자 아님 |
| `404 Not Found` | `22001` | `OPEN_CHAT_ROOM_NOT_FOUND` | 채팅방 없음 |

---

## 변경 전/후 비교

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| `POST …/messages/image` 응답 타입 | `ResponseOpenChatMessageDto` (단건) | `List<ResponseOpenChatMessageDto>` (N건) |
| 이미지 3장 전송 시 메시지 저장 수 | 1개 (imageUrls에 URL 3건) | 3개 (각 imageUrls에 URL 1건) |
| WebSocket 브로드캐스트 횟수 | 1회 | N회 (이미지 수만큼) |
| 참여자 단순 목록 엔드포인트 | 없음 | `GET …/participants/simple` 신규 추가 |

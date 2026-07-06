# 채팅 FCM 알림 모드 설정 API 명세서 (BR-672)

> Base URL: `https://{host}/open-chat-rooms`
> 인증: 모든 엔드포인트에 Bearer Token 필요

---

## 채팅방 알림 모드 변경

| 항목 | 내용 |
|------|------|
| **메서드** | `PATCH` |
| **경로** | `/open-chat-rooms/{roomId}/participants/me/notification` |
| **인증** | Bearer Token (JWT) |
| **설명** | 로그인 사용자의 특정 채팅방 FCM 알림 수신 방식을 변경한다 |

> 기존 `?enabled=true/false` 쿼리 파라미터 방식에서 Request Body 방식으로 **변경됨**.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 채팅방 ID |

#### Request Body

Content-Type: `application/json`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `mode` | `String (enum)` | ✅ | 알림 모드 (`EVERY` / `BUNDLED` / `OFF`) |

**`mode` 값 설명**

| 값 | 동작 |
|----|------|
| `EVERY` | 새 메시지 수신 시 즉시 FCM 푸시 발송 |
| `BUNDLED` | 매시 정각에 1시간치 안읽은 메시지를 묶어서 FCM 발송 |
| `OFF` | FCM 푸시 발송 안 함 |

```json
{
  "mode": "BUNDLED"
}
```

### Response

#### 성공 응답 — `204 No Content`

응답 Body 없음.

#### 에러 응답

| 상태 코드 | ErrorCode | code | 발생 조건 |
|-----------|-----------|------|----------|
| `400 Bad Request` | `VALIDATION_FAILED` | 5001 | `mode` 필드 누락 또는 enum 값 불일치 |
| `401 Unauthorized` | `JWT_ENTRY_POINT` | — | 인증 토큰 없음 또는 만료 |
| `404 Not Found` | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 22004 | 해당 채팅방의 참여자가 아님 |

**에러 응답 형식**

```json
{
  "code": 22004,
  "name": "OPEN_CHAT_PARTICIPANT_NOT_FOUND",
  "message": "[OpenChat] 참여하지 않은 채팅방입니다.",
  "errors": null
}
```

`VALIDATION_FAILED`의 경우 `errors` 배열에 필드별 오류 메시지가 포함된다.

```json
{
  "code": 5001,
  "name": "VALIDATION_FAILED",
  "message": "DTO에서 요청한 값이 올바르지 않습니다.",
  "errors": ["mode: must not be null"]
}
```

---

## 알림 모드 기본값 (입장 시)

채팅방 입장(`POST /open-chat-rooms/{roomId}/participants/me`) 또는 채팅방 생성 시 알림 모드는 자동으로 `EVERY`로 설정된다. 별도 API 호출 불필요.

---

## FCM 푸시 Payload 형식

### EVERY 모드 (즉시 발송)

| 필드 | 값 |
|------|-----|
| `title` | 채팅방 이름 |
| `body` | 메시지 내용 (이미지 메시지는 `"[이미지]"`) |

### BUNDLED 모드 (묶음 발송)

| 필드 | 값 |
|------|-----|
| `title` | 채팅방 이름 |
| `body` | `"새 메시지 n개"` |

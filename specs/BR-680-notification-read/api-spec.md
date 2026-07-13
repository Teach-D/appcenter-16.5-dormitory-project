# 알림 읽음 처리 API 명세서 (BR-680)

> Base URL: `{server-host}`  
> Controller: `NotificationController` (`/notifications`)

---

## 알림 읽음 처리

| 항목 | 내용 |
|------|------|
| **메서드** | `PATCH` |
| **경로** | `/notifications/read` |
| **인증** | Bearer Token (JWT) 필수 |
| **설명** | 공지사항 또는 채팅방 진입 시 해당 알림의 읽음 상태를 서버에 동기화한다. NOTICE 타입은 `UserNotification.isRead`를, CHAT 타입은 `OpenChatParticipant.lastReadMessageId`를 업데이트한다. |

### Request

#### Request Body

Content-Type: `application/json`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `type` | `String` | ✅ | 알림 타입. `"NOTICE"` 또는 `"CHAT"` 만 허용 (대소문자 구분) |
| `targetId` | `String` | ✅ | 읽음 처리할 대상의 PK. NOTICE이면 공지사항 ID, CHAT이면 채팅방 ID |

```json
// 공지사항 진입 시
{
  "type": "NOTICE",
  "targetId": "5678"
}

// 채팅방 진입 시
{
  "type": "CHAT",
  "targetId": "1234"
}
```

### Response

#### 성공 응답 — `200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 항상 `true` |
| `message` | `String` | 고정 문자열 |

```json
{
  "success": true,
  "message": "성공적으로 읽음 처리되었습니다."
}
```

> **멱등성**: NOTICE 타입에서 대상 `UserNotification`이 없거나 이미 읽음 상태여도 200 OK를 반환한다.  
> **CHAT 타입**: 채팅방에 메시지가 없으면 `lastReadMessageId`를 변경하지 않고 200 OK를 반환한다.

#### 에러 응답

에러 응답 공통 형식:

```json
{
  "code": 5001,
  "name": "VALIDATION_FAILED",
  "message": "[Validation] Request에서 요청한 값이 올바르지 않습니다."
}
```

| 상태 코드 | ErrorCode | code | 발생 조건 |
|-----------|-----------|------|-----------|
| `400 Bad Request` | `VALIDATION_FAILED` | 5001 | `type`이 `null`이거나 `NOTICE`/`CHAT` 이외의 값, 또는 `targetId`가 비어있거나 숫자로 파싱 불가 |
| `401 Unauthorized` | `JWT_ENTRY_POINT` | 1007 | JWT 토큰 없음 또는 유효하지 않음 |
| `403 Forbidden` | `OPEN_CHAT_NOT_PARTICIPANT` | 22005 | CHAT 타입 요청 시 해당 채팅방의 참여자가 아님 |

```json
// 401 예시
{
  "code": 1007,
  "name": "JWT_ENTRY_POINT",
  "message": "[Jwt] 인증되지 않은 사용자입니다."
}

// 403 예시 (CHAT 타입, 비참여자)
{
  "code": 22005,
  "name": "OPEN_CHAT_NOT_PARTICIPANT",
  "message": "[OpenChat] 채팅 내역 조회 권한이 없습니다."
}
```

---

## 추론 항목

> 아래 항목은 코드에서 명시적으로 확인되지 않아 기존 코드 패턴 및 명세로 추론했습니다.

- **타입 유효성 검증**: `FcmRoutingType` Jackson 역직렬화 시 잘못된 값을 400으로 반환하려면, 구현 시 `HttpMessageNotReadableException` 핸들러를 `GlobalExceptionHandler`에 추가하거나 DTO에서 String으로 받아 서비스에서 명시 검증하는 방식을 선택해야 한다.
- **OPEN_CHAT_NOT_PARTICIPANT HTTP 상태**: requirement.md에는 404로 기술되어 있으나 `ErrorCode.java` 기준 실제 HTTP 상태는 `403 FORBIDDEN`이다. 명세 오기재이므로 코드 기준인 403으로 작성했다.

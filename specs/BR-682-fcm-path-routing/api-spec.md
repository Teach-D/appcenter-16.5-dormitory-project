# BR-682 FCM path 라우팅 API 명세서

> 이 BR은 신규 REST 엔드포인트를 추가하지 않는다.  
> 변경 계약은 두 가지다.  
> 1. **FCM 푸시 페이로드** — 서버 → 모바일 클라이언트로 전달되는 data 필드 추가  
> 2. **알림 읽음 처리 API** — `type` 필드에 허용되는 enum 값 확장

---

## 1. FCM 푸시 페이로드 (서버 → Firebase → 클라이언트)

`FcmAsyncSender.sendOutboxBatch()`가 전송하는 FCM 메시지의 data 필드 계약.  
`routingType` / `routingId`가 모두 존재할 때만 아래 필드가 포함된다.

### data 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `path` | `String` | **(신규)** 앱 내 이동 경로 |
| `type` | `String` | routingType enum 이름 (기존 유지, 하위 호환) |
| `{dataKey}` | `String` | routingId 문자열 값 (기존 유지) |

### routingType별 페이로드 값

| routingType | `path` | `apns.thread-id` | `data.type` | `data.{dataKey}` |
|-------------|--------|-----------------|-------------|-----------------|
| `CHAT_OPEN` | `/chat/open/{id}` | `chat_room_{id}` | `CHAT_OPEN` | `chatRoomId: "{id}"` |
| `CHAT_PERSONAL` | `/chat/open/{id}` | `chat_room_{id}` | `CHAT_PERSONAL` | `chatRoomId: "{id}"` |
| `ANNOUNCEMENT` | `/announcements/{id}` | `notice` | `ANNOUNCEMENT` | `noticeId: "{id}"` |
| `COMPLAINT` | `/complain/{id}` | `complaint` | `COMPLAINT` | `complaintId: "{id}"` |
| `ROOMMATE_POST` | `/roommate/list/{id}` | `roommate` | `ROOMMATE_POST` | `roommatePostId: "{id}"` |
| `null` (routingType 없음) | (없음) | (없음) | (없음) | (없음) |

### 페이로드 예시

#### CHAT_OPEN / CHAT_PERSONAL (roomId = 1234)
```json
{
  "notification": {
    "title": "채팅방 이름",
    "body": "메시지 내용"
  },
  "data": {
    "path": "/chat/open/1234",
    "type": "CHAT_OPEN",
    "chatRoomId": "1234"
  },
  "apns": {
    "aps": {
      "sound": "default",
      "thread-id": "chat_room_1234"
    }
  },
  "android": {
    "notification": {
      "sound": "default",
      "tag": "chat_room_1234"
    }
  }
}
```

#### ANNOUNCEMENT (announcementId = 7)
```json
{
  "notification": {
    "title": "공지 제목",
    "body": "공지 내용 요약"
  },
  "data": {
    "path": "/announcements/7",
    "type": "ANNOUNCEMENT",
    "noticeId": "7"
  },
  "apns": {
    "aps": {
      "sound": "default",
      "thread-id": "notice"
    }
  },
  "android": {
    "notification": {
      "sound": "default",
      "tag": "notice"
    }
  }
}
```

#### routingType 없음 (기본 알림)
```json
{
  "notification": {
    "title": "알림 제목",
    "body": "알림 내용"
  }
}
```

---

## 2. 알림 읽음 처리 API (기존 엔드포인트, type 값 확장)

| 항목 | 내용 |
|------|------|
| **메서드** | `PATCH` |
| **경로** | `/notifications/read` |
| **인증** | Bearer Token (Spring Security) |
| **설명** | FCM 알림 탭에서 특정 알림을 읽음 처리한다 |

### Request

#### Request Body
Content-Type: `application/json`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `type` | `FcmRoutingType` | ✅ | 알림 유형 (아래 허용 값 참고) |
| `targetId` | `String` | ✅ | 대상 리소스 ID (숫자 문자열, 양수) |

**`type` 허용 값 (BR-682 이후)**

| 값 | 설명 | 읽음 처리 대상 |
|----|------|--------------|
| `ANNOUNCEMENT` | 공지 알림 | 해당 공지의 UserNotification 읽음 처리 |
| `CHAT_OPEN` | 오픈채팅 알림 | 해당 채팅방 메시지 읽음 처리 |
| `CHAT_PERSONAL` | 개인(룸메이트) 채팅 알림 | 해당 채팅방 메시지 읽음 처리 |
| `COMPLAINT` | 민원 알림 | 해당 채팅방 메시지 읽음 처리 |
| `ROOMMATE_POST` | 룸메이트 게시글 알림 | 해당 채팅방 메시지 읽음 처리 |

> **Breaking change**: 기존 `NOTICE`, `CHAT` 값은 더 이상 허용되지 않는다.  
> 클라이언트는 `ANNOUNCEMENT` / `CHAT_OPEN` / `CHAT_PERSONAL`로 교체해야 한다.

```json
{
  "type": "ANNOUNCEMENT",
  "targetId": "42"
}
```

### Response

#### 성공 응답 — `200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `Boolean` | 처리 성공 여부 |
| `message` | `String` | 결과 메시지 |

```json
{
  "success": true,
  "message": "성공적으로 읽음 처리되었습니다."
}
```

#### 에러 응답

| 상태 코드 | 발생 조건 |
|-----------|-----------|
| `400 Bad Request` | `type` 누락 / 허용되지 않는 enum 값 / `targetId`가 빈 문자열이거나 양수가 아닌 경우 |
| `401 Unauthorized` | 인증 토큰 없음 또는 만료 |

---

## 추론 항목

> 아래 항목은 코드에서 명시적으로 확인되지 않아 코드 패턴으로 추론했습니다.

- `COMPLAINT`, `ROOMMATE_POST` type으로 `markAsRead` 호출 시: `NotificationReadService`에서 `ANNOUNCEMENT` 분기가 아니므로 `openChatMessageService.markChatRoomAsRead(targetId, userId)`로 위임된다. 이 두 타입에 대한 `markChatRoomAsRead` 동작이 의미 있는지는 별도 확인 필요.
- 에러 응답 형식: 프로젝트 공통 `GlobalExceptionHandler` / `ErrorCode` 체계를 따름 (실제 에러 body 구조는 해당 핸들러 참고).

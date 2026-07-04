# BR-657 — 오픈채팅 메시지 신고 API 명세

**여기에 정의된 엔드포인트 외에는 구현하지 않는다.**

---

## 엔드포인트 목록

| 메서드 | 경로 | 설명 |
|--------|------|------|
| `POST` | `/open-chat-rooms/messages/{messageId}/reports` | 오픈채팅 메시지 신고 접수 |

---

### `POST /open-chat-rooms/messages/{messageId}/reports`

| 항목 | 내용 |
|------|------|
| **인증** | Bearer Token 필수 (USER 권한) |
| **설명** | 방 참여자가 타인의 메시지를 신고한다. 신고 내용은 DB에 저장되며 자동 제재 없음 |

#### Request

**Path Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `messageId` | `Long` | Y | 신고 대상 메시지 ID |

**Request Body** — `application/json`

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `reason` | `String` | Y | `@NotBlank` | 신고 사유 (빈 문자열 불가) |

```json
{
  "reason": "스팸성 광고입니다"
}
```

#### Response

**성공 — `201 Created`**

바디 없음 (`ResponseEntity<Void>`)

**오류**

| HTTP | ErrorCode | 조건 |
|------|-----------|------|
| `400 Bad Request` | *(validation)* | `reason` 누락 또는 빈 문자열 (`@Valid` + `@NotBlank`) |
| `400 Bad Request` | `OPEN_CHAT_REPORT_SELF` (22019) | 신고자 본인이 작성한 메시지를 신고 |
| `401 Unauthorized` | *(Spring Security)* | 비인증 요청 |
| `403 Forbidden` | `OPEN_CHAT_NOT_PARTICIPANT` (22005) | 신고자가 해당 방의 현재 참여자가 아님 |
| `404 Not Found` | `OPEN_CHAT_MESSAGE_NOT_FOUND` (22018) | `messageId`에 해당하는 메시지 없음 |

---

## 이 API가 하지 않는 것 (Non-goals)

- 신고 목록 조회 (`GET /open-chat-rooms/messages/{messageId}/reports`) — 관리자 기능, 이번 범위 외
- 신고 후 메시지 자동 숨김 또는 발신자 자동 차단
- 신고 취소
- 동일 신고자의 중복 신고 차단 (중복 허용)
- FCM 푸시 알림 발송

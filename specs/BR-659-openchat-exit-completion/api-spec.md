# 오픈채팅 퇴장(BR-659) API 명세서

> Base URL: `https://{host}/open-chat-rooms`
> 인증: 모든 엔드포인트 Bearer Token 필요

---

## 강제퇴장

| 항목 | 내용 |
|------|------|
| **메서드** | `DELETE` |
| **경로** | `/open-chat-rooms/{roomId}/participants/{targetUserId}` |
| **인증** | Bearer Token |
| **설명** | 방장 또는 ADMIN이 특정 참여자를 강제퇴장시킨다. BR-659 변경: `reason` 필수 파라미터 추가, ADMIN이 방장 강퇴 시 `newHostUserId` 조건부 필수 |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 채팅방 ID |
| `targetUserId` | `Long` | ✅ | 강퇴 대상 사용자 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `reason` | `KickReason` | ✅ | 강퇴 사유 (enum) |
| `newHostUserId` | `Long` | 조건부 ✅ | ADMIN이 방장을 강퇴할 때, 남은 참여자가 있으면 필수 |

**KickReason 허용값**

| 값 | 의미 |
|----|------|
| `SPAM` | 도배/광고 |
| `ABUSE` | 욕설/비방 |
| `IMPERSONATION` | 사칭 |
| `REPORT_ACCUMULATED` | 신고 누적 |
| `OTHER` | 기타 |

### Response

#### 성공 응답 — `204 No Content`

응답 Body 없음.

#### 에러 응답

| 상태 코드 | ErrorCode | code | 발생 조건 |
|-----------|-----------|------|-----------|
| `400 Bad Request` | `OPEN_CHAT_NEW_HOST_REQUIRED` | 22018 | ADMIN이 방장 강퇴 시 `newHostUserId` 누락 + 다른 참여자 있음 |
| `400 Bad Request` | `OPEN_CHAT_ALREADY_HOST` | 22015 | `newHostUserId`가 이미 방장인 사용자 |
| `403 Forbidden` | `OPEN_CHAT_KICK_FORBIDDEN` | 22017 | 권한 없음 (일반 참여자 강퇴 시도, 방장이 다른 방장 강퇴 시도, 자기 자신 강퇴 시도, ADMIN 강퇴 시도) |
| `404 Not Found` | `OPEN_CHAT_ROOM_NOT_FOUND` | 22001 | 존재하지 않는 roomId |
| `404 Not Found` | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 22004 | targetUserId 또는 newHostUserId가 해당 방 미참여자 |

**에러 응답 형식**

```json
{
  "code": 22018,
  "name": "OPEN_CHAT_NEW_HOST_REQUIRED",
  "message": "[OpenChat] 방장 강퇴 시 새 방장을 지정해야 합니다.",
  "errors": null
}
```

#### 시나리오별 동작 요약

| 호출자 | targetUserId 역할 | newHostUserId | 결과 |
|--------|------------------|---------------|------|
| 방장 | 일반 참여자 | 무시 | 204, participant 삭제 |
| 방장 | 다른 방장 | — | 403 `OPEN_CHAT_KICK_FORBIDDEN` |
| ADMIN | 일반 참여자 | 무시 | 204, participant 삭제 |
| ADMIN | 방장 (다른 참여자 있음) | 유효한 참여자 ID | 204, 새 방장 지정 + 대상 삭제 |
| ADMIN | 방장 (다른 참여자 있음) | 없음 | 400 `OPEN_CHAT_NEW_HOST_REQUIRED` |
| ADMIN | 방장 (방장 혼자) | 무시 | 204, participant + room 삭제 |
| 누구든 | 자기 자신 | — | 403 `OPEN_CHAT_KICK_FORBIDDEN` |
| 누구든 | ADMIN | — | 403 `OPEN_CHAT_KICK_FORBIDDEN` |

---

## 자진 퇴장

| 항목 | 내용 |
|------|------|
| **메서드** | `DELETE` |
| **경로** | `/open-chat-rooms/{roomId}/participants/me` |
| **인증** | Bearer Token |
| **설명** | 현재 사용자가 채팅방을 자진 퇴장한다. BR-659 변경: 비공식 방에서 방장 혼자 남은 경우 방 하드 삭제 후 `roomDeleted: true` 반환 |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 채팅방 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `newHostUserId` | `Long` | 조건부 ✅ | 방장이 자진 퇴장 시 새 방장 지정. 방장 + 다른 참여자 있음일 때 필수 |

### Response

#### 성공 응답 — `200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomDeleted` | `Boolean` | 방 삭제 여부. 비공식 방에서 방장 혼자 퇴장 시 `true`, 나머지는 `false` |

```json
{ "roomDeleted": false }
```

```json
{ "roomDeleted": true }
```

#### 에러 응답

| 상태 코드 | ErrorCode | code | 발생 조건 |
|-----------|-----------|------|-----------|
| `400 Bad Request` | `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` | 22016 | ① 비공식 방에서 방장 + 다른 참여자 있음 + `newHostUserId` 없음 ② 공식 방(`isOfficial=true`)에서 방장 혼자 퇴장 |
| `400 Bad Request` | `OPEN_CHAT_ALREADY_HOST` | 22015 | `newHostUserId`가 이미 방장 또는 자기 자신 |
| `403 Forbidden` | `OPEN_CHAT_ROOM_FORBIDDEN` | 22002 | 방장이 아닌 사용자가 `newHostUserId` 전달 |
| `404 Not Found` | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 22004 | 해당 방 미참여자 또는 `newHostUserId`가 미참여자 |

#### 시나리오별 동작 요약

| 호출자 역할 | 방 종류 | 남은 참여자 | newHostUserId | 결과 |
|------------|--------|-------------|---------------|------|
| 일반 참여자 | 무관 | 무관 | 무관 | `{ roomDeleted: false }` |
| 방장 | 무관 | 다른 방장 있음 | 무관 | `{ roomDeleted: false }` |
| 방장 (sole host) | 비공식 | 없음 (혼자) | 무관 | `{ roomDeleted: true }`, room + participant 삭제 |
| 방장 (sole host) | 공식 | 없음 (혼자) | 무관 | 400 `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` |
| 방장 (sole host) | 무관 | 다른 참여자 있음 | 유효한 참여자 ID | `{ roomDeleted: false }`, 새 방장 지정 후 퇴장 |
| 방장 (sole host) | 무관 | 다른 참여자 있음 | 없음 | 400 `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` |

---

## 추론 항목

> 코드에서 명시적으로 확인되지 않아 추론한 항목입니다.

- **`reason` 누락 시 응답**: `MissingServletRequestParameterException` 발생. 현재 `GlobalExceptionHandler`에 해당 핸들러 없어 `handleUnexpectedException`(500)으로 떨어질 수 있음. `/implement` 단계에서 핸들러 추가 또는 명시적 검증 처리 권장.
- **`reason` 잘못된 enum 값**: `MethodArgumentTypeMismatchException` 발생 → 동일하게 500. 핸들러 추가 권장.

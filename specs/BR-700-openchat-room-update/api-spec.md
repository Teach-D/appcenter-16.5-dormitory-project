# BR-700 오픈채팅방 정보 수정 API 명세서

> Base URL: `https://<host>/open-chat-rooms`
> 인증: 모든 요청에 `Authorization: Bearer {accessToken}` 헤더 필요

---

## 채팅방 정보 수정 (방장 전용)

| 항목 | 내용 |
|------|------|
| **메서드** | `PATCH` |
| **경로** | `/open-chat-rooms/{roomId}` |
| **인증** | Bearer Token 필요 |
| **설명** | 방장이 오픈채팅방(OPEN·DERIVED 타입)의 정보를 부분 수정한다. `null` 필드는 변경하지 않는다. |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 수정할 채팅방 ID |

#### Request Body

Content-Type: `application/json`

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `name` | `String` | ❌ | 1~30자 | 채팅방 이름. `null` = 변경 안 함 |
| `description` | `String` | ❌ | max 100자 | 채팅방 설명. `null` = 변경 안 함 |
| `scope` | `String` | ❌ | `DORMITORY` \| `ALL` | 공개 범위. `null` = 변경 안 함 |
| `maxParticipants` | `Integer` | ❌ | 2~100, 현재 참여자 수 이상 | 최대 인원. `null` = 변경 안 함 |
| `password` | `String` | ❌ | max 50자 | `null` = 변경 안 함 / `""` = 비밀번호 해제 / 값 = 비밀번호 설정 |
| `isPublic` | `Boolean` | ❌ | - | 검색 목록 노출 여부. `null` = 변경 안 함 |

```json
{
  "name": "새로운 방 이름",
  "description": "방 설명을 바꿨어요",
  "scope": "ALL",
  "maxParticipants": 20,
  "password": "1234",
  "isPublic": true
}
```

**비밀번호 해제 예시** (`""` 전달 시 비밀번호 null로 초기화):
```json
{
  "password": ""
}
```

**이름만 수정 예시** (나머지 필드 생략 또는 `null`):
```json
{
  "name": "변경된 이름만"
}
```

### Response

#### 성공 응답 — `204 No Content`

응답 바디 없음.

#### 에러 응답

에러 응답 공통 형식:
```json
{
  "code": 22025,
  "name": "OPEN_CHAT_NOT_HOST",
  "message": "[OpenChat] 방장만 채팅방 정보를 수정할 수 있습니다."
}
```

Bean Validation 실패 시 `errors` 배열 포함:
```json
{
  "code": 5001,
  "name": "VALIDATION_FAILED",
  "message": "DTO에서 요청한 값이 올바르지 않습니다.",
  "errors": [
    "name: size must be between 1 and 30",
    "maxParticipants: must be greater than or equal to 2"
  ]
}
```

| 상태 코드 | ErrorCode | code | 발생 조건 |
|-----------|-----------|------|-----------|
| `400 Bad Request` | `VALIDATION_FAILED` | 5001 | `name` 길이 초과 / `maxParticipants` 범위(2~100) 위반 |
| `400 Bad Request` | `OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL` | 22026 | `maxParticipants` < 현재 참여자 수 |
| `401 Unauthorized` | `JWT_ENTRY_POINT` | 1007 | 인증 토큰 없음 또는 만료 |
| `403 Forbidden` | `OPEN_CHAT_NOT_HOST` | 22025 | 요청자가 방장이 아님 |
| `403 Forbidden` | `OPEN_CHAT_ROOM_FORBIDDEN` | 22002 | `roomType`이 PERSONAL이거나 `isOfficial = true`인 방 |
| `404 Not Found` | `OPEN_CHAT_ROOM_NOT_FOUND` | 22001 | 해당 `roomId` 채팅방 없음 |
| `404 Not Found` | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 22004 | 요청자가 해당 채팅방의 참여자가 아님 |

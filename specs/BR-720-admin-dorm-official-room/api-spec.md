# BR-720 — 관리자 기숙사별 공식 오픈채팅방 API 명세서

> Base URL: `http://localhost:8080`
>
> 인증: 모든 엔드포인트는 `Authorization: Bearer {accessToken}` 헤더 필요

---

## 에러 응답 공통 형식

모든 에러는 아래 형식으로 반환된다.

```json
{
  "code": 22027,
  "name": "OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS",
  "message": "[OpenChat] 해당 기숙사의 공식 오픈채팅방이 이미 존재합니다.",
  "errors": null
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `code` | `Integer` | 내부 에러 코드 |
| `name` | `String` | 에러 코드 이름 |
| `message` | `String` | 에러 메시지 |
| `errors` | `List<String>` \| `null` | 필드 유효성 오류 목록 (DTO 검증 실패 시만 값 존재) |

---

## 1. 기숙사 공식 오픈채팅방 생성

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/admin/open-chat-rooms/dorm` |
| **인증** | Bearer Token (ROLE_ADMIN 전용) |
| **설명** | 특정 기숙사의 공식 오픈채팅방을 생성하고, 현재 해당 기숙사로 등록된 모든 유저를 일괄 참여시킨다. 기숙사당 1개만 허용된다. |

### Request

#### Request Body

Content-Type: `application/json`

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `name` | `String` | ✅ | 최대 30자, 공백 불가 | 채팅방 이름 |
| `description` | `String` | ❌ | 최대 100자 | 채팅방 설명 |
| `dormType` | `String` | ✅ | `"1기숙사"` \| `"2기숙사"` \| `"3기숙사"` | 공식 방을 생성할 기숙사 (`"해당없음"` 불가) |

```json
{
  "name": "1기숙사 공식 오픈채팅",
  "description": "1기숙사 거주 학생들을 위한 공식 채팅방입니다.",
  "dormType": "1기숙사"
}
```

### Response

#### 성공 응답 — `201 Created`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 생성된 공식 오픈채팅방 ID |

```json
{
  "roomId": 42
}
```

**부수 효과**: 요청한 `dormType`과 일치하는 모든 유저(`User.dormType = 요청 dormType`)가 생성된 방의 `OpenChatParticipant`로 일괄 등록된다. 해당 기숙사 유저가 없으면 참여자 없이 방만 생성된다.

#### 에러 응답

| 상태 코드 | 에러 코드 | code | 발생 조건 |
|-----------|-----------|------|-----------|
| `400 Bad Request` | `VALIDATION_FAILED` | 5001 | `name` 누락·공백·30자 초과, `dormType` 누락 |
| `400 Bad Request` | `INVALID_DORM_TYPE` | 14007 | `dormType`이 `"해당없음"` 이거나 허용되지 않는 값 |
| `401 Unauthorized` | `JWT_ENTRY_POINT` | 1007 | 토큰 없음 또는 유효하지 않은 토큰 |
| `403 Forbidden` | `JWT_ACCESS_DENIED` | 1008 | ROLE_ADMIN이 아닌 사용자의 요청 |
| `409 Conflict` | `OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS` | 22027 | 해당 기숙사의 공식 방이 이미 존재 |

**400 예시** (dormType 누락 — DTO 검증 실패):
```json
{
  "code": 5001,
  "name": "VALIDATION_FAILED",
  "message": "DTO에서 요청한 값이 올바르지 않습니다.",
  "errors": ["dormType: must not be null"]
}
```

**400 예시** (NONE dormType):
```json
{
  "code": 14007,
  "name": "INVALID_DORM_TYPE",
  "message": "[Complaint] 올바르지 않은 기숙사 유형입니다.",
  "errors": null
}
```

**409 예시**:
```json
{
  "code": 22027,
  "name": "OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS",
  "message": "[OpenChat] 해당 기숙사의 공식 오픈채팅방이 이미 존재합니다.",
  "errors": null
}
```

---

## 2. 기숙사 변경 시 공식 방 자동 재배정 (기존 API 동작 변경)

| 항목 | 내용 |
|------|------|
| **메서드** | `PUT` |
| **경로** | `/users` |
| **인증** | Bearer Token (일반 유저) |
| **설명** | 기존 유저 정보 수정 API. BR-720으로 인해 `dormType` 변경 시 공식 오픈채팅방 자동 재배정 부수 효과가 추가된다. |

### 신규 부수 효과 (BR-720 추가)

`dormType`이 실제로 변경될 때만 아래 동작이 발생한다.

| 조건 | 자동 처리 |
|------|----------|
| 이전 기숙사 공식 방에 참여 중 | 해당 방에서 자동 퇴장 (참여 레코드 삭제) |
| 이전 기숙사 공식 방에 참여하지 않은 상태 | 퇴장 처리 없음 |
| 새 기숙사 공식 방이 존재 | 해당 방에 자동 입장 (이미 참여 중이면 중복 삽입 없음) |
| 새 기숙사 공식 방이 존재하지 않음 | 입장 처리 없음 |
| 새 `dormType`이 `"해당없음"` | 입장 처리 없음 |

> **Request/Response 스키마는 기존 `PUT /users` 스펙과 동일하다** — 이 문서는 부수 효과만 정의한다.

---

## 생성된 공식 방 특성

방 생성 시 서버가 고정하는 속성값:

| 속성 | 고정값 | 설명 |
|------|--------|------|
| `isOfficial` | `true` | 공식 방 표시 — 수정·삭제 불가 |
| `scope` | `DORMITORY` | 기숙사 범위 |
| `roomType` | `OPEN` | 오픈 채팅방 유형 |
| `maxParticipants` | `2147483647` | 사실상 무제한 (`Integer.MAX_VALUE`) |
| `isPublic` | `true` | 공개 방 |
| `targetDorm` | 요청 `dormType` | 기숙사 식별자 (신규 필드) |

---

## 추론 항목

> 코드에 명시되지 않아 패턴·컨벤션으로 추론한 항목입니다.

- `INVALID_DORM_TYPE` 에러 코드는 기존에 Complaint 도메인(`14007`)에서 사용하는 것을 재사용한다. 에러 메시지에 "[Complaint]" 프리픽스가 남아 있으나, 구현 시 새 에러 코드 추가 여부를 검토할 수 있다.
- 403 Forbidden 응답은 Spring Security의 AccessDeniedHandler를 통해 `JWT_ACCESS_DENIED(1008)` 코드로 반환된다 (SecurityConfig 기존 패턴 기반 추론).
- 생성된 공식 방에 Admin 계정 자신은 일반 참여자로 포함되지 않는다 (명세 "관리자도 일반 참여자" 조항은 DORM_X 유저에 해당 — Admin의 dormType이 DORM_X라면 포함될 수 있음).

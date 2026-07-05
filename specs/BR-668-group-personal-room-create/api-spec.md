# 단체·개인 채팅방 생성 API 명세서 (BR-668)

> Base URL: `http://localhost:8080`
> 인증: 모든 엔드포인트에 Bearer Token 필요 (Spring Security)

---

## 1. 단체 채팅방 생성 (기존 수정)

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/open-chat-rooms` |
| **인증** | Bearer Token 필요 |
| **설명** | 새 오픈 채팅방을 생성하고 생성자를 방장으로 등록한다. `isPublic`, `password` 필드 추가. |

### Request

#### Request Body
Content-Type: `application/json`

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `name` | `String` | ✅ | max=30 | 채팅방 이름 |
| `description` | `String` | ❌ | max=100 | 채팅방 설명 |
| `scope` | `String (enum)` | ✅ | `ALL` \| `DORMITORY` | 공개 범위 |
| `maxParticipants` | `Integer` | ✅ | min=2, max=100 | 최대 인원 수 |
| `isPublic` | `Boolean` | ❌ | — | 전체 목록 노출 여부. null 전달 시 `true` 적용 |
| `password` | `String` | ❌ | max=50 | 입장 비밀번호. null 또는 빈 문자열이면 비밀번호 없음 |

```json
{
  "name": "게임 같이 해요",
  "description": "롤 같이 할 사람 모여요",
  "scope": "ALL",
  "maxParticipants": 20,
  "isPublic": false,
  "password": "1234"
}
```

### Response

#### 성공 응답 — `201 Created`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 생성된 채팅방 ID |

```json
{
  "roomId": 42
}
```

#### 에러 응답

| HTTP | code | name | 발생 조건 |
|------|------|------|-----------|
| `400` | `5001` | `VALIDATION_FAILED` | 필수 필드 누락, name/description/password 길이 초과, maxParticipants 범위 위반 |
| `400` | `5001` | `VALIDATION_FAILED` | scope 값이 `ALL`/`DORMITORY` 외의 값 |
| `401` | — | — | 인증 토큰 없음 또는 만료 |
| `404` | `10001` | `USER_NOT_FOUND` | scope=DORMITORY인데 생성자 계정이 없는 경우 |

```json
{
  "code": 5001,
  "name": "VALIDATION_FAILED",
  "message": "DTO에서 요청한 값이 올바르지 않습니다.",
  "errors": ["name: must not be blank"]
}
```

---

## 2. 개인 채팅방 생성 (신규)

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/open-chat-rooms/personal` |
| **인증** | Bearer Token 필요 |
| **설명** | 최대 2명, 항상 비공개인 개인 채팅방을 생성한다. 생성 즉시 생성자(host)와 `targetUserId` 사용자(일반 참여자)가 등록된다. `maxParticipants=2`, `isPublic=false`는 서버 고정이며 클라이언트가 전달하지 않는다. |

### Request

#### Request Body
Content-Type: `application/json`

| 필드 | 타입 | 필수 | 제약 | 설명 |
|------|------|------|------|------|
| `name` | `String` | ✅ | max=30 | 채팅방 이름 |
| `targetUserId` | `Long` | ✅ | — | 함께 채팅할 상대방 사용자 ID |
| `password` | `String` | ❌ | max=50 | 입장 비밀번호. null 또는 빈 문자열이면 비밀번호 없음 |

```json
{
  "name": "우리끼리 방",
  "targetUserId": 7,
  "password": "secret"
}
```

### Response

#### 성공 응답 — `201 Created`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 생성된 채팅방 ID |

```json
{
  "roomId": 99
}
```

#### 에러 응답

| HTTP | code | name | 발생 조건 |
|------|------|------|-----------|
| `400` | `5001` | `VALIDATION_FAILED` | name이 blank이거나 length 초과, password length 초과, targetUserId 누락 |
| `400` | `22019` | `OPEN_CHAT_SELF_PERSONAL_FORBIDDEN` | targetUserId가 생성자 본인 ID인 경우 |
| `401` | — | — | 인증 토큰 없음 또는 만료 |
| `404` | `10001` | `USER_NOT_FOUND` | targetUserId에 해당하는 사용자가 존재하지 않는 경우 |

---

## 3. 채팅방 입장 (PERSONAL 분기 추가)

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/open-chat-rooms/{roomId}/participants/me` |
| **인증** | Bearer Token 필요 |
| **설명** | 지정한 채팅방에 입장한다. `PERSONAL` 타입 방은 생성 시 이미 정원(2명)이 차므로 추가 입장이 불가하다. 이미 참여 중인 경우 입장 처리 없이 상세 정보를 반환한다. |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `roomId` | `Long` | ✅ | 입장할 채팅방 ID |

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `password` | `String` | ❌ | DERIVED 방 비밀번호. 비밀번호가 설정된 방이면 필수 |

### Response

#### 성공 응답 — `200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 채팅방 ID |
| `name` | `String` | 채팅방 이름 |
| `description` | `String` | 채팅방 설명 |
| `scope` | `String (enum)` | 공개 범위 (`ALL` \| `DORMITORY`) |
| `currentParticipants` | `Integer` | 현재 참여 인원 |
| `maxParticipants` | `Integer` | 최대 참여 인원 |
| `isOfficial` | `Boolean` | 공식 채팅방 여부 |
| `createdAt` | `String (ISO 8601)` | 채팅방 생성 시각 |

```json
{
  "roomId": 99,
  "name": "우리끼리 방",
  "description": null,
  "scope": "ALL",
  "currentParticipants": 1,
  "maxParticipants": 2,
  "isOfficial": false,
  "createdAt": "2026-07-05T14:30:00"
}
```

#### 에러 응답

| HTTP | code | name | 발생 조건 |
|------|------|------|-----------|
| `400` | `22003` | `OPEN_CHAT_ROOM_FULL` | 정원 초과 (PERSONAL: 2명, 기타: maxParticipants) |
| `401` | — | — | 인증 토큰 없음 또는 만료 |
| `403` | `22002` | `OPEN_CHAT_ROOM_FORBIDDEN` | PERSONAL 방 비밀번호 불일치 / DORMITORY 방 기숙사 불일치 |
| `404` | `22001` | `OPEN_CHAT_ROOM_NOT_FOUND` | 존재하지 않는 roomId |
| `404` | `10001` | `USER_NOT_FOUND` | 입장자 계정이 없는 경우 |

```json
{
  "code": 22002,
  "name": "OPEN_CHAT_ROOM_FORBIDDEN",
  "message": "[OpenChat] 채팅방 접근 권한이 없습니다."
}
```

---

## 추론 항목

> 코드에서 명시적으로 확인되지 않아 관례 및 패턴으로 추론한 항목입니다.

- `password` 빈 문자열(`""`) 처리: `createPersonal()` 팩토리 메서드 설계 기준으로 null로 정규화된다고 추론 (구현 시 확인 필요)
- `401` 응답 형식: Spring Security가 직접 처리하며 GlobalExceptionHandler를 거치지 않아 응답 형식이 다를 수 있음
- `OPEN_CHAT_ROOM_FULL` 상태 코드: ErrorCode 정의상 `BAD_REQUEST(400)` — HTTP 409 아님

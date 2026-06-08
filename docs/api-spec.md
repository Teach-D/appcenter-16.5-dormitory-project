# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`
> 기능: 파생 톡방 (비공개 그룹 채팅)

---

## 1. 공통 정보

### Base URL
`/open-chat-rooms`, `/open-chat-invitations`

### 인증 방식
- 헤더: `Authorization: Bearer {accessToken}`
- 미인증 시: `401 UNAUTHORIZED`

### 공통 응답 형식

**성공:**
```json
{
  "success": true,
  "data": {},
  "message": null
}
```

**실패:**
```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

### 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| UNAUTHORIZED | 401 | 인증 토큰 없음 또는 만료 |
| OPEN_CHAT_ROOM_FORBIDDEN | 403 | 해당 방의 참여자가 아님 |
| OPEN_CHAT_ROOM_NOT_FOUND | 404 | 채팅방 없음 |
| OPEN_CHAT_INVITATION_NOT_FOUND | 404 | 초대 없음 |
| OPEN_CHAT_ROOM_FULL | 400 | 정원 초과 |
| OPEN_CHAT_INVITATION_INVALID_TARGET | 400 | 초대 대상이 부모 방 참여자 아님 |
| OPEN_CHAT_INVITATION_ALREADY_EXISTS | 409 | 이미 PENDING 초대 존재 |
| OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS | 409 | 이미 해당 방 참여자 |
| VALIDATION_ERROR | 400 | 요청값 검증 실패 |

---

## 2. API 목록

---

### [1] 파생 톡방 생성

**`POST /open-chat-rooms/derived`**

| 항목 | 내용 |
|------|------|
| 설명 | 특정 오픈채팅방(OPEN) 참여자가 해당 방 기반 비공개 파생 톡방을 생성한다 |
| 인증 | 필요 |
| 권한 | USER, DORMITORY |
| 멱등성 | 비멱등 |

**Request Body:**
```json
{
  "parentRoomId": "Long | 파생의 기반이 될 오픈채팅방 ID | 필수",
  "name": "String | 파생 톡방 이름 | 필수",
  "description": "String | 설명 | 선택",
  "maxParticipants": "Integer | 최대 참여 인원 | 필수"
}
```

**Validation Rules:**
- `parentRoomId`: null 불가
- `name`: 1~30자, 공백만으로 구성 불가
- `maxParticipants`: 2 이상, TBD 상한값 이하

**Response (성공 — 201 CREATED):**
```json
{
  "success": true,
  "data": {
    "roomId": "Long | 생성된 파생 톡방 ID"
  }
}
```

**비즈니스 로직 요약:**
1. 요청자가 `parentRoomId` 방의 참여자인지 검증 (BR-01)
2. `parentRoomId` 방의 `roomType`이 `OPEN`인지 검증 (재파생 방지, 아키텍처 위험 2)
3. `roomType = DERIVED`, `parentRoomId` 세팅하여 방 생성
4. 생성자를 첫 번째 `OpenChatParticipant`로 즉시 등록 (hostUserId = 생성자)
5. 201 CREATED + 생성된 roomId 반환

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| parentRoomId 방에 참여하지 않은 사용자 | 403 | OPEN_CHAT_ROOM_FORBIDDEN | 해당 오픈채팅방의 참여자만 파생 톡방을 만들 수 있습니다 |
| parentRoomId 방이 존재하지 않음 | 404 | OPEN_CHAT_ROOM_NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| parentRoomId 방이 DERIVED 타입 | 400 | VALIDATION_ERROR | 파생 톡방에서 재파생할 수 없습니다 |
| name 길이 초과 또는 공백 | 400 | VALIDATION_ERROR | 방 이름은 1~30자여야 합니다 |
| maxParticipants 범위 위반 | 400 | VALIDATION_ERROR | 참여 인원은 2명 이상이어야 합니다 |

**동시성 & 멱등성:**
- 동일 사용자가 같은 parentRoomId로 여러 번 요청하면 각각 별개의 방이 생성됨 (허용)

**사이드 이펙트 / 도메인 이벤트:**
- `DerivedRoomCreated` 이벤트 발행 (현재 구독자 없음)

---

### [2] 초대 발송

**`POST /open-chat-rooms/{roomId}/invitations`**

| 항목 | 내용 |
|------|------|
| 설명 | 파생 톡방 참여자가 부모 방의 다른 참여자에게 입장 초대를 발송한다 |
| 인증 | 필요 |
| 권한 | USER, DORMITORY |
| 멱등성 | 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 초대를 발송할 파생 톡방 ID |

**Request Body:**
```json
{
  "inviteeUserId": "Long | 초대할 사용자 ID | 필수"
}
```

**Validation Rules:**
- `inviteeUserId`: null 불가, 자기 자신 초대 불가

**Response (성공 — 201 CREATED):**
```json
{
  "success": true,
  "data": {
    "invitationId": "Long | 생성된 초대 ID"
  }
}
```

**비즈니스 로직 요약:**
1. `roomId` 방이 존재하며 `roomType = DERIVED`인지 확인
2. 요청자(inviter)가 해당 파생 톡방의 참여자인지 검증 (BR-01 파생)
3. invitee가 해당 파생 톡방의 `parentRoomId` 방의 참여자인지 검증 (BR-02)
4. invitee가 이미 파생 톡방 참여자인지 확인 (BR-04)
5. 동일 (roomId, inviteeUserId) 쌍으로 PENDING 초대가 존재하는지 확인 (BR-03, INV-05)
6. `OpenChatInvitation` 생성 (status = PENDING)
7. 201 CREATED + invitationId 반환

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| roomId 방이 존재하지 않음 | 404 | OPEN_CHAT_ROOM_NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 요청자가 해당 방 참여자 아님 | 403 | OPEN_CHAT_ROOM_FORBIDDEN | 해당 채팅방의 참여자만 초대할 수 있습니다 |
| invitee가 부모 방 참여자 아님 | 400 | OPEN_CHAT_INVITATION_INVALID_TARGET | 초대 대상이 오픈채팅방 참여자가 아닙니다 |
| invitee가 이미 파생 톡방 참여자 | 409 | OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS | 이미 채팅방에 참여 중인 사용자입니다 |
| invitee에게 이미 PENDING 초대 존재 | 409 | OPEN_CHAT_INVITATION_ALREADY_EXISTS | 이미 초대를 보낸 사용자입니다 |
| 자기 자신 초대 | 400 | VALIDATION_ERROR | 자기 자신을 초대할 수 없습니다 |

**동시성 & 멱등성:**
- PENDING 중복 체크는 DB UNIQUE 제약 없이 애플리케이션 레벨 선검증으로 처리
- 동시 발송 시 둘 다 통과할 수 있으나 실제 UX상 빈도 낮음 (TBD: DB UNIQUE 추가 여부)

---

### [3] 초대 수락

**`POST /open-chat-rooms/{roomId}/invitations/{invitationId}/accept`**

| 항목 | 내용 |
|------|------|
| 설명 | 피초대자가 초대를 수락하여 파생 톡방의 참여자로 등록된다 |
| 인증 | 필요 |
| 권한 | USER, DORMITORY |
| 멱등성 | 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 파생 톡방 ID |
| invitationId | Long | 수락할 초대 ID |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "roomId": "Long | 입장된 파생 톡방 ID",
    "roomName": "String | 방 이름",
    "currentParticipants": "Integer | 현재 참여 인원",
    "maxParticipants": "Integer | 최대 참여 인원"
  }
}
```

**비즈니스 로직 요약:**
1. invitationId로 `OpenChatInvitation` 조회
2. 초대의 `inviteeUserId`가 요청자 본인인지 검증 (BR-07, INV-07)
3. 초대 `status`가 `PENDING`인지 확인 (INV-06 — ACCEPTED/REJECTED면 400)
4. `OpenChatRoom`을 비관적 락으로 조회 (BR-10 정원 초과 방지)
5. 현재 참여 인원이 `maxParticipants` 미만인지 확인
6. invitee가 이미 참여자인지 중복 체크 (INV-04)
7. `OpenChatInvitation.status` → `ACCEPTED`
8. `OpenChatParticipant` 생성 및 저장
9. 방 입장 상세 정보 반환

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| invitationId 초대가 존재하지 않음 | 404 | OPEN_CHAT_INVITATION_NOT_FOUND | 초대를 찾을 수 없습니다 |
| 본인에게 온 초대가 아님 | 403 | OPEN_CHAT_ROOM_FORBIDDEN | 본인의 초대만 수락할 수 있습니다 |
| 이미 수락/거절된 초대 | 400 | VALIDATION_ERROR | 이미 처리된 초대입니다 |
| 정원 초과 | 400 | OPEN_CHAT_ROOM_FULL | 채팅방 정원이 가득 찼습니다 |
| 이미 파생 톡방 참여자 | 409 | OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS | 이미 채팅방에 참여 중입니다 |

**동시성 & 멱등성:**
- `OpenChatRoom` 비관적 락으로 동시 수락 시 정원 초과 방지 보장

**사이드 이펙트 / 도메인 이벤트:**
- `InvitationAccepted` 이벤트 발행 (현재 구독자 없음)
- 시스템 메시지 전송: `"{닉네임}님이 입장했습니다."` (기존 sendSystemMessage 재사용 — TBD)

---

### [4] 초대 거절

**`POST /open-chat-rooms/{roomId}/invitations/{invitationId}/reject`**

| 항목 | 내용 |
|------|------|
| 설명 | 피초대자가 초대를 거절한다. 이후 재초대 가능하다 |
| 인증 | 필요 |
| 권한 | USER, DORMITORY |
| 멱등성 | 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 파생 톡방 ID |
| invitationId | Long | 거절할 초대 ID |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**비즈니스 로직 요약:**
1. invitationId로 `OpenChatInvitation` 조회
2. 초대의 `inviteeUserId`가 요청자 본인인지 검증 (BR-07, INV-07)
3. 초대 `status`가 `PENDING`인지 확인 (INV-06)
4. `OpenChatInvitation.status` → `REJECTED`
5. 200 OK 반환 (재초대 허용 — BR-06)

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| invitationId 초대가 존재하지 않음 | 404 | OPEN_CHAT_INVITATION_NOT_FOUND | 초대를 찾을 수 없습니다 |
| 본인에게 온 초대가 아님 | 403 | OPEN_CHAT_ROOM_FORBIDDEN | 본인의 초대만 거절할 수 있습니다 |
| 이미 수락/거절된 초대 | 400 | VALIDATION_ERROR | 이미 처리된 초대입니다 |

**동시성 & 멱등성:**
- 동시 거절 요청 시 두 번째 요청은 PENDING 아님 → 400 반환 (자연스럽게 멱등 처리)

---

### [5] 오픈채팅방 참여자 목록 조회

**`GET /open-chat-rooms/{roomId}/participants`**

| 항목 | 내용 |
|------|------|
| 설명 | 해당 방(OPEN 또는 DERIVED)의 참여자 목록을 조회한다. 참여자 본인만 조회 가능하다 |
| 인증 | 필요 |
| 권한 | USER, DORMITORY |
| 멱등성 | 멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| roomId | Long | 채팅방 ID (OPEN 또는 DERIVED 모두 허용) |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "roomId": "Long | 채팅방 ID",
    "participants": [
      {
        "userId": "Long | 사용자 ID",
        "nickname": "String | 닉네임",
        "joinedAt": "String (ISO 8601) | 입장 시각",
        "isHost": "Boolean | 호스트 여부"
      }
    ],
    "totalCount": "Integer | 전체 참여자 수"
  }
}
```

**비즈니스 로직 요약:**
1. `roomId` 방이 존재하는지 확인
2. 요청자가 해당 방의 참여자인지 검증 (BR-09)
3. `OpenChatParticipant` 목록 조회 (joinedAt 오름차순)
4. 각 참여자의 userId로 User 정보(nickname) 조인
5. hostUserId와 비교하여 `isHost` 필드 세팅
6. 참여자 목록 반환

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| roomId 방이 존재하지 않음 | 404 | OPEN_CHAT_ROOM_NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 요청자가 해당 방 참여자 아님 | 403 | OPEN_CHAT_ROOM_FORBIDDEN | 참여자만 목록을 조회할 수 있습니다 |

**동시성 & 멱등성:**
- 읽기 전용, 동시성 이슈 없음

**엣지 케이스:**
- [ ] 참여자가 1명(호스트만)인 경우: 정상 반환, isHost=true
- [ ] OPEN 방에서도 이 API 호출 가능 — 참여자이면 허용

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|------------|-----------|-----------|
| 파생 톡방 생성 | `DerivedRoomCreated` | 없음 (확장용) | — |
| 초대 수락 | `InvitationAccepted` | 없음 (확장용) | — |

---

## 4. API 간 의존 관계

- `[2] 초대 발송` 호출 전 `[1] 파생 톡방 생성` 선행 필요 → 초대 대상 방이 존재해야 함
- `[3] 초대 수락` / `[4] 초대 거절` 호출 전 `[2] 초대 발송` 선행 필요 → invitationId 생성 선행
- `[5] 참여자 목록 조회`는 `[1]` 또는 `[3]` 이후 의미 있는 결과 반환

---

## 5. 보안 체크리스트

- [x] 모든 쓰기 API에 인증 적용
- [x] 초대 수락/거절 시 inviteeUserId == 요청자 본인 검증 (타인 리소스 접근 차단)
- [x] 참여자 목록 조회 시 방 참여자 여부 검증 (비참여자 차단)
- [x] 파생 톡방 생성 시 parentRoomId 방 참여 여부 검증
- [ ] 요청 크기 제한: name, description 필드 길이 Validation으로 대응
- [ ] Rate Limit: 초대 발송 API — 동일 사용자 단시간 대량 초대 제한 (TBD)

---

## 6. 최종 검토

- [x] ambiguous endpoint 없음 (파생 톡방 생성과 일반 방 생성 URL 분리)
- [x] 동시성 위험 식별 완료 — 초대 수락 시 비관적 락 명시
- [x] 누락된 인가 검사 없음 — 모든 엔드포인트 참여자 검증 포함
- [x] 최종 일관성 없음 — 모든 처리 단일 트랜잭션
- [x] 기존 `/open-chat-rooms` CRUD API와 URL 충돌 없음 (`/derived` 하위 경로 분리)

---

## 7. TBD

- [ ] `maxParticipants` 상한값 결정 (예: 50명)
- [ ] 초대 발송 PENDING 중복 체크에 DB UNIQUE 제약 추가 여부 (동시 발송 엣지케이스)
- [ ] 초대 수락 후 입장 시스템 메시지 전송 여부 (`sendSystemMessage` 재사용)
- [ ] 초대 목록 조회 API (나에게 온 초대: `GET /open-chat-invitations?type=received`) 추가 여부
- [ ] 초대 발송 Rate Limit 정책
- [ ] 참여자 목록 페이지네이션 필요 여부 (현재 전체 반환)

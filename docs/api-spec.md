# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`
> 기능: 오픈채팅 다중 방장 시스템

---

## 1. 공통 정보

### Base URL
`/open-chat-rooms`

### 인증 방식
- 헤더: `Authorization: Bearer {accessToken}`
- 미인증 시: `401 UNAUTHORIZED`

### 신규 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `OPEN_CHAT_ALREADY_HOST` | 400 | 대상이 이미 방장입니다 |
| `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` | 400 | 단독 방장은 방 삭제 또는 방장 위임 후 나갈 수 있습니다 |

> 기존 에러 코드 유지: `OPEN_CHAT_ROOM_NOT_FOUND(22001)`, `OPEN_CHAT_ROOM_FORBIDDEN(22002)`, `OPEN_CHAT_PARTICIPANT_NOT_FOUND(22004)`

---

## 2. API 목록

---

### [NEW] 방장 부여

**`POST /open-chat-rooms/{roomId}/hosts/{targetUserId}`**

| 항목 | 내용 |
|------|------|
| 설명 | 방장이 다른 참여자에게 방장 권한을 부여한다 |
| 인증 | 필요 |
| 권한 | `USER` (해당 방 방장), `ADMIN` (참여 여부 무관) |
| 멱등성 | 비멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 채팅방 ID |
| `targetUserId` | Long | 방장 권한을 부여받을 참여자의 userId |

**Request Body:** 없음

**Response (성공): `204 No Content`**

**비즈니스 로직 요약:**
1. 요청자가 `ADMIN` role이거나 해당 방의 `OpenChatParticipant.isHost = true`인지 확인 (BR-03)
2. `targetUserId`가 해당 방의 참여자인지 확인 (INV-02)
3. 대상이 이미 방장(`isHost = true`)인지 확인 (INV-03)
4. 대상의 `OpenChatParticipant.isHost = true` 저장
5. `OpenChatHostGranted` 이벤트 발행 → 대상 유저에게 FCM 알림 (BR-11)

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 요청자가 방장도 ADMIN도 아닌 경우 | 403 | `OPEN_CHAT_ROOM_FORBIDDEN` | 채팅방 접근 권한이 없습니다 |
| roomId에 해당하는 방이 없음 | 404 | `OPEN_CHAT_ROOM_NOT_FOUND` | 채팅방을 찾을 수 없습니다 |
| targetUserId가 해당 방 참여자가 아님 | 404 | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 참여하지 않은 채팅방입니다 |
| targetUserId가 이미 방장인 경우 | 400 | `OPEN_CHAT_ALREADY_HOST` | 이미 방장인 사용자입니다 |
| targetUserId == 요청자 본인 | 400 | `OPEN_CHAT_ALREADY_HOST` | 이미 방장인 사용자입니다 |

**사이드 이펙트 / 도메인 이벤트:**
- `OpenChatHostGranted`: 방장 부여 완료 후 발행 → FCM 서비스가 구독, 대상 유저에게 알림 발송
- FCM 발송 실패 시 방장 부여는 롤백하지 않음 (BR-11)

**엣지 케이스:**
- [ ] 방장 부여 직후 대상이 나가는 경우: 나감과 동시에 `isHost` 소멸 (별도 처리 불필요)

---

### [CHANGED] 채팅방 나가기

**`DELETE /open-chat-rooms/{roomId}/participants/me`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방에서 나간다. 단독 방장은 반드시 `newHostUserId`를 지정해야 한다 |
| 인증 | 필요 |
| 권한 | `USER`, `ADMIN`, `DORMITORY` (참여자만) |
| 멱등성 | 비멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 채팅방 ID |

**Query Parameters:**

| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `newHostUserId` | Long | 조건부 필수 | 단독 방장인 경우 필수. 위임할 참여자의 userId |

**Request Body:** 없음

**Response (성공): `200 OK`**

```json
{
  "roomDeleted": false
}
```

> `roomDeleted`는 항상 `false`. 단독 방장 나가기는 반드시 위임이 필요하므로 나가기 후 방이 삭제되는 경우는 없음 (방 삭제는 별도 DELETE /open-chat-rooms/{roomId} 사용)

**비즈니스 로직 요약:**

**[케이스 A] 일반 참여자 또는 복수 방장 중 1명이 나가는 경우** (`newHostUserId` 미제공)
1. 요청자의 `OpenChatParticipant` 존재 확인
2. 요청자가 단독 방장인지 확인 (`isHost = true` count == 1 AND 요청자가 방장)
3. 단독 방장이면 400 에러 반환 (BR-06)
4. 단독 방장이 아니면 요청자의 `OpenChatParticipant` row 삭제
5. 퇴장 시스템 메시지 전송 (BR-08)

**[케이스 B] 단독 방장이 위임+나가기를 하는 경우** (`newHostUserId` 제공)
1. 해당 방 participant rows에 **비관적 락** 적용 (ADR-02)
2. 요청자가 단독 방장인지 재확인 (락 획득 후 재검증)
3. `newHostUserId`가 해당 방 참여자인지 확인
4. `newHostUserId == 요청자 본인`이면 400 에러
5. 위임 대상의 `OpenChatParticipant.isHost = true` 저장
6. 위임 대상에게 FCM 알림 (`OpenChatHostGranted` 이벤트)
7. 요청자의 `OpenChatParticipant` row 삭제
8. 퇴장 시스템 메시지 전송

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 요청자가 해당 방 참여자가 아님 | 404 | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 참여하지 않은 채팅방입니다 |
| 단독 방장이 `newHostUserId` 없이 나가기 시도 | 400 | `OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` | 단독 방장은 방 삭제 또는 방장 위임 후 나갈 수 있습니다 |
| `newHostUserId`가 해당 방 참여자가 아님 | 404 | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` | 참여하지 않은 채팅방입니다 |
| `newHostUserId == 요청자 본인` | 400 | `OPEN_CHAT_ALREADY_HOST` | 이미 방장인 사용자입니다 |

**동시성 & 멱등성:**
- 케이스 B(위임+나가기): 비관적 락(`SELECT FOR UPDATE`) 적용으로 동시 방장 부여와의 충돌 방지

**사이드 이펙트 / 도메인 이벤트:**
- `OpenChatHostGranted` (케이스 B만): 위임 대상 유저에게 FCM 알림

**엣지 케이스:**
- [ ] 재입장 시 방장 권한 미복원 — 새 `OpenChatParticipant` row는 `isHost = false`로 생성 (BR-08)

---

### [CHANGED] 채팅방 삭제

**`DELETE /open-chat-rooms/{roomId}`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방을 삭제한다. 모든 방장이 삭제 권한을 가진다 |
| 인증 | 필요 |
| 권한 | `USER` (해당 방 방장), `ADMIN` (참여 여부 무관) |
| 멱등성 | 비멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 채팅방 ID |

**Request Body:** 없음

**Response (성공): `204 No Content`**

**비즈니스 로직 요약:**
1. 요청자가 `ADMIN` role이거나 해당 방의 `OpenChatParticipant.isHost = true`인지 확인 (BR-07)
2. 공식 방(`isOfficial = true`)이면 `ADMIN`만 삭제 가능 (INV-05, BR-07)
3. 해당 방의 모든 `OpenChatParticipant` row 삭제
4. `OpenChatRoom` row 삭제

**에러 케이스:**

| 상황 | HTTP | code | message |
|------|------|------|---------|
| 요청자가 방장도 ADMIN도 아닌 경우 | 403 | `OPEN_CHAT_ROOM_FORBIDDEN` | 채팅방 접근 권한이 없습니다 |
| 공식 방을 비 ADMIN이 삭제 시도 | 403 | `OPEN_CHAT_ROOM_FORBIDDEN` | 채팅방 접근 권한이 없습니다 |
| roomId에 해당하는 방이 없음 | 404 | `OPEN_CHAT_ROOM_NOT_FOUND` | 채팅방을 찾을 수 없습니다 |

**변경 내용 요약 (기존 대비):**
- 기존: `hostUserId == 요청자` 단일 비교
- 변경: `OpenChatParticipant.isHost = true AND userId == 요청자` OR `role == ADMIN`

---

### [EXISTING - 응답 변경] 참여자 목록 조회

**`GET /open-chat-rooms/{roomId}/participants`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방 참여자 목록 조회 (방장 여부 포함) |
| 인증 | 필요 |
| 권한 | `USER`, `ADMIN`, `DORMITORY` (참여자만) |
| 멱등성 | 멱등 |

**Path Parameters:**

| 이름 | 타입 | 설명 |
|------|------|------|
| `roomId` | Long | 채팅방 ID |

**Response (성공): `200 OK`**

```json
{
  "participants": [
    {
      "userId": "Long | 참여자 userId",
      "nickname": "String | 닉네임",
      "joinedAt": "String | ISO 8601 입장 시각",
      "isHost": "Boolean | 방장 여부"
    }
  ],
  "hostCount": "int | 방장 수"
}
```

> `isHost` 필드: `OpenChatParticipant.isHost` 값을 그대로 반영. 기존 `ResponseOpenChatParticipantDto`에 이미 `isHost` 필드 존재하나, `OpenChatParticipant` 엔티티에 `isHost` 컬럼 추가 전까지는 항상 `false` 반환됨 → 엔티티 컬럼 추가 후 정상 반영

**변경 내용 요약 (기존 대비):**
- `isHost` 필드 응답에 올바른 값 반영 (`OpenChatParticipant.isHost` 기반)
- `hostCount` 필드 신규 추가 (클라이언트가 단독 방장 여부 판단에 사용)

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|------------|-----------|-----------|
| POST /hosts/{targetUserId} | `OpenChatHostGranted` | OpenChatNotificationService | 대상 유저에게 FCM 알림 |
| DELETE /participants/me (케이스 B) | `OpenChatHostGranted` | OpenChatNotificationService | 위임 대상 유저에게 FCM 알림 |

---

## 4. API 간 의존 관계

- `POST /hosts/{targetUserId}` 호출 전 `POST /{roomId}/participants/me` (입장) 선행 필요 (요청자가 참여자이자 방장이어야 함)
- 단독 방장이 `DELETE /participants/me` 호출 시 `newHostUserId`를 알기 위해 `GET /participants` 선행 조회 필요 (클라이언트 책임)

---

## 5. DB 마이그레이션 체크리스트

> API 스펙과 연동된 DB 변경사항 (구현 시 Flyway 마이그레이션 필수)

- [ ] `open_chat_participant` 테이블에 `is_host BOOLEAN NOT NULL DEFAULT FALSE` 컬럼 추가
- [ ] 기존 `open_chat_room.host_user_id` 기준으로 `open_chat_participant.is_host = true` 데이터 이관
  ```sql
  UPDATE open_chat_participant p
  JOIN open_chat_room r ON p.room_id = r.id
  SET p.is_host = TRUE
  WHERE p.user_id = r.host_user_id;
  ```
- [ ] `open_chat_room.host_user_id` 컬럼 제거

---

## 6. 보안 체크리스트

- [x] 모든 쓰기 API에 인증 적용
- [x] 방장 부여: 요청자가 해당 방의 방장인지 서비스 레이어에서 검증 (타인 방 방장 부여 차단)
- [x] 나가기: 요청자 본인의 participant만 삭제 (`me` 엔드포인트로 고정)
- [x] 방 삭제: ADMIN 역할 체크 + 방장 `isHost` 체크 이중 검증
- [x] `newHostUserId == 요청자 본인` 자기 위임 차단

---

## 7. TBD

- [ ] 방장 부여 FCM 알림에 사용할 `NotificationType` 확정 (기존 타입 재사용 vs 신규 추가)
- [ ] `GET /participants` 응답에 `hostCount` 필드 추가 여부 확인 (현재 `ResponseOpenChatParticipantListDto` 구조 미확인)
- [ ] ADMIN이 공식 방에 자동 입장(participant row 생성)해야 하는지 여부
- [ ] 방당 최대 방장 수 제한 여부

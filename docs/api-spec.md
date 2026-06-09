# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`
> 기능: 상호 동의 학번 공개

---

## 1. 공통 정보

### Base URL
`/student-id-disclosures`

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

### 도메인 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| UNAUTHORIZED | 401 | 인증 토큰 없음 또는 만료 |
| DISCLOSURE_REQUEST_NOT_FOUND | 404 | 요청 없음 |
| DISCLOSURE_REQUEST_ALREADY_EXISTS | 409 | PENDING 또는 ACCEPTED 요청 이미 존재 |
| DISCLOSURE_REQUEST_FORBIDDEN | 403 | 본인 소유 요청이 아님 |
| DISCLOSURE_CANNOT_REQUEST_SELF | 400 | 자기 자신에게 요청 불가 |
| DISCLOSURE_NOT_IN_SAME_ROOM | 400 | 두 사용자가 같은 방에 없음 |
| DISCLOSURE_INVALID_STATUS | 400 | 현재 상태에서 허용되지 않는 전이 |
| OPEN_CHAT_ROOM_NOT_FOUND | 404 | 채팅방 없음 |
| VALIDATION_ERROR | 400 | 요청값 검증 실패 |

---

## 2. API 목록

---

### [1] 학번 공개 요청 발송

**`POST /student-id-disclosures`**

| 항목 | 내용 |
|------|------|
| 설명 | 채팅방에서 상대방에게 학번 상호 공개를 요청한다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 비멱등 |

**Request Body:**
```json
{
  "roomId": "Long | 요청을 발송할 채팅방 ID | 필수",
  "targetId": "Long | 학번 공개를 요청할 상대방 사용자 ID | 필수"
}
```

**Validation Rules:**
- `roomId`: null 불가
- `targetId`: null 불가, 요청자 본인 ID와 달라야 함

**Response (성공 — 201 CREATED):**
```json
{
  "success": true,
  "data": {
    "requestId": "Long | 생성된 요청 ID"
  }
}
```

**비즈니스 로직 요약:**
1. `targetId == 요청자 ID` 이면 즉시 거부 (BR-02, INV-02)
2. `roomId` 방이 존재하는지 확인
3. 요청자와 대상자 모두 해당 방의 `OpenChatParticipant`인지 확인 (BR-01)
4. 동일 (requesterId, targetId, roomId) 쌍의 PENDING 또는 ACCEPTED 레코드 존재 여부 확인 (BR-03, INV-01)
   - 존재하면 409 CONFLICT
5. 동일 쌍의 REJECTED 또는 CANCELED 레코드가 있으면 먼저 삭제 (ADR-01)
6. `status = PENDING` 인 `StudentIdDisclosureRequest` 저장
7. `DisclosureRequestCreated` 이벤트 → 대상자에게 FCM 알림 발송 (BR-10, `NotificationType.CHAT`)
8. 201 CREATED + requestId 반환

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| targetId == 요청자 본인 | 400 | DISCLOSURE_CANNOT_REQUEST_SELF | 자기 자신에게 요청할 수 없습니다 |
| roomId 방이 존재하지 않음 | 404 | OPEN_CHAT_ROOM_NOT_FOUND | 채팅방을 찾을 수 없습니다 |
| 요청자 또는 대상자가 해당 방 비참여자 | 400 | DISCLOSURE_NOT_IN_SAME_ROOM | 같은 채팅방에 있는 사용자에게만 요청할 수 있습니다 |
| 이미 PENDING/ACCEPTED 요청 존재 | 409 | DISCLOSURE_REQUEST_ALREADY_EXISTS | 이미 진행 중인 요청이 있습니다 |

**동시성 & 멱등성:**
- 동일 쌍의 동시 발송: DB UNIQUE 제약 `(requester_id, target_id, room_id)` 이 최후 방어선
- `DataIntegrityViolationException` → 409 CONFLICT 로 변환

**사이드 이펙트 / 도메인 이벤트:**
- `DisclosureRequestCreated`: 요청 저장 완료 → 대상자 FCM 알림 (`NotificationType.CHAT`)

**엣지 케이스:**
- [ ] PENDING → REJECTED → 즉시 재요청: 기존 REJECTED 레코드 삭제 후 정상 저장 (BR-04)
- [ ] B→A 방향 요청이 이미 PENDING인 상태에서 A→B 요청: (requesterId=A, targetId=B)는 별개 레코드이므로 허용

---

### [2] 학번 공개 요청 취소

**`DELETE /student-id-disclosures/{requestId}`**

| 항목 | 내용 |
|------|------|
| 설명 | 요청자가 본인이 발송한 PENDING 상태의 요청을 취소한다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| requestId | Long | 취소할 요청 ID |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**비즈니스 로직 요약:**
1. `requestId`로 `StudentIdDisclosureRequest` 조회
2. `requesterId == 요청자 본인`인지 검증 (BR-05, INV-03)
3. `status == PENDING`인지 확인 (INV-04)
4. `status → CANCELED`

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 요청 없음 | 404 | DISCLOSURE_REQUEST_NOT_FOUND | 요청을 찾을 수 없습니다 |
| 본인 요청 아님 | 403 | DISCLOSURE_REQUEST_FORBIDDEN | 본인이 발송한 요청만 취소할 수 있습니다 |
| PENDING 아님 (이미 수락/거절됨) | 400 | DISCLOSURE_INVALID_STATUS | 이미 처리된 요청은 취소할 수 없습니다 |

**동시성 & 멱등성:**
- 해당 없음 (단건 상태 전이, 동시 취소 시 두 번째 요청은 PENDING 아님 → 400)

---

### [3] 학번 공개 요청 수락

**`POST /student-id-disclosures/{requestId}/accept`**

| 항목 | 내용 |
|------|------|
| 설명 | 대상자가 수신한 PENDING 요청을 수락하여 양방향 학번 공개 관계를 성립시킨다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| requestId | Long | 수락할 요청 ID |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "requestId": "Long | 요청 ID",
    "requesterStudentNumber": "String | 요청자 학번 (수락 직후 바로 노출)"
  }
}
```

**비즈니스 로직 요약:**
1. `requestId`로 `StudentIdDisclosureRequest` 조회
2. `targetId == 요청자 본인`인지 검증 (BR-06, INV-03)
3. `status == PENDING`인지 확인 (INV-04)
4. `status → ACCEPTED`
5. 요청자의 `studentNumber` 조회 → 응답에 포함
6. `DisclosureRequestAccepted` 이벤트 → 요청자에게 FCM 알림 (BR-11, `NotificationType.CHAT`)

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 요청 없음 | 404 | DISCLOSURE_REQUEST_NOT_FOUND | 요청을 찾을 수 없습니다 |
| 본인에게 온 요청 아님 | 403 | DISCLOSURE_REQUEST_FORBIDDEN | 본인에게 온 요청만 수락할 수 있습니다 |
| PENDING 아님 | 400 | DISCLOSURE_INVALID_STATUS | 이미 처리된 요청입니다 |

**동시성 & 멱등성:**
- 동시 수락: 두 번째 요청은 PENDING 아님 → 400 (자연스럽게 방어)

**사이드 이펙트 / 도메인 이벤트:**
- `DisclosureRequestAccepted`: 수락 완료 → 요청자 FCM 알림 (`NotificationType.CHAT`)

---

### [4] 학번 공개 요청 거절

**`POST /student-id-disclosures/{requestId}/reject`**

| 항목 | 내용 |
|------|------|
| 설명 | 대상자가 수신한 PENDING 요청을 거절한다. 이후 요청자의 재요청이 가능하다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| requestId | Long | 거절할 요청 ID |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": null
}
```

**비즈니스 로직 요약:**
1. `requestId`로 `StudentIdDisclosureRequest` 조회
2. `targetId == 요청자 본인`인지 검증 (BR-06, INV-03)
3. `status == PENDING`인지 확인 (INV-04)
4. `status → REJECTED`
5. `DisclosureRequestRejected` 이벤트 → 요청자에게 FCM 알림 (BR-11, `NotificationType.CHAT`)

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 요청 없음 | 404 | DISCLOSURE_REQUEST_NOT_FOUND | 요청을 찾을 수 없습니다 |
| 본인에게 온 요청 아님 | 403 | DISCLOSURE_REQUEST_FORBIDDEN | 본인에게 온 요청만 거절할 수 있습니다 |
| PENDING 아님 | 400 | DISCLOSURE_INVALID_STATUS | 이미 처리된 요청입니다 |

**사이드 이펙트 / 도메인 이벤트:**
- `DisclosureRequestRejected`: 거절 완료 → 요청자 FCM 알림 (`NotificationType.CHAT`)

---

### [5] 채팅방 내 학번 공개 상태 조회

**`GET /student-id-disclosures/status`**

| 항목 | 내용 |
|------|------|
| 설명 | 특정 채팅방에서 현재 사용자와 대상자 간의 학번 공개 상태를 조회한다. 채팅 UI 렌더링에 사용한다 |
| 인증 | 필요 |
| 권한 | USER |
| 멱등성 | 멱등 |

**Query Parameters:**
| 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|
| roomId | Long | Y | 조회할 채팅방 ID |
| targetId | Long | Y | 상태를 확인할 상대방 사용자 ID |

**Response (성공 — 200 OK):**
```json
{
  "success": true,
  "data": {
    "status": "String | DISCLOSED | PENDING_SENT | PENDING_RECEIVED | NONE",
    "requestId": "Long | null — PENDING_SENT, PENDING_RECEIVED, DISCLOSED일 때 요청 ID",
    "targetStudentNumber": "String | null — status가 DISCLOSED일 때만 반환"
  }
}
```

**status 값 정의:**
| 값 | 의미 |
|----|------|
| `DISCLOSED` | 양방향 공개 완료 (ACCEPTED 레코드 존재) |
| `PENDING_SENT` | 현재 사용자가 요청을 발송했고 대기 중 |
| `PENDING_RECEIVED` | 현재 사용자가 요청을 수신했고 대기 중 |
| `NONE` | 활성 요청 없음 (요청 가능 상태) |

**비즈니스 로직 요약:**
1. (requesterId=나, targetId=상대) 방향의 활성 레코드 조회
2. (requesterId=상대, targetId=나) 방향의 활성 레코드 조회 (아키텍처 위험 2 — 양방향 검색)
3. ACCEPTED가 있으면 `DISCLOSED` + 상대방 `studentNumber` 반환
4. 내가 보낸 PENDING이 있으면 `PENDING_SENT` 반환
5. 내가 받은 PENDING이 있으면 `PENDING_RECEIVED` 반환 (`requestId`는 상대방 요청 ID)
6. 아무것도 없으면 `NONE` 반환

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| roomId, targetId 누락 | 400 | VALIDATION_ERROR | 필수 파라미터가 없습니다 |

**동시성 & 멱등성:**
- 읽기 전용, 동시성 이슈 없음

**엣지 케이스:**
- [ ] 나와 상대방 모두 PENDING 상태(A→B, B→A 각각): `PENDING_SENT` 우선 반환 (또는 둘 다 반환 — TBD)
- [ ] 채팅방에서 퇴장 후 조회 시: ACCEPTED 레코드가 삭제되어 있으므로 `NONE` 반환

---

### [6] 채팅방 퇴장 시 공개 관계 정리 (기존 API 연동)

> 신규 엔드포인트 아님 — 기존 `DELETE /open-chat-rooms/{roomId}/leave` (또는 동등한 퇴장 API) 처리 흐름에 아래 단계를 추가한다.

**추가 처리 흐름 (Controller 레벨):**
1. 기존 `OpenChatRoomService.leave(roomId, userId)` 호출
2. `StudentIdDisclosureRequestService.deleteByRoomAndUser(roomId, userId)` 추가 호출
   - 해당 방에서 `requesterId == userId` OR `targetId == userId` 인 모든 레코드 hard delete (BR-07, ADR-02)

**사이드 이펙트:**
- 상대방 클라이언트는 다음 상태 조회 시 `NONE`을 수신하여 닉네임 모드로 전환

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|------------|-----------|-----------|
| [1] 요청 발송 | `DisclosureRequestCreated` | FcmService | 대상자에게 CHAT 타입 FCM 알림 |
| [3] 수락 | `DisclosureRequestAccepted` | FcmService | 요청자에게 CHAT 타입 FCM 알림 |
| [4] 거절 | `DisclosureRequestRejected` | FcmService | 요청자에게 CHAT 타입 FCM 알림 |

---

## 4. API 간 의존 관계

- `[3] 수락` / `[4] 거절` / `[2] 취소` 호출 전 `[1] 요청 발송` 선행 필요 → requestId 생성 선행
- `[5] 상태 조회`는 항상 독립적으로 호출 가능 (`NONE` 반환)
- `[6] 퇴장 처리`는 `[1]` 이후 ACCEPTED 상태인 경우 공개 관계를 초기화함

---

## 5. 보안 체크리스트

- [x] 모든 쓰기 API에 인증 적용
- [x] 취소: requesterId == 현재 사용자 검증 (타인 요청 취소 차단)
- [x] 수락/거절: targetId == 현재 사용자 검증 (타인 요청 처리 차단)
- [x] 상태 조회: 학번(`studentNumber`)은 ACCEPTED 상태에서만 반환
- [x] 자기 자신 요청 차단 (INV-02)
- [ ] Rate Limit: 요청 발송 API — 동일 사용자 단시간 대량 발송 제한 (TBD)

---

## 6. 최종 검토

- [x] ambiguous endpoint 없음 — accept/reject/cancel을 각각 분리된 행동 URL로 표현
- [x] 동시성 위험 식별 완료 — DB UNIQUE 제약으로 동시 발송 방어, 상태 전이는 단건 원자 처리
- [x] 누락된 인가 검사 없음 — 취소(요청자 검증), 수락/거절(대상자 검증) 명시
- [x] 양방향 검색 위험 명시 — 상태 조회 시 두 방향 모두 검색 (아키텍처 위험 2)
- [x] FCM 수신 동의 미체크 시 알림 생략, 요청 자체는 정상 처리 명시 (BR-10, BR-11)

---

## 7. TBD

- [ ] A→B, B→A 모두 PENDING인 경우 상태 조회 응답 정책 (`PENDING_SENT` 우선 vs. 별도 상태값 추가)
- [ ] 채팅방 삭제 시 연쇄 삭제 방식: DB Cascade vs. 서비스 레이어 순차 삭제
- [ ] FCM 알림 메시지 본문 문구 결정

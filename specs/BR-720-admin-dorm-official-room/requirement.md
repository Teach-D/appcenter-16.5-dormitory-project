# BR-720 — 관리자 기숙사별 공식 오픈채팅방 생성 및 자동 참여 처리

## 기능 요약

ROLE_ADMIN이 각 기숙사(1·2·3기숙사)별 공식 오픈채팅방을 생성하면, 해당 기숙사로 등록된 모든 현재 유저가 일괄 참여 처리된다.
이후 유저가 기숙사 정보를 변경(신규 설정·기숙사 변경)하면, 이전 기숙사 공식 방에서 자동 퇴장하고 새 기숙사 공식 방에 자동 입장한다.

---

## 동작 명세

### A. 관리자 공식 방 생성 (`POST /admin/open-chat-rooms/dorm`)

1. 요청자가 ROLE_ADMIN인지 검증한다.
2. 요청한 `dormType`(DORM_1·DORM_2·DORM_3)의 공식 방이 이미 존재하면 `OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS(409)` 반환.
3. `OpenChatRoom`을 생성한다.
   - `isOfficial = true`
   - `targetDorm = 요청 dormType`  ← 신규 필드
   - `maxParticipants = Integer.MAX_VALUE`
   - `scope = DORMITORY`
   - `roomType = OPEN`
4. 요청한 `dormType`과 일치하는 모든 유저를 조회한다(`User.dormType = 요청 dormType`).
5. 조회된 유저를 `OpenChatParticipant` 리스트로 변환해 `saveAll()`로 벌크 저장한다.
   - `isHost = false` (관리자 계정 포함, 관리자도 일반 참여자)
6. 생성된 `roomId`를 201 Created로 반환한다.

### B. 유저 기숙사 변경 시 방 자동 재배정

`UserService.updateUser()`에서 `dormType`이 실제로 변경될 때만 실행된다.

| 조건 | 처리 |
|------|------|
| 이전 dormType이 DORM_X이고 해당 공식 방에 참여 중 | 해당 방에서 `OpenChatParticipant` 삭제 |
| 이전 dormType이 NONE 또는 null | 퇴장 처리 없음 |
| 새 dormType이 DORM_X이고 해당 공식 방이 존재 | `OpenChatParticipant`를 새로 생성해 참여 처리 |
| 새 dormType이 NONE 또는 null | 입장 처리 없음 |
| 새 dormType의 공식 방이 존재하지 않음 | 입장 처리 없음 (스킵) |
| 이미 해당 방에 참여 중인 경우(재입장 시) | 중복 방지 — 기존 참여 레코드 유지, 새로 삽입하지 않음 |

---

## 도메인 데이터

### OpenChatRoom — 신규 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `targetDorm` | `DormType` (nullable, `@Enumerated(STRING)`) | 이 방이 대표하는 기숙사. 공식 기숙사 방일 때만 값 설정. 일반 방은 null |

### OpenChatRoom — 공식 기숙사 방 특성

- `isOfficial = true`
- `scope = DORMITORY`
- `roomType = OPEN`
- `maxParticipants = Integer.MAX_VALUE`
- 기숙사당 1개 고유 (`targetDorm` 유니크 제약 또는 서비스 레벨 중복 검사)

### OpenChatParticipant

- 기존 엔티티 그대로 사용
- 벌크 생성 시 `isHost = false`, `joinedAt = LocalDateTime.now()`

---

## 비즈니스 규칙 / 제약

1. **생성 권한**: ROLE_ADMIN만 공식 기숙사 방을 생성할 수 있다.
2. **기숙사당 1개 고유**: DORM_1·DORM_2·DORM_3 각각 공식 방은 1개만 존재한다. 중복 생성 시 409.
3. **대상 DormType**: DORM_1·DORM_2·DORM_3만 허용. NONE은 요청 불가(400).
4. **방 삭제 불가**: `isOfficial = true`이므로 기존 삭제 방지 로직이 적용됨(추가 코드 불필요).
5. **방 수정 불가**: `isOfficial = true`이므로 기존 수정 방지 로직이 적용됨(추가 코드 불필요).
6. **유저 자발적 탈퇴 가능**: 기존 퇴장 API 그대로 사용. 탈퇴 후 기숙사 변경이 발생하면 새 방에 재입장, 이전 방 퇴장 처리는 이미 탈퇴한 경우 참여 레코드가 없으므로 스킵.
7. **기숙사 변경 시 강제 재배정**: 탈퇴 이력과 관계없이, 기숙사 변경 시 새 기숙사 공식 방에 강제 입장(기존 참여 중이 아닐 때만 삽입).
8. **공식 방 미존재 시 스킵**: 공식 방이 없는 상태에서 기숙사를 변경해도 자동 입장하지 않는다.
9. **벌크 삽입 성능**: 방 생성 시 유저 전체 참여 처리는 단일 `saveAll()` 호출로 처리(N+1 금지).

---

## 예외 · 경계 상황

| 상황 | 기대 동작 |
|------|-----------|
| 이미 같은 기숙사 공식 방 존재 | 409 `OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS` |
| ROLE_ADMIN 아닌 사용자가 방 생성 시도 | 403 |
| 요청 `dormType`이 NONE | 400 `INVALID_DORM_TYPE` |
| 방 생성 시 해당 기숙사 유저가 0명 | 참여자 없이 방만 생성됨(정상) |
| dormType 변경 없이 updateUser() 호출 | 방 재배정 로직 미실행(불필요한 쿼리 방지) |
| 이전 방에 이미 참여하지 않은 채로 dormType 변경 | 이전 방 퇴장 처리 스킵(참여 레코드 없음) |
| 새 기숙사 공식 방에 이미 참여 중인 채로 dormType 변경(정상 불가하나 방어) | 중복 삽입 없이 기존 레코드 유지 |

---

## 비목표 (Non-goals)

- 공식 기숙사 방 정보 수정(이름·설명 변경) — 별도 브랜치에서 논의
- 자동 입장·퇴장 시 FCM 푸시 알림 발송
- WebSocket STOMP 이벤트를 통한 실시간 참여자 목록 업데이트
- 방 생성 시 초대 메시지·시스템 메시지 전송
- 관리자가 공식 기숙사 방 목록을 조회하는 별도 API
- `DORMITORY_OFFICIAL` 신규 `OpenChatRoomType` 추가 — `isOfficial=true` + `targetDorm NOT NULL`로 식별

---

## 수용 기준 (Acceptance Criteria)

**AC-1** — 공식 방 최초 생성·유저 일괄 참여
```
Given ROLE_ADMIN, DORM_1 유저 3명 존재, DORM_1 공식 방 없음
When  POST /admin/open-chat-rooms/dorm { dormType: "DORM_1", name: "1기숙사 오픈채팅" }
Then  201 Created, roomId 반환
And   OpenChatRoom: isOfficial=true, targetDorm=DORM_1, maxParticipants=Integer.MAX_VALUE, scope=DORMITORY
And   OpenChatParticipant 3개 생성 (DORM_1 유저 전원)
```

**AC-2** — 중복 생성 차단
```
Given ROLE_ADMIN, DORM_1 공식 방 이미 존재
When  POST /admin/open-chat-rooms/dorm { dormType: "DORM_1", ... }
Then  409 OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS
```

**AC-3** — 권한 없는 유저 차단
```
Given ROLE_USER
When  POST /admin/open-chat-rooms/dorm
Then  403
```

**AC-4** — 잘못된 dormType 차단
```
Given ROLE_ADMIN
When  POST /admin/open-chat-rooms/dorm { dormType: "NONE" }
Then  400 INVALID_DORM_TYPE
```

**AC-5** — 기숙사 변경 시 자동 퇴장 + 입장 (두 방 모두 존재)
```
Given DORM_2 공식 방·DORM_1 공식 방 모두 존재
And   유저 A가 DORM_2 공식 방에 참여 중
When  유저 A의 dormType을 DORM_1로 updateUser()
Then  유저 A의 DORM_2 OpenChatParticipant 삭제
And   유저 A의 DORM_1 OpenChatParticipant 신규 생성
```

**AC-6** — 새 기숙사 공식 방 없을 때 퇴장만 처리
```
Given DORM_2 공식 방 존재, DORM_1 공식 방 없음
And   유저 A가 DORM_2 공식 방에 참여 중
When  유저 A의 dormType을 DORM_1로 updateUser()
Then  유저 A의 DORM_2 OpenChatParticipant 삭제
And   DORM_1 입장 처리 없음
```

**AC-7** — NONE → DORM_X 전환 시 입장만 처리
```
Given DORM_1 공식 방 존재, 유저 A의 dormType = NONE
When  유저 A의 dormType을 DORM_1로 updateUser()
Then  유저 A의 DORM_1 OpenChatParticipant 신규 생성
```

**AC-8** — dormType 변경 없으면 방 재배정 없음
```
Given 유저 A의 dormType = DORM_1 (변경 없이 다른 필드만 수정)
When  updateUser() 호출
Then  OpenChatParticipant 변화 없음
```

**AC-9** — 자발 탈퇴 후 기숙사 변경 시 재입장
```
Given DORM_1 공식 방 존재, 유저 A dormType=DORM_1
And   유저 A가 DORM_1 공식 방에서 자발 탈퇴
When  유저 A의 dormType을 DORM_2로 변경 후 다시 DORM_1로 updateUser()
Then  유저 A의 DORM_1 OpenChatParticipant 재생성
```

**AC-10** — 신규 에러 코드
```
OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS(CONFLICT, 22027, "[OpenChat] 해당 기숙사의 공식 오픈채팅방이 이미 존재합니다.")
```

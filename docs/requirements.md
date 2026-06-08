# 요구사항 명세서

> **기능**: 파생 톡방 (비공개 그룹 채팅)

---

## 1. 개요

- **서비스 목적**: 오픈채팅방 참여자끼리 초대 기반 비공개 그룹 채팅방(파생 톡방)을 만들어 소규모 소통 공간을 제공
- **핵심 사용자**: 기숙사 입주 학생(USER)
- **범위**
  - In Scope: 파생 톡방 생성, 초대 발송/수락/거절, 탐색 목록 제외, 참여자 목록 조회
  - Out of Scope: 파생 톡방 내 채팅 기능(기존 WebSocket/STOMP 재사용), 파생 톡방에서 재파생, FCM 초대 알림

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 |
|--------|-----------|
| `OpenChatRoom` | id, name, description, scope, maxParticipants, hostUserId, isOfficial, **roomType**, **parentRoomId**, lastMessage, lastMessageAt |
| `OpenChatParticipant` | id, roomId, userId, notificationEnabled, joinedAt, lastReadMessageId |
| `OpenChatInvitation` | id, roomId, inviterUserId, inviteeUserId, status, createdAt |
| `User` | id, name, dormType, role |

### 엔티티 간 관계

- `OpenChatRoom` 1 ↔ N `OpenChatParticipant`
- `OpenChatRoom` 1 ↔ N `OpenChatInvitation`
- `OpenChatRoom` (OPEN) 1 ↔ N `OpenChatRoom` (DERIVED, parentRoomId FK)
- `User` 1 ↔ N `OpenChatInvitation` (inviter / invitee)

### 상태 다이어그램 — OpenChatInvitation.status

```
PENDING → ACCEPTED (invitee가 수락)
PENDING → REJECTED (invitee가 거절)
ACCEPTED / REJECTED → (종료, 변경 불가)
```

### roomType 구분

```
OPEN   : 기존 오픈채팅방 (탐색 목록에 노출)
DERIVED: 파생 톡방 (탐색 목록에서 제외, 초대로만 입장)
```

---

## 3. 비즈니스 규칙

1. **BR-01** 파생 톡방은 특정 오픈채팅방(OPEN) 참여자만 생성 가능
   - 생성 시 `parentRoomId`를 지정하며, 요청자가 해당 방의 참여자인지 검증
   - 위반 시: 403 FORBIDDEN (OPEN_CHAT_ROOM_FORBIDDEN)

2. **BR-02** 파생 톡방 초대 대상은 `parentRoomId` 오픈채팅방의 참여자만 가능
   - invitee가 parent 방의 참여자인지 검증
   - 위반 시: 400 BAD_REQUEST (OPEN_CHAT_INVITATION_INVALID_TARGET)

3. **BR-03** 동일 대상에게 이미 PENDING 초대가 존재하면 중복 초대 불가
   - 위반 시: 409 CONFLICT (OPEN_CHAT_INVITATION_ALREADY_EXISTS)

4. **BR-04** 이미 해당 파생 톡방 참여자인 사람은 초대 불가
   - 위반 시: 409 CONFLICT (OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS)

5. **BR-05** 초대 수락 시 즉시 해당 파생 톡방의 `OpenChatParticipant`로 등록
   - 수락 후 invitation status = ACCEPTED, 이후 재수락/재거절 불가

6. **BR-06** 초대 거절 시 invitation status = REJECTED, 재초대 가능
   - 거절 후 동일 invitee에게 새 초대 발송 허용 (BR-03은 PENDING 상태만 체크)

7. **BR-07** 초대 수락/거절은 invitee 본인만 가능
   - 위반 시: 403 FORBIDDEN

8. **BR-08** 파생 톡방은 방 탐색 목록(ALL 탭)에서 제외 (`roomType = DERIVED` 필터링)
   - MY 탭(내가 참여한 방 목록)에는 정상 노출

9. **BR-09** 참여자 목록 조회는 해당 방의 참여자만 가능
   - 위반 시: 403 FORBIDDEN (OPEN_CHAT_ROOM_FORBIDDEN)

10. **BR-10** 파생 톡방 인원 제한은 생성 시 지정 (`maxParticipants`), 기본 상한 TBD
    - 정원 초과 상태에서 초대 수락 시: 400 BAD_REQUEST (OPEN_CHAT_ROOM_FULL)

---

## 4. 사용자 & 권한

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` | 파생 톡방 생성(parent 참여자), 초대 발송, 초대 수락/거절(본인), 참여자 목록(참여자만), MY 탭 조회 |
| `DORMITORY` | USER와 동일 |
| 비인증 | 없음 |

---

## 5. 주요 시나리오

### Happy Path — 파생 톡방 생성 & 초대

1. USER A가 오픈채팅방(id=10)에 참여 중이다.
2. A가 parentRoomId=10을 지정해 파생 톡방(roomType=DERIVED)을 생성한다.
3. A가 방 10의 참여자 B, C에게 초대를 발송한다 → invitation status=PENDING.
4. B가 초대를 수락한다 → B가 파생 톡방의 참여자로 등록, status=ACCEPTED.
5. C가 초대를 거절한다 → status=REJECTED, 이후 재초대 가능.
6. A와 B가 파생 톡방에서 채팅한다 (기존 WebSocket 재사용).

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| parent 방 비참여자가 파생 톡방 생성 시도 | 403 FORBIDDEN |
| parent 방 비참여자를 초대 대상으로 지정 | 400 BAD_REQUEST |
| 이미 PENDING 초대가 있는 대상에게 재초대 | 409 CONFLICT |
| 이미 파생 톡방 참여자인 사람 초대 | 409 CONFLICT |
| 정원 초과 상태에서 초대 수락 | 400 BAD_REQUEST (ROOM_FULL) |
| 방 탐색(ALL 탭)에서 파생 톡방 접근 시도 | 목록에 노출되지 않으므로 진입 불가 |
| 비참여자가 참여자 목록 조회 | 403 FORBIDDEN |

---

## 6. 비기능 요구사항

- **성능**: 참여자 목록 조회 200ms 이내 (페이지네이션 필요 여부 TBD)
- **동시성**: 초대 수락 시 정원 초과 방지 — 비관적 락(`findByIdWithLock`) 적용
- **데이터 보존**: 파생 톡방 삭제 시 invitation, participant 연쇄 삭제 (hard delete)
- **외부 연동**: 없음 (FCM 알림 발송은 Out of Scope)

---

## 7. 미결 사항 (TBD)

- [ ] 파생 톡방 `maxParticipants` 기본값 및 상한 (예: 최대 50명)
- [ ] 파생 톡방 host가 나갔을 때 처리 (기존 OPEN 방과 동일 로직 적용 여부)
- [ ] 초대 목록 조회 API 필요 여부 (나에게 온 초대 목록, 내가 보낸 초대 목록)
- [ ] parent 오픈채팅방이 삭제됐을 때 파생 톡방 처리 (유지 vs. 연쇄 삭제)

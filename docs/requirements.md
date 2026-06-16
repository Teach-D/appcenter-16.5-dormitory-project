# 요구사항 명세서

> **기능**: 오픈채팅 다중 방장 시스템

---

## 1. 개요

- **서비스 목적**: 오픈채팅방에 여러 명의 방장을 두어 방 운영 부담을 분산하고, 방장 부재 시에도 방이 안정적으로 유지될 수 있도록 한다
- **핵심 사용자**: 기숙사 입주 학생(USER), 관리자(ADMIN)
- **범위**
  - In Scope: 방장 다중 지정, 방장 권한 부여, 방장 나가기(단독 방장 위임+퇴장 포함), 방 삭제 권한 다중화, 공식 방 ADMIN 자동 방장, FCM 알림
  - Out of Scope: 방장 권한 박탈, 방장 순위/역할 구분, 방장 이력 관리

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 | 변경 |
|--------|-----------|------|
| `OpenChatRoom` | id, name, description, scope, maxParticipants, createdBy, isOfficial, roomType, parentRoomId | `hostUserId` 컬럼 제거 |
| `OpenChatParticipant` | id, roomId, userId, **isHost**, notificationEnabled, joinedAt, lastReadMessageId | `isHost` 컬럼 추가 |

### 엔티티 간 관계

- `OpenChatRoom` 1 ↔ N `OpenChatParticipant`
- 방장 여부는 `OpenChatParticipant.isHost`로 판별 (방 단위, 참여 중일 때만 유효)

### 방장 상태 전이

```
[방 생성] → 생성자의 OpenChatParticipant.isHost = true
[방장 부여] → 대상 참여자의 isHost = true (부여자가 방장인 경우만)
[방장 나가기] → OpenChatParticipant row 삭제 → isHost 소멸
[재입장] → 새 OpenChatParticipant 생성, isHost = false (방장 권한 복원 없음)
[방 삭제] → 방장인 사용자(또는 ADMIN)가 명시적 삭제 → 모든 참여자 row 삭제
```

---

## 3. 비즈니스 규칙

1. **BR-01** 방 생성자는 자동으로 방장이 된다
   - 처리: 방 생성 시 생성자의 `OpenChatParticipant.isHost = true` 저장

2. **BR-02** 하나의 방에 방장이 여러 명 존재할 수 있다
   - 제한 없음 (상한 TBD)

3. **BR-03** 방장만 다른 참여자에게 방장 권한을 부여할 수 있다
   - 위반 시: 403 FORBIDDEN (OPEN_CHAT_ROOM_FORBIDDEN)
   - 대상이 방 참여자가 아닌 경우: 404 NOT_FOUND (OPEN_CHAT_PARTICIPANT_NOT_FOUND)
   - 대상이 이미 방장인 경우: 400 BAD_REQUEST (OPEN_CHAT_ALREADY_HOST)

4. **BR-04** 방장 권한은 박탈할 수 없다 — 부여만 가능

5. **BR-05** 방장이 여러 명일 경우 방장도 자유롭게 나갈 수 있다
   - 나간 시점에 `isHost` 소멸

6. **BR-06** 방장이 혼자인 경우 아래 두 가지 중 하나를 선택해야 한다
   - **방 삭제**: 방 전체를 삭제한다
   - **위임+나가기**: 다른 참여자를 방장으로 지정하고 동시에 자신은 퇴장한다 (단일 요청)
   - 단순 나가기 시도(위임 없이) 시: 400 BAD_REQUEST (OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE)

7. **BR-07** 모든 방장은 방 삭제 권한을 가진다
   - 공식 방(`isOfficial=true`)은 ADMIN만 삭제 가능 (기존 정책 유지)

8. **BR-08** 방장이 나갔다가 재입장하면 일반 참여자로 입장한다 (방장 권한 미복원)

9. **BR-09** 공식 방(`isOfficial=true`)은 ADMIN 계정의 참여자가 자동으로 방장이 된다
   - ADMIN이 공식 방에 입장할 때 `isHost = true` 부여
   - ADMIN이 공식 방을 생성할 때 자동 방장

10. **BR-10** ADMIN은 참여자 여부와 무관하게 모든 방의 방장 부여·방 삭제를 할 수 있다
    - 단, ADMIN이 참여자가 아닌 상태에서 방장 부여는 가능하나 자신이 방장 목록에 추가되지는 않음 (TBD)

11. **BR-11** 방장 권한이 부여된 경우 대상 유저에게 FCM 알림을 발송한다
    - 대상 유저의 알림 수신 설정(`notificationEnabled`)과 무관하게 발송
    - 알림 실패 시 권한 부여는 롤백하지 않음

12. **BR-12** 파생 방(`roomType=DERIVED`)에도 동일한 다중 방장 규칙이 적용된다

---

## 4. 사용자 & 권한

| 역할 | 방장 부여 | 방 삭제 | 나가기 (방장) |
|------|----------|---------|--------------|
| `USER` (방장) | 가능 (참여자 대상) | 가능 (비공식 방) | 다른 방장 있으면 자유, 혼자면 위임 또는 삭제 |
| `USER` (일반) | 불가 | 불가 | 자유 |
| `ADMIN` | 가능 (모든 방) | 가능 (모든 방) | 방장이면 일반 방장 규칙 적용 |
| `DORMITORY` | 방장인 경우만 가능 | 방장인 경우 비공식 방 | 일반 방장 규칙 적용 |

---

## 5. 주요 시나리오

### Happy Path — 방장 부여

1. 방장 A가 `POST /open-chat-rooms/{roomId}/hosts/{targetUserId}` 호출
2. 서버가 호출자의 `isHost` 확인, 대상이 참여자인지 확인
3. 대상의 `OpenChatParticipant.isHost = true` 저장
4. 대상에게 FCM 알림 발송 ("방장 권한이 부여되었습니다")

### Happy Path — 단독 방장 위임+나가기

1. 단독 방장 A가 `DELETE /open-chat-rooms/{roomId}/participants/me?newHostUserId={B}` 호출
2. 서버가 A가 단독 방장인지 확인, B가 참여자인지 확인
3. B의 `OpenChatParticipant.isHost = true` 저장 → B에게 FCM 알림
4. A의 `OpenChatParticipant` row 삭제 → 퇴장 시스템 메시지 전송

### Happy Path — 방장 나가기 (복수 방장)

1. 방장 A가 `DELETE /open-chat-rooms/{roomId}/participants/me` 호출
2. 서버가 다른 방장 존재 확인
3. A의 `OpenChatParticipant` row 삭제 → 퇴장 시스템 메시지

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| 일반 참여자가 방장 부여 시도 | 403 FORBIDDEN |
| 이미 방장인 사용자에게 방장 부여 | 400 BAD_REQUEST (OPEN_CHAT_ALREADY_HOST) |
| 비참여자에게 방장 부여 | 404 NOT_FOUND |
| 단독 방장이 `newHostUserId` 없이 나가기 시도 | 400 BAD_REQUEST (OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE) |
| 단독 방장이 비참여자를 위임 대상으로 지정 | 404 NOT_FOUND |
| 단독 방장이 자기 자신을 위임 대상으로 지정 | 400 BAD_REQUEST (OPEN_CHAT_ALREADY_HOST) |
| 공식 방을 비 ADMIN이 삭제 시도 | 403 FORBIDDEN |
| FCM 알림 발송 실패 | 방장 부여는 유지, 알림만 실패 처리 |

---

## 6. 비기능 요구사항

- **성능**: 방장 부여·나가기 응답 200ms 이내
- **동시성**: 단독 방장 위임 시 race condition 방지 필요 (비관적 락 또는 트랜잭션 직렬화)
- **데이터 보존**: 방 삭제 시 `OpenChatParticipant` 전체 hard delete (기존 정책 유지)
- **외부 연동**: FCM (기존 `OpenChatNotificationService` 재사용)
- **마이그레이션**: 기존 `open_chat_room.host_user_id` 데이터를 `open_chat_participant.is_host`로 이관 후 컬럼 제거

---

## 7. 미결 사항 (TBD)

- [ ] ADMIN이 참여자가 아닌 상태에서 방장 부여 시 ADMIN 자신이 방장 목록에 추가되는지 여부
- [ ] 한 방의 최대 방장 수 제한 여부
- [ ] 방장 부여 FCM 알림 타입 — 기존 `NotificationType` 중 어느 것을 사용할지, 또는 신규 타입 추가 여부

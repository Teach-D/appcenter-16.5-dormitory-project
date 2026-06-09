# 요구사항 명세서

> **기능**: 상호 동의 학번 공개

---

## 1. 개요

- **서비스 목적**: 채팅방 내에서 닉네임으로 대화하는 두 사용자가 상호 동의 하에 학번을 공개하여 신뢰도를 높이는 기능
- **핵심 사용자**: 기숙사 입주 학생(USER)
- **범위**
  - In Scope: 학번 공개 요청 발송/취소, 수락/거절, 채팅방 내 프로필 표시 전환, FCM 알림
  - Out of Scope: 실명 공개, 다른 채팅방으로의 공개 상태 전파, 관리자 개입 기능

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 |
|--------|-----------|
| `StudentIdDisclosureRequest` | id, requesterId, targetId, roomId, status, createdAt, updatedAt |
| `User` | id, nickname, studentId (기존 엔티티, 학번 컬럼 활용) |
| `OpenChatRoom` | id (기존 엔티티, roomId 참조용) |

### 엔티티 간 관계

- `StudentIdDisclosureRequest` N ↔ 1 `User` (requester)
- `StudentIdDisclosureRequest` N ↔ 1 `User` (target)
- `StudentIdDisclosureRequest` N ↔ 1 `OpenChatRoom` (roomId FK)
- 동일 (requesterId, targetId, roomId) 쌍에 PENDING 또는 ACCEPTED 상태의 요청은 최대 1개

### 상태 다이어그램 — StudentIdDisclosureRequest.status

```
[없음]  → PENDING   (요청 발송)
PENDING → ACCEPTED  (대상자 수락)
PENDING → REJECTED  (대상자 거절)
PENDING → CANCELED  (요청자 취소)
ACCEPTED → [삭제]   (요청자 또는 대상자가 채팅방 퇴장 시 레코드 hard delete)
```

---

## 3. 비즈니스 규칙

1. **BR-01** 요청자와 대상자는 동일한 채팅방에 현재 참여 중이어야 함
   - 위반 시: 400 BAD_REQUEST (NOT_IN_SAME_ROOM)

2. **BR-02** 자기 자신에게 요청 발송 불가
   - 위반 시: 400 BAD_REQUEST (CANNOT_REQUEST_SELF)

3. **BR-03** 동일 (requesterId, targetId, roomId) 쌍에 이미 PENDING 또는 ACCEPTED 상태의 요청이 존재하면 새 요청 발송 불가
   - 위반 시: 409 CONFLICT (DISCLOSURE_REQUEST_ALREADY_EXISTS)

4. **BR-04** REJECTED 또는 CANCELED 상태인 경우 재요청 가능 (대기 기간 없음)

5. **BR-05** 요청자 본인만 PENDING 상태의 요청을 CANCELED로 변경 가능
   - 위반 시: 403 FORBIDDEN

6. **BR-06** 대상자 본인만 PENDING 상태의 요청을 ACCEPTED 또는 REJECTED로 변경 가능
   - 위반 시: 403 FORBIDDEN

7. **BR-07** 수락(ACCEPTED) 후 요청자 또는 대상자 중 한 명이라도 해당 채팅방을 퇴장하면 요청 레코드를 hard delete하고 양측 모두 닉네임 표시로 초기화
   - 채팅방 재입장 후 학번 확인이 필요하면 새 요청 필요

8. **BR-08** 학번 공개는 해당 roomId 기준으로만 적용되며 다른 채팅방에는 영향 없음

9. **BR-09** 학번 공개 요청은 오픈채팅방(OPEN)과 파생 톡방(DERIVED) 모두에서 가능

10. **BR-10** 요청 발송 시 대상자에게 FCM 알림 전송 (알림 수신 동의한 경우에만)

11. **BR-11** 수락/거절 시 요청자에게 FCM 알림 전송 (알림 수신 동의한 경우에만)

---

## 4. 사용자 & 권한

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` | 요청 발송, 본인 요청 취소, 수신 요청 수락/거절, 채팅방 내 공개 상태 조회 |
| `DORMITORY` / `ADMIN` | 해당 없음 |
| 비인증 | 없음 |

---

## 5. 주요 시나리오

### Happy Path

1. USER A가 채팅방에서 USER B에게 학번 공개 요청을 발송한다 (status=PENDING)
2. USER B에게 FCM 알림이 전송된다
3. USER B가 요청을 수락한다 (status=ACCEPTED)
4. USER A에게 수락 FCM 알림이 전송된다
5. 해당 채팅방에서 A는 B의 학번을, B는 A의 학번을 프로필에서 확인할 수 있다
6. A가 채팅방을 퇴장하면 요청 레코드가 삭제되고 B 화면에서 A의 닉네임으로 돌아간다

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| PENDING 상태에서 대상자가 채팅방을 먼저 퇴장 | 요청 레코드 삭제 (퇴장 이벤트에서 처리) |
| 이미 ACCEPTED 상태인데 요청 재발송 | 409 CONFLICT (BR-03) |
| REJECTED 직후 즉시 재요청 | 허용 (BR-04) |
| 그룹 채팅방에서 요청 | 가능, 특정 두 사람 간 1:1 관계로 적용 |
| 파생 톡방에서 요청 | 가능 (BR-09) |
| 동일 유저 쌍이 다른 방에서도 요청 | roomId별로 독립 처리 |
| FCM 토큰 없는 사용자에게 알림 | 알림 생략, 요청 자체는 정상 처리 |
| 채팅방 삭제 시 ACCEPTED 요청 존재 | 채팅방 삭제와 함께 연쇄 삭제 |

---

## 6. 비기능 요구사항

- **성능**: 채팅방 메시지/참여자 조회 시 학번 공개 상태 확인은 roomId + 두 userId 조건으로 단건 조회 (인덱스 필요)
- **동시성**: 동일 쌍의 동시 요청 발송 시 중복 삽입 방지 — DB 유니크 제약(requesterId, targetId, roomId) + PENDING/ACCEPTED 상태 조건
- **데이터 보존**: 채팅방 퇴장 또는 방 삭제 시 요청 레코드 hard delete (개인정보 최소화)
- **외부 연동**: FCM 알림 (기존 알림 인프라 활용)

---

## 7. 미결 사항 (TBD)

- [ ] 채팅 메시지에서 학번 표시 시점: 요청 수락 이후 메시지부터만 표시할지, 이전 메시지에도 소급 적용할지
- [ ] 알림 타입(NotificationType) 추가 여부: 기존 열거형에 STUDENT_ID_DISCLOSURE 항목 추가 필요한지 확인
- [ ] 채팅방 조회 API 응답에서 학번 공개 상태를 함께 반환할지, 별도 API로 조회할지

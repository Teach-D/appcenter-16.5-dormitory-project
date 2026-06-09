# 도메인 모델

> 기반 요구사항: `docs/requirements.md`
> 기능: 상호 동의 학번 공개

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| 학번 공개 요청 (Disclosure Request) | 특정 채팅방 안에서 A가 B에게 학번(`studentNumber`)을 서로 공개하자고 보내는 요청 |
| 요청자 (Requester) | 학번 공개 요청을 발송한 사용자 |
| 대상자 (Target) | 학번 공개 요청을 수신한 사용자 |
| 공개 관계 (Disclosed Pair) | 수락 상태(ACCEPTED)인 (requester, target, roomId) 3-tuple — 해당 방 안에서만 유효 |
| 닉네임 모드 | 공개 관계가 없는 상태 — 채팅방에서 상대방 이름 대신 닉네임이 표시됨 |
| 학번 모드 | 공개 관계가 ACCEPTED인 상태 — 채팅방에서 상대방의 `studentNumber`가 표시됨 |

---

## 2. 바운디드 컨텍스트

단일 신규 컨텍스트: `studentIdDisclosure`
- `openChat` 컨텍스트와 협력 (참여 여부 검증, 퇴장 이벤트 연동)
- `User` 컨텍스트 의존: `studentNumber` 필드 조회

---

## 3. 애그리거트

### Aggregate: StudentIdDisclosureRequest

#### 책임
요청의 발송·수락·거절·취소 상태 전이를 보호하고, 한 쌍(pair)에 중복 활성 요청이 없음을 보장한다.

#### 애그리거트 루트
`StudentIdDisclosureRequest`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | `StudentIdDisclosureRequest` | id, requesterId, targetId, roomId, status, createdAt, updatedAt | 요청 단건 |

#### 비즈니스 불변식 (Invariants)

- **INV-01**: 동일 (requesterId, targetId, roomId) 쌍에 레코드는 최대 1개 존재한다.
  - DB UNIQUE 제약 `(requester_id, target_id, room_id)` 로 보장
  - 재요청 시 기존 REJECTED/CANCELED 레코드를 먼저 삭제 후 신규 삽입
- **INV-02**: requesterId ≠ targetId (자기 자신에게 요청 불가).
  - 위반 시: 400 BAD_REQUEST (CANNOT_REQUEST_SELF)
- **INV-03**: PENDING 상태 전이는 cancel(요청자) 또는 accept/reject(대상자)만 가능.
  - 위반 시: 403 FORBIDDEN
- **INV-04**: ACCEPTED/REJECTED/CANCELED 상태의 요청은 상태 재전이 불가.
  - 위반 시: 400 BAD_REQUEST

#### 라이프사이클 & 상태 머신

```
[없음]   -[요청자 발송]→         PENDING
PENDING  -[대상자 수락]→         ACCEPTED
PENDING  -[대상자 거절]→         REJECTED
PENDING  -[요청자 취소]→         CANCELED
ACCEPTED -[어느 한쪽 퇴장/방 삭제]→ [hard delete]
REJECTED -[요청자 재요청]→       [기존 삭제 후 신규 PENDING]
CANCELED -[요청자 재요청]→       [기존 삭제 후 신규 PENDING]
```

#### 트랜잭션 경계
- 요청 발송: `StudentIdDisclosureRequestService` 단일 트랜잭션
  1. 자기 자신 여부 검증
  2. 두 유저가 동일 방에 있는지 확인 (`OpenChatParticipantRepository` 조회)
  3. 기존 활성 레코드(PENDING/ACCEPTED) 존재 시 409 예외
  4. 기존 비활성 레코드(REJECTED/CANCELED) 존재 시 삭제
  5. 신규 PENDING 레코드 저장
- 수락/거절/취소: 단건 상태 전이, 단일 트랜잭션

#### 동시성 고려사항
- 동일 쌍의 동시 발송: DB UNIQUE 제약이 최후 방어선
  - 애플리케이션 레벨 선검증(existsByRequesterIdAndTargetIdAndRoomId) 후 저장
  - 충돌 시 `DataIntegrityViolationException` → 409 CONFLICT 변환

#### 도메인 이벤트
- `DisclosureRequestCreated`: 요청 발송 완료 시 → FCM 알림 트리거 (대상자에게)
- `DisclosureRequestAccepted`: 수락 완료 시 → FCM 알림 트리거 (요청자에게)
- `DisclosureRequestRejected`: 거절 완료 시 → FCM 알림 트리거 (요청자에게)

---

## 4. 애그리거트 관계도

```
User (requester)  --N:1-- StudentIdDisclosureRequest
User (target)     --N:1-- StudentIdDisclosureRequest
OpenChatRoom      --N:1-- StudentIdDisclosureRequest (roomId 참조, ID만)

StudentIdDisclosureRequest는 OpenChatParticipant와 직접 연관 없음
  → 서비스 레이어에서 OpenChatParticipantRepository로 참여 여부 검증
```

---

## 5. 도메인 이벤트

| 이벤트명 | 발행 주체 | 발행 시점 | 구독 주체 | 처리 내용 |
|----------|-----------|-----------|-----------|-----------|
| `DisclosureRequestCreated` | StudentIdDisclosureRequest | 요청 저장 완료 | FcmService | 대상자에게 CHAT 타입 FCM 알림 전송 |
| `DisclosureRequestAccepted` | StudentIdDisclosureRequest | 수락 상태 전이 완료 | FcmService | 요청자에게 CHAT 타입 FCM 알림 전송 |
| `DisclosureRequestRejected` | StudentIdDisclosureRequest | 거절 상태 전이 완료 | FcmService | 요청자에게 CHAT 타입 FCM 알림 전송 |

> `NotificationType.CHAT`이 이미 존재하므로 별도 타입 추가 불필요

---

## 6. 도메인 서비스

단일 서비스 `StudentIdDisclosureRequestService`로 통합 (로직이 단순하여 별도 도메인 서비스 불필요).

크로스-도메인 협력은 서비스 레이어에서 리포지토리 직접 참조로 처리:
- `OpenChatParticipantRepository.existsByRoomIdAndUserId()` — 동일 방 참여 여부 검증

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 컨텍스트 | 일관성 전략 | 처리 위치 |
|------|--------------|-------------|-----------|
| 요청 발송 시 동일 방 검증 | studentIdDisclosure → openChat | 단일 트랜잭션 (읽기만) | `DisclosureRequestService` |
| 채팅방 퇴장 시 공개 관계 삭제 | openChat → studentIdDisclosure | 순차 호출 (Controller) | `OpenChatRoomController.leave()` → `DisclosureRequestService.deleteByRoomAndUser()` 순차 호출 |
| 채팅방 삭제 시 공개 관계 삭제 | openChat → studentIdDisclosure | DB Cascade 또는 순차 호출 | `OpenChatRoomService.deleteRoom()` 내에서 순차 삭제 |

---

## 8. 레포지토리 인터페이스

### StudentIdDisclosureRequestRepository

```
// 활성 요청(PENDING/ACCEPTED) 존재 여부
existsByRequesterIdAndTargetIdAndRoomIdAndStatusIn(
    requesterId, targetId, roomId, statuses): boolean

// 요청 단건 조회 (수락/거절/취소 처리용)
findByIdAndRequesterId(id, requesterId): Optional<StudentIdDisclosureRequest>
findByIdAndTargetId(id, targetId): Optional<StudentIdDisclosureRequest>

// 재요청 전 기존 비활성 레코드 삭제
deleteByRequesterIdAndTargetIdAndRoomId(requesterId, targetId, roomId): void

// 채팅방 퇴장/삭제 시 해당 방의 특정 유저 관련 레코드 삭제
deleteByRoomIdAndRequesterIdOrRoomIdAndTargetId(roomId, userId): void

// 학번 공개 상태 조회 (특정 방에서 두 유저 간 ACCEPTED 여부)
findByRoomIdAndRequesterIdAndTargetIdAndStatus(
    roomId, userAId, userBId, ACCEPTED): Optional<StudentIdDisclosureRequest>
```

---

## 9. 패키지 구조 제안

```
com.example.appcenter_project
└── domain/
    └── studentIdDisclosure/
        ├── entity/
        │   └── StudentIdDisclosureRequest.java
        ├── enums/
        │   └── DisclosureRequestStatus.java   (PENDING, ACCEPTED, REJECTED, CANCELED)
        ├── dto/
        │   ├── request/
        │   │   └── RequestCreateDisclosureDto.java   (targetId, roomId)
        │   └── response/
        │       └── ResponseDisclosureStatusDto.java  (status, targetStudentNumber?)
        ├── service/
        │   └── StudentIdDisclosureRequestService.java
        ├── controller/
        │   ├── StudentIdDisclosureController.java
        │   └── StudentIdDisclosureApiSpecification.java
        └── repository/
            └── StudentIdDisclosureRequestRepository.java
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-01: 재요청 시 기존 레코드 삭제 후 신규 삽입
- **결정**: REJECTED/CANCELED 레코드를 삭제하고 새 PENDING 레코드를 삽입
- **이유**: `(requesterId, targetId, roomId)` UNIQUE 제약으로 중복 활성 요청을 DB 레벨에서 원천 차단, 쿼리 단순화
- **trade-off**: 요청 이력이 사라지나, 이력 조회 요구사항이 없으므로 허용

### ADR-02: 채팅방 퇴장 연동을 Controller 순차 호출로 처리
- **결정**: `openChat` 도메인 서비스에 `studentIdDisclosure` 의존성을 추가하지 않고, Controller에서 leave → deleteByRoomAndUser 순서로 호출
- **이유**: 도메인 간 결합 제거, 두 작업 모두 실패해도 재시도 가능(멱등)
- **trade-off**: 퇴장 성공 후 삭제 실패 시 고아 레코드 발생 가능 — ACCEPTED 레코드가 남아있어도 방 참여자가 아니면 실제 노출되지 않으므로 허용

### ADR-03: FCM 알림에 기존 NotificationType.CHAT 재사용
- **결정**: 별도 `STUDENT_ID_DISCLOSURE` 타입 추가 없이 기존 `CHAT` 타입 사용
- **이유**: 이미 `CHAT` 타입이 존재하며, 사용자 알림 수신 설정 UI를 변경하지 않아도 됨
- **trade-off**: 알림 종류를 세분화할 수 없으나, 현재 요구사항에서 불필요

---

## 11. 아키텍처 위험 요소

- **위험 1 — 고아 레코드**: 채팅방 퇴장 후 `deleteByRoomAndUser` 실패 시 ACCEPTED 레코드가 잔존. 방 참여자 여부를 함께 검증하는 조회 쿼리로 방어 가능.
- **위험 2 — 방향성 중복**: A→B와 B→A는 별개 레코드. "공개 관계" 조회 시 두 방향을 모두 검색해야 함 (`(requesterId=A AND targetId=B) OR (requesterId=B AND targetId=A)`). 조회 쿼리 설계 시 반드시 고려.
- **위험 3 — N+1**: 채팅방 참여자 목록 조회 시 각 참여자마다 학번 공개 여부를 단건 조회하면 N+1 발생. 조회 API는 roomId + currentUserId 기준으로 ACCEPTED 레코드를 한 번에 조회 후 매핑 필요.

---

## 12. TBD

- [ ] 채팅 메시지에서 학번 표시 시점: 수락 이후 메시지부터만 표시할지, 이전 메시지에도 소급 적용할지
- [ ] 채팅방 삭제 시 연쇄 삭제 방식: DB Cascade vs. 서비스 레이어 순차 삭제

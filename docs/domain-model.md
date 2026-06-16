# 도메인 모델

> 기반 요구사항: `docs/requirements.md`
> 기능: 오픈채팅 다중 방장 시스템

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| 방장 (Host) | `OpenChatParticipant.isHost = true`인 참여자. 방장 부여·방 삭제 권한을 가짐 |
| 단독 방장 (Sole Host) | 해당 방에서 `isHost = true`인 참여자가 오직 1명인 상태 |
| 방장 부여 (Grant Host) | 방장이 다른 참여자의 `isHost`를 `true`로 변경하는 행위 |
| 위임+나가기 (Delegate & Leave) | 단독 방장이 다른 참여자를 방장으로 지정하는 동시에 자신은 퇴장하는 단일 원자적 행위 |
| 방장 소멸 | 방장이 방을 나갈 때 해당 `OpenChatParticipant` row가 삭제되어 방장 권한이 사라지는 것 |
| 공식 방 (Official Room) | `isOfficial = true`인 방. ADMIN만 삭제 가능하며 ADMIN 참여 시 자동 방장 |
| 파생 방 (Derived Room) | `roomType = DERIVED`인 방. 다중 방장 규칙이 동일하게 적용됨 |
| ADMIN 역할 권한 | ADMIN role 사용자는 참여자 여부와 무관하게 모든 방의 방장 권한 행위를 수행할 수 있음 (`isHost` row 미생성) |

---

## 2. 바운디드 컨텍스트

단일 컨텍스트: `openChat`

외부 컨텍스트 의존:
- `fcm` — 방장 부여 시 알림 발송
- `user` — ADMIN role 확인, 참여자 정보 조회

---

## 3. 애그리거트

### Aggregate: OpenChatRoom

#### 책임
방의 메타데이터와 방장 목록(via OpenChatParticipant)의 일관성을 보호한다.

#### 애그리거트 루트
`OpenChatRoom`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | `OpenChatRoom` | id, name, description, scope, maxParticipants, createdBy, isOfficial, roomType, parentRoomId | `hostUserId` 제거됨 |
| Entity | `OpenChatParticipant` | id, roomId, userId, **isHost**, notificationEnabled, joinedAt, lastReadMessageId | `isHost` 신규 추가 |

#### 비즈니스 불변식 (Invariants)

- **INV-01**: 방에는 반드시 1명 이상의 방장이 존재해야 한다 (단, 방 삭제 직전 제외)
  - 위반 시: 단독 방장 퇴장 시도 → 400 BAD_REQUEST (OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE)

- **INV-02**: 방장 부여는 현재 참여자에게만 가능하다
  - 위반 시: 404 NOT_FOUND (OPEN_CHAT_PARTICIPANT_NOT_FOUND)

- **INV-03**: 이미 방장인 참여자에게는 방장을 중복 부여할 수 없다
  - 위반 시: 400 BAD_REQUEST (OPEN_CHAT_ALREADY_HOST)

- **INV-04**: 방장 권한은 박탈할 수 없다 — 부여 후 해당 참여자가 직접 나갈 때만 소멸
  - 설계상 revoke API 없음

- **INV-05**: 공식 방(`isOfficial=true`)은 ADMIN만 삭제할 수 있다
  - 위반 시: 403 FORBIDDEN (OPEN_CHAT_ROOM_FORBIDDEN)

- **INV-06**: 참여자 수는 `maxParticipants`를 초과할 수 없다 (기존 불변식 유지)

#### 라이프사이클 & 상태 머신

`OpenChatParticipant.isHost` 전이:

```
[false] -[방장 부여 by 방장 or ADMIN]→ [true]
[true]  -[퇴장 (participant row 삭제)]→ (소멸)
[재입장] → 새 row 생성, isHost = false
```

`OpenChatRoom` 전이:

```
[생성] -[방장 부여/위임/나가기/삭제]→ [운영 중]
[운영 중] -[호스트 삭제 or ADMIN 삭제]→ [삭제됨]
```

#### 트랜잭션 경계

| 유스케이스 | 트랜잭션 범위 |
|-----------|-------------|
| 방장 부여 | `OpenChatParticipant.isHost` 변경 + FCM 이벤트 발행 (단일 트랜잭션, FCM은 트랜잭션 외 비동기) |
| 위임+나가기 | 대상 participant `isHost=true` 변경 + 요청자 participant row 삭제 + 시스템 메시지 (단일 트랜잭션) |
| 방 삭제 | 모든 participant row 삭제 + room row 삭제 (단일 트랜잭션) |
| 일반 나가기 | participant row 삭제 + 시스템 메시지 (단일 트랜잭션) |

#### 동시성 고려사항

- **위임+나가기**: 단독 방장 여부 확인과 위임 처리 사이에 다른 방장 부여가 발생할 수 있음
  - 전략: **비관적 락** — 해당 `roomId`의 participant rows에 `SELECT FOR UPDATE` 적용
  - 적용 메서드: `findByRoomIdWithLock(roomId)` 또는 `findParticipantsByRoomIdWithLock(roomId)`

- **방장 부여**: 동시에 두 방장이 같은 참여자에게 방장 부여 시도 → DB unique constraint 없음, INV-03으로 서비스 레이어에서 방어

#### 도메인 이벤트

- `OpenChatHostGranted`: 방장이 부여된 시점 → FCM 서비스가 구독, 대상 유저에게 알림 발송

---

## 4. 애그리거트 관계도

```
OpenChatRoom --1:N--> OpenChatParticipant (roomId로 참조)
OpenChatRoom --1:N--> OpenChatMessage (roomId로 참조, 기존)
OpenChatRoom --0:1--> OpenChatRoom (parentRoomId, 파생 방)

[외부]
OpenChatParticipant --N:1--> User (userId로 참조)
OpenChatHostGranted --발행→ FCM 서비스
```

---

## 5. 도메인 이벤트

| 이벤트명 | 발행 주체 | 발행 시점 | 구독 주체 | 처리 내용 |
|----------|-----------|-----------|-----------|-----------|
| `OpenChatHostGranted` | OpenChatRoom | 방장 부여 완료 후 | OpenChatNotificationService | 대상 유저에게 FCM 알림 ("방장 권한이 부여되었습니다") |

---

## 6. 도메인 서비스

### HostGrantService (or OpenChatRoomService 내 메서드)

- **책임**: 방장 부여 행위의 권한 검증 + participant isHost 변경 + 이벤트 발행
- **관여 애그리거트**: OpenChatRoom, OpenChatParticipant, User(role 확인)
- **로직 요약**:
  1. 요청자가 ADMIN role이거나 해당 방의 `isHost=true` participant인지 확인
  2. 대상이 해당 방의 participant인지 확인 (없으면 404)
  3. 대상이 이미 `isHost=true`인지 확인 (이미 방장이면 400)
  4. 대상 participant의 `isHost = true` 저장
  5. `OpenChatHostGranted` 이벤트 발행
- **트랜잭션 전략**: 단일 트랜잭션 (FCM은 트랜잭션 커밋 후 비동기)

### SoleHostLeaveService (or OpenChatRoomService 내 메서드)

- **책임**: 단독 방장의 위임+나가기 원자적 처리
- **관여 애그리거트**: OpenChatRoom, OpenChatParticipant
- **로직 요약**:
  1. 비관적 락으로 해당 방 participant rows 잠금
  2. 요청자가 방장이며 단독 방장(`isHost=true` count == 1)인지 확인
  3. 위임 대상(`newHostUserId`)이 해당 방 participant인지 확인
  4. 위임 대상 participant의 `isHost = true` 저장
  5. 요청자 participant row 삭제 + 시스템 메시지 발송
  6. `OpenChatHostGranted` 이벤트 발행 (위임 대상 대상)
- **트랜잭션 전략**: 단일 트랜잭션 + 비관적 락

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 | 일관성 전략 | 이유 |
|------|------|------------|------|
| 방장 부여 후 FCM 발송 | OpenChatParticipant → FCM | 최종 일관성 (비동기) | FCM 실패 시 방장 부여 롤백하지 않음 (BR-11) |
| ADMIN role 확인 | OpenChatRoom ← User | 동기 (트랜잭션 내 조회) | 권한 결정이 핵심 불변식에 영향 |

---

## 8. 레포지토리 인터페이스

### OpenChatParticipantRepository (추가 메서드)

```
// isHost 여부 확인
existsByRoomIdAndUserIdAndIsHost(roomId, userId, isHost): boolean

// 방의 방장 수 조회
countByRoomIdAndIsHost(roomId, isHost): long

// 비관적 락으로 방 participant 전체 조회
findAllByRoomIdWithLock(roomId): List<OpenChatParticipant>

// 방의 방장 목록 조회
findAllByRoomIdAndIsHost(roomId, isHost): List<OpenChatParticipant>
```

### OpenChatRoomRepository (기존 유지, hostUserId 관련 쿼리 제거)

```
// 기존 findOldestParticipantExcluding 제거 (자동 방장 선임 로직 삭제)
```

---

## 9. 패키지 구조 제안

```
com.example.appcenter_project
└── domain/
    └── openChat/
        ├── entity/
        │   ├── OpenChatRoom.java        (hostUserId 필드 제거)
        │   └── OpenChatParticipant.java (isHost 필드 추가)
        ├── dto/
        │   └── request/
        │       └── RequestLeaveOpenChatRoomDto.java (newHostUserId 포함)
        ├── service/
        │   └── OpenChatRoomService.java (grantHost, leaveRoom 변경)
        └── repository/
            └── OpenChatParticipantRepository.java (신규 메서드 추가)
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-01: ADMIN 방장 권한은 role 체크로 처리 (isHost row 미생성)
- **결정**: ADMIN은 `OpenChatParticipant.isHost` 없이 role로 방장 행위 권한 보유
- **이유**: ADMIN이 의도치 않게 방 참여자 목록에 포함되는 부작용 방지
- **trade-off**: 방장 목록 조회 시 ADMIN은 노출되지 않음. ADMIN이 공식 방에 실제 참여할 때는 별도 입장 API 통해 participant row 생성 (isHost=true 포함)

### ADR-02: 단독 방장 위임+나가기에 비관적 락 적용
- **결정**: 위임+나가기 트랜잭션 시작 시 해당 방 participant rows에 `SELECT FOR UPDATE`
- **이유**: "단독 방장 여부 확인 → 위임 → 나가기" 사이 동시 방장 부여로 INV-01 위반 가능성 차단
- **trade-off**: 잠금으로 인한 처리량 감소. 방장 수 변경이 빈번하지 않아 실용적으로 허용 가능

### ADR-03: hostUserId 컬럼 제거
- **결정**: `open_chat_room.host_user_id` 컬럼 삭제, 방장 정보는 `open_chat_participant.is_host`로 일원화
- **이유**: 단일 진실 공급원 원칙. 두 곳에 방장 정보가 분산되면 동기화 오류 발생 가능
- **trade-off**: Flyway 마이그레이션으로 기존 데이터 이관 필요

---

## 11. 아키텍처 위험 요소

- **위험 1 — 비관적 락 데드락**: `findAllByRoomIdWithLock`과 다른 트랜잭션이 같은 participant row를 역순으로 잠그면 데드락 가능
  - 권고: 항상 `roomId` 단위로 잠금 범위를 제한, 단일 방에 대한 participant 잠금 순서 일관성 유지

- **위험 2 — FCM 알림 누락**: 트랜잭션 커밋 후 비동기 FCM 발송 실패 시 알림 유실
  - 권고: 기존 `FcmOutbox` 패턴 활용하거나, 실패 로그 기록 후 재시도 정책 적용 (현재 비동기 허용 — BR-11)

- **위험 3 — 공식 방 방장 없는 상태**: ADMIN이 공식 방에 입장하지 않은 경우 공식 방에 방장이 0명일 수 있음
  - 권고: 공식 방 생성 시 ADMIN을 자동 입장시키거나, 공식 방의 방장 조회 시 ADMIN role 보유자를 가상 방장으로 포함하는 로직 고려 (TBD)

---

## 12. TBD

- [ ] ADMIN이 공식 방에 자동 입장(participant row 생성)해야 하는지, 아니면 role 기반 권한만 갖는지
- [ ] 한 방의 최대 방장 수 제한 여부 (현재 무제한)
- [ ] 방장 부여 FCM 알림에 사용할 `NotificationType` — 기존 타입 재사용 또는 신규 추가
- [ ] 공식 방에 방장이 0명인 상태 허용 여부 및 처리 방침

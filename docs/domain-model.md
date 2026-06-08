# 도메인 모델

> 기반 요구사항: `docs/requirements.md`
> 기능: 파생 톡방 (비공개 그룹 채팅)

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| 오픈채팅방 (OPEN) | 탐색 목록에 노출되어 누구나 참여 가능한 기존 채팅방 (`roomType = OPEN`) |
| 파생 톡방 (DERIVED) | 특정 오픈채팅방 참여자끼리 초대로만 입장 가능한 비공개 채팅방 (`roomType = DERIVED`) |
| 부모 방 (Parent Room) | 파생 톡방이 파생된 원본 오픈채팅방 (`parentRoomId`로 참조) |
| 초대 (Invitation) | 파생 톡방 참여자가 부모 방의 다른 참여자에게 보내는 입장 요청 |
| 초대자 (Inviter) | 초대를 발송한 파생 톡방 참여자 |
| 피초대자 (Invitee) | 초대를 수신한 사용자 (부모 방 참여자여야 함) |
| 호스트 (Host) | 채팅방을 생성하거나 승계받은 대표 참여자 |

---

## 2. 바운디드 컨텍스트

단일 컨텍스트: `openChat`
- 기존 OpenChatRoom/OpenChatParticipant/OpenChatMessage 애그리거트에 OpenChatInvitation 애그리거트를 추가

---

## 3. 애그리거트

### Aggregate: OpenChatRoom

#### 책임
채팅방의 유형(OPEN/DERIVED)·정원·호스트 일관성을 보호하며, 파생 톡방 생성 자격을 검증한다.

#### 애그리거트 루트
`OpenChatRoom`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | `OpenChatRoom` | id, name, description, scope, maxParticipants, hostUserId, isOfficial, roomType, parentRoomId, lastMessage, lastMessageAt | 방 자체 |

#### 비즈니스 불변식 (Invariants)
- **INV-01**: `roomType = DERIVED`인 방은 반드시 `parentRoomId`가 존재해야 한다.
  - 위반 시: 도메인 생성 메서드 내부에서 IllegalArgumentException → CustomException 변환
- **INV-02**: `roomType = OPEN`인 방은 `parentRoomId`가 null이어야 한다.
  - 위반 시: 동일
- **INV-03**: `isOfficial = true`인 방은 삭제 불가.
  - 위반 시: 403 FORBIDDEN

#### 라이프사이클 & 상태 머신
```
[생성] roomType=OPEN or DERIVED
[존재] host 퇴장 시 → 다음 참여자로 host 승계
[존재] 참여자 0명 + isOfficial=false → 방 자동 삭제
[삭제] invitation, participant 연쇄 hard delete
```

#### 트랜잭션 경계
- 파생 톡방 생성: OpenChatRoom 단독 트랜잭션 (생성자를 첫 참여자로 OpenChatParticipant와 함께)
- 초대 수락 시 정원 체크: `findByIdWithLock(roomId)`로 OpenChatRoom에 비관적 락 → participant count 검증 후 OpenChatParticipant 등록

#### 동시성 고려사항
- 초대 수락 동시 요청 시 정원 초과 방지: `SELECT ... FOR UPDATE` (비관적 락)
- 기존 `joinRoom` 패턴(`findByIdWithLock`) 동일하게 적용

#### 도메인 이벤트
- `DerivedRoomCreated`: 파생 톡방 생성 완료 시 (현재 구독자 없음, 확장 고려)

---

### Aggregate: OpenChatParticipant

#### 책임
특정 방의 참여자 목록 일관성을 보호하며, 중복 참여를 방지한다.

#### 애그리거트 루트
`OpenChatParticipant`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | `OpenChatParticipant` | id, roomId, userId, notificationEnabled, joinedAt, lastReadMessageId | 방-사용자 참여 관계 |

#### 비즈니스 불변식 (Invariants)
- **INV-04**: (roomId, userId) 조합은 유일해야 한다 (DB UNIQUE 제약 + 애플리케이션 중복 체크).
  - 위반 시: 409 CONFLICT (OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS)

#### 트랜잭션 경계
- 초대 수락 도메인 서비스(`InvitationAcceptService`)가 OpenChatRoom 락 획득 후 OpenChatParticipant를 등록한다. 단일 트랜잭션.

#### 동시성 고려사항
- DB UNIQUE 제약이 최후 방어선; 애플리케이션 레벨에서 existsByRoomIdAndUserId 선검증.

---

### Aggregate: OpenChatInvitation

#### 책임
초대의 발송·수락·거절 상태 전이를 보호하며, 중복 초대와 비자격 초대를 막는다.

#### 애그리거트 루트
`OpenChatInvitation`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | `OpenChatInvitation` | id, roomId, inviterUserId, inviteeUserId, status, createdAt | 초대 단건 |

#### 비즈니스 불변식 (Invariants)
- **INV-05**: 동일 (roomId, inviteeUserId) 쌍에 PENDING 초대가 이미 존재하면 신규 초대 불가.
  - 위반 시: 409 CONFLICT (OPEN_CHAT_INVITATION_ALREADY_EXISTS)
- **INV-06**: ACCEPTED 또는 REJECTED 상태로 전이된 초대는 재전이 불가.
  - 위반 시: 400 BAD_REQUEST
- **INV-07**: 수락/거절은 inviteeUserId 본인만 수행 가능.
  - 위반 시: 403 FORBIDDEN

#### 라이프사이클 & 상태 머신
```
PENDING -[invitee 수락]→ ACCEPTED
PENDING -[invitee 거절]→ REJECTED
ACCEPTED → (종료, 불변)
REJECTED → (종료, 불변) — 재초대 시 신규 PENDING 생성
```

#### 트랜잭션 경계
- 초대 발송: OpenChatInvitation 단독 트랜잭션 (부모 방 참여 여부·중복 체크 포함)
- 초대 수락: `InvitationAcceptService` 도메인 서비스가 단일 트랜잭션 내에서
  1. OpenChatRoom 비관적 락 → 정원 체크
  2. OpenChatInvitation 상태 → ACCEPTED
  3. OpenChatParticipant 생성

#### 동시성 고려사항
- 수락 요청이 동시에 들어와도 OpenChatRoom 비관적 락이 순차 처리를 보장.

#### 도메인 이벤트
- `InvitationAccepted`: 초대 수락 완료 시 (현재 구독자 없음, FCM 확장 고려)

---

## 4. 애그리거트 관계도

```
OpenChatRoom (OPEN) --1:N--> OpenChatRoom (DERIVED, parentRoomId)
OpenChatRoom        --1:N--> OpenChatParticipant (roomId)
OpenChatRoom        --1:N--> OpenChatInvitation (roomId)
OpenChatInvitation           → inviterUserId (User ID 참조)
OpenChatInvitation           → inviteeUserId (User ID 참조)
```

---

## 5. 도메인 이벤트

| 이벤트명 | 발행 주체 | 발행 시점 | 구독 주체 | 처리 내용 |
|----------|-----------|-----------|-----------|-----------|
| `DerivedRoomCreated` | OpenChatRoom | 파생 톡방 생성 완료 | (없음, 확장용) | — |
| `InvitationAccepted` | OpenChatInvitation | 수락 상태 전이 완료 | (없음, 확장용) | — |

---

## 6. 도메인 서비스

### InvitationAcceptService
- **책임**: 초대 수락 시 두 애그리거트(OpenChatRoom, OpenChatInvitation) + OpenChatParticipant 생성을 단일 트랜잭션으로 조율
- **관여 애그리거트**: OpenChatRoom, OpenChatInvitation, OpenChatParticipant
- **로직 요약**:
  1. OpenChatInvitation 조회 → invitee 본인 검증 (INV-07)
  2. 상태 PENDING 확인 (INV-06)
  3. OpenChatRoom 비관적 락 획득 → 정원 체크 (BR-10)
  4. OpenChatParticipant 중복 체크 (INV-04)
  5. OpenChatInvitation.status → ACCEPTED
  6. OpenChatParticipant 생성 & 저장
- **트랜잭션 전략**: 단일 트랜잭션 (`@Transactional`)

### InvitationSendService
- **책임**: 초대 발송 자격(inviter가 파생 톡방 참여자, invitee가 부모 방 참여자)을 검증하고 초대를 생성
- **관여 애그리거트**: OpenChatRoom, OpenChatParticipant, OpenChatInvitation
- **로직 요약**:
  1. 파생 톡방 존재 확인 + roomType=DERIVED 검증
  2. inviter가 해당 파생 톡방 참여자인지 확인 (BR-01 파생)
  3. invitee가 부모 방(parentRoomId) 참여자인지 확인 (BR-02)
  4. invitee가 이미 파생 톡방 참여자인지 확인 (BR-04)
  5. PENDING 중복 초대 확인 (INV-05)
  6. OpenChatInvitation 생성 & 저장
- **트랜잭션 전략**: 단일 트랜잭션

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 애그리거트 | 일관성 전략 | 이유 |
|------|----------------|-------------|------|
| 파생 톡방 생성 | OpenChatRoom → OpenChatParticipant | 단일 트랜잭션 | 생성자를 즉시 참여자로 등록해야 일관성 보장 |
| 초대 발송 | OpenChatParticipant(검증) → OpenChatInvitation(생성) | 단일 트랜잭션 | 발송 자격과 초대 생성이 원자적이어야 함 |
| 초대 수락 | OpenChatRoom(락) + OpenChatInvitation(상태변경) + OpenChatParticipant(생성) | 단일 트랜잭션 | 정원 체크·상태 전이·참여 등록이 동시에 일관되어야 함 |

---

## 8. 레포지토리 인터페이스

### OpenChatRoomRepository (기존 확장)
```
// 기존 메서드 유지
findAllPublicRooms(): List<OpenChatRoom>   // roomType=OPEN만 반환하도록 필터 추가
findMyRooms(userId): List<OpenChatRoom>     // DERIVED 포함 (MY 탭)
findByIdWithLock(roomId): Optional<OpenChatRoom>  // 비관적 락 (기존)
```

### OpenChatParticipantRepository (기존 확장)
```
// 기존 메서드 유지
findByRoomId(roomId): List<OpenChatParticipant>  // 참여자 목록 조회 (신규)
existsByRoomIdAndUserId(roomId, userId): boolean
```

### OpenChatInvitationRepository (신규)
```
findByIdAndInviteeUserId(id, inviteeUserId): Optional<OpenChatInvitation>
existsByRoomIdAndInviteeUserIdAndStatus(roomId, inviteeUserId, PENDING): boolean
findByInviteeUserId(inviteeUserId): List<OpenChatInvitation>  // TBD
```

---

## 9. 패키지 구조 제안

```
com.example.appcenter_project
└── domain/
    └── openChat/
        ├── entity/
        │   ├── OpenChatRoom.java         (roomType, parentRoomId 필드 추가)
        │   ├── OpenChatParticipant.java  (기존 유지)
        │   ├── OpenChatMessage.java      (기존 유지)
        │   └── OpenChatInvitation.java   (신규)
        ├── enums/
        │   ├── OpenChatRoomType.java     (신규: OPEN, DERIVED)
        │   ├── OpenChatInvitationStatus.java  (신규: PENDING, ACCEPTED, REJECTED)
        │   ├── OpenChatRoomScope.java    (기존 유지)
        │   └── OpenChatRoomTab.java      (기존 유지)
        ├── dto/
        │   ├── request/
        │   │   ├── RequestCreateDerivedRoomDto.java   (신규)
        │   │   └── RequestSendInvitationDto.java      (신규)
        │   └── response/
        │       ├── ResponseOpenChatParticipantDto.java  (신규)
        │       └── ResponseOpenChatInvitationDto.java   (신규)
        ├── service/
        │   ├── OpenChatRoomService.java         (기존, findAllPublicRooms 필터 수정)
        │   ├── OpenChatInvitationService.java   (신규: InvitationSendService + InvitationAcceptService 통합)
        │   └── OpenChatMessageService.java      (기존 유지)
        └── repository/
            ├── OpenChatRoomRepository.java
            ├── OpenChatInvitationRepository.java      (신규)
            ├── OpenChatInvitationQuerydslRepository.java  (신규)
            └── OpenChatInvitationQuerydslRepositoryImpl.java (신규)
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-01: OpenChatInvitation을 독립 애그리거트로 분리
- **결정**: OpenChatInvitation을 OpenChatRoom 내부 엔티티가 아닌 독립 애그리거트 루트로 설계
- **이유**: 초대 조회(나에게 온 초대 목록 등)가 Room 경계를 넘으며, 초대 목록이 커져도 Room 애그리거트를 비대화하지 않기 위함
- **trade-off**: 수락 로직에 도메인 서비스(InvitationAcceptService)가 추가되지만, 각 애그리거트 책임이 명확해짐

### ADR-02: 초대 수락 시 OpenChatRoom 비관적 락으로 정원 체크
- **결정**: `findByIdWithLock(roomId)` → participant count 검증 → 참여자 등록 순으로 단일 트랜잭션 처리
- **이유**: 기존 `joinRoom` 패턴과 동일하게 유지해 코드 일관성 확보, 이중 락 없이 정원 보장
- **trade-off**: 같은 방에 동시 수락 요청 시 순차 처리되지만, 파생 톡방 특성상 동시 수락 빈도가 낮아 허용

### ADR-03: 파생 톡방 host 퇴장 처리를 기존 OPEN 방과 동일하게 적용
- **결정**: host 퇴장 시 가장 오래된 참여자 승계, 참여자 0명이면 방 삭제 (`handleHostLeave` 재사용)
- **이유**: 파생 톡방도 지속적인 소통 공간이 될 수 있으므로 host 부재로 강제 삭제하지 않음
- **trade-off**: 로직 재사용으로 구현 간단하나, 파생 톡방 특화 처리(예: 부모 방 연결 해제)는 TBD

---

## 11. 아키텍처 위험 요소

- **위험 1 — 기존 findAllPublicRooms 필터 누락**: `OpenChatRoomQuerydslRepositoryImpl`의 탐색 조회에서 `roomType = OPEN` 필터를 누락하면 파생 톡방이 노출됨. 마이그레이션으로 기존 row에 `room_type = 'OPEN'` 기본값 설정 필수.
- **위험 2 — parentRoomId 정합성**: 파생 톡방 생성 시 parentRoomId가 실제 OPEN 방인지 검증하지 않으면 DERIVED 방을 부모로 지정 가능 (재파생). 서비스 레이어에서 `roomType = OPEN` 검증 필수.
- **위험 3 — 초대 수락 후 parent 방 탈퇴**: invitee가 초대 수락 후 부모 방을 나가도 파생 톡방 참여는 유지됨. 의도된 동작이나 UX 혼란 가능성 있음 → 문서화 필요.

---

## 12. TBD

- [ ] `maxParticipants` 기본값 및 상한 (예: 최대 50명)
- [ ] 초대 목록 조회 API (나에게 온 초대, 내가 보낸 초대) 포함 여부
- [ ] parent 오픈채팅방 삭제 시 파생 톡방 처리 (유지 vs. 연쇄 삭제)
- [ ] 파생 톡방 참여자가 부모 방을 나갈 경우 파생 톡방 유지 여부

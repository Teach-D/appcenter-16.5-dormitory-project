# 도메인 모델

> 기반 요구사항: `docs/requirements.md`

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| 오픈채팅방 (OpenChatRoom) | 기숙사생이 자유롭게 개설하는 채팅 공간. 공개 범위에 따라 전체 또는 특정 기숙사에게만 노출 |
| 참여자 (Participant) | 채팅방에 입장한 유저. 방당 최대 인원 제한 있음 |
| 방장 (Host) | 채팅방의 현재 관리자. 생성자 또는 방장 이전으로 지정된 참여자 |
| 공개 범위 (Scope) | DORMITORY(특정 기숙사) 또는 ALL(전체 공개) |
| 공식 방 (Official Room) | 서비스가 직접 생성한 방. created_by=NULL + is_official=TRUE 조합으로 식별. 삭제 보호 적용 |
| 방장 이전 | 현재 방장이 나갈 때 joined_at 기준 가장 오래된 참여자에게 방장 권한 자동 이전 |
| 기숙사 탭 (Dormitory Tab) | 요청자의 DormType과 creator_dormitory가 일치하는 방만 노출하는 목록 탭 |
| 멱등 입장 | 이미 참여 중인 방에 재입장 요청 시 에러 없이 roomId 정상 반환 |
| 메시지 (Message) | 참여자가 채팅방에 발행한 텍스트. TEXT(일반), SYSTEM(시스템 알림) 구분 |
| 마지막 읽은 메시지 (lastReadMessageId) | 각 참여자가 마지막으로 읽은 메시지 ID. 읽지 않은 메시지 수 계산의 기준 |
| 읽지 않은 사람 수 (unreadCount) | 특정 메시지를 아직 읽지 않은 참여자 수. 총 참여자 수 − lastReadMessageId ≥ 해당 메시지 ID인 참여자 수 |
| 시스템 메시지 | 입장/퇴장 이벤트 시 자동 생성되는 메시지. type=SYSTEM |

---

## 2. 바운디드 컨텍스트

```
[ OpenChat Context ]          [ User Context ]
  OpenChatRoom                  User (ID 참조만)
  OpenChatParticipant           DormType (enum 공유)
  OpenChatMessage (Phase 2)
```

User 컨텍스트는 ID와 DormType만 참조. User 엔티티 직접 소유 금지.

---

## 3. 애그리거트

### Aggregate: OpenChatRoom

#### 책임
채팅방 참여 인원의 일관성(최대 인원 초과 방지, 방장 단일성)과 공개 범위 접근 제어를 보호한다.

#### 애그리거트 루트
`OpenChatRoom`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | OpenChatRoom | id, name, description, scope, maxParticipants, creatorDormitory, hostUserId, lastMessageAt, **lastMessage**, isOfficial, createdBy, createdAt | 채팅방 메타정보 및 현재 방장. lastMessage는 Phase 2에서 추가 |
| Entity | OpenChatParticipant | id, roomId, userId, notificationEnabled, joinedAt, **lastReadMessageId** | 루트가 소유하는 참여자 컬렉션 멤버. lastReadMessageId는 Phase 2에서 추가 |
| VO | RoomScope | DORMITORY \| ALL | 공개 범위. 변경 불가 값 객체 |

#### 비즈니스 불변식 (Invariants)

- **INV-01**: 참여자 수는 `maxParticipants`를 초과할 수 없다
  - 위반 시: 400 OPEN_CHAT_ROOM_FULL
- **INV-02**: `hostUserId`는 현재 참여자 목록 중 정확히 1명을 가리켜야 한다
  - 위반 시: 방장 이전 또는 방 삭제로 일관성 복구
- **INV-03**: `scope = DORMITORY`인 방의 입장자는 `user.dormType == creatorDormitory`이어야 한다
  - 위반 시: 403 FORBIDDEN
- **INV-04**: `is_official = TRUE`인 방은 참여자 수가 0이 되어도 삭제되지 않는다
  - 위반 시: 삭제 로직 진입 차단

#### 라이프사이클 & 상태 머신

OpenChatRoom에 별도 status 필드 없음. 상태는 참여자 수와 isOfficial로 암묵적으로 표현.

```
[생성] --입장--> [활성: 참여자 1명 이상]
[활성] --나가기(방장, 참여자 1명)--> [삭제] (is_official=FALSE만)
[활성] --나가기(방장, 참여자 N명)--> [방장 이전] --> [활성]
[활성] --ADMIN 삭제--> [삭제]
```

#### 트랜잭션 경계

- **방 생성**: OpenChatRoom + OpenChatParticipant(첫 번째 = 방장) 동시 생성 → 단일 트랜잭션
- **방 입장**: OpenChatRoom 락 획득 → 인원 체크 → OpenChatParticipant 삽입 → 단일 트랜잭션
- **방 나가기**: OpenChatParticipant 삭제 → 방장 이전(필요 시 OpenChatRoom.hostUserId 업데이트) → 참여자 0명이면 OpenChatRoom 삭제 → 단일 트랜잭션

#### 동시성 고려사항

- **비관적 락 (SELECT FOR UPDATE)**: 방 입장 시 `OpenChatRoom` 행에 락 획득 후 인원 수 체크
  - 이유: 마지막 자리를 두고 동시 입장 시 `maxParticipants` 초과 방지
  - 적용 대상: `OpenChatRoomRepository.findByIdWithLock(roomId)`
- **방장 이전**: 단일 트랜잭션 내에서 처리되므로 추가 락 불필요

#### 도메인 이벤트

- `RoomCreated`: 방 생성 완료 시 발행 (Phase 2 - FCM 알림 연동 시 사용)
- `ParticipantJoined`: 새 참여자 입장 시 발행 (Phase 2)
- `HostTransferred`: 방장 이전 완료 시 발행 (Phase 2)
- `RoomDeleted`: 방 삭제 시 발행 (Phase 2 - 연관 메시지 정리)

---

### Aggregate: OpenChatMessage (Phase 2 구현)

#### 책임
채팅 메시지의 원자적 저장을 보장하고, 발행 후 방의 lastMessage/lastMessageAt 갱신 및 발신자의 lastReadMessageId 동기화를 주도한다.

#### 애그리거트 루트
`OpenChatMessage`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | OpenChatMessage | id, roomId, senderId, content, type, createdAt | 개별 메시지. Room과 ID 참조로 연결 |
| VO | MessageType | TEXT \| IMAGE \| **SYSTEM** | 메시지 종류. SYSTEM은 입장/퇴장 자동 생성 |

#### 비즈니스 불변식 (Invariants)

- **INV-MSG-01**: 메시지 발행자(senderId)는 해당 방의 `OpenChatParticipant`로 등록된 참여자여야 한다
  - 위반 시: 메시지 처리 중단 (STOMP ERROR 또는 무시)
- **INV-MSG-02**: SYSTEM 메시지의 senderId는 0 (시스템 발행)으로 고정
- **INV-MSG-03**: `lastMessage`는 메시지 내용 최대 500자 truncate 후 저장

#### 트랜잭션 경계

메시지 저장(`OpenChatMessage` INSERT) + 방 정보 갱신(`OpenChatRoom.lastMessage`, `lastMessageAt` UPDATE) + 발신자 읽음 갱신(`OpenChatParticipant.lastReadMessageId` UPDATE)을 **단일 트랜잭션**으로 처리.

이유: lastMessage/lastMessageAt 갱신 실패 시 목록 미리보기가 stale 상태로 남는 것을 방지.

#### 동시성 고려사항

메시지 저장은 append-only로 경쟁 조건 없음. `lastReadMessageId` 갱신은 단순 UPDATE(단일 참여자 행)이므로 락 불필요.

#### 도메인 이벤트

- `MessageSent`: 메시지 저장 완료 후 발행 → STOMP 브로드캐스트 트리거

---

## 4. 애그리거트 관계도

```
User (외부 컨텍스트)
  │  (userId로만 참조)
  │
  ├──[createdBy]──> OpenChatRoom (Aggregate Root)
  │                   │
  │                   └──[1:N]──> OpenChatParticipant
  │                                 (roomId + userId 복합 UQ)
  │
  └──[senderId]──> OpenChatMessage (별도 Aggregate)
                      (roomId로 OpenChatRoom 참조)
```

---

## 5. 도메인 이벤트

| 이벤트명 | 발행 주체 | 발행 시점 | 구독 주체 | 처리 내용 |
|----------|-----------|-----------|-----------|-----------|
| RoomCreated | OpenChatRoom | 방 생성 완료 | (Phase 3) FCM | 참여자 알림 |
| ParticipantJoined | OpenChatRoom | 입장 완료 | **OpenChatMessageService** | SYSTEM 메시지 저장 + 브로드캐스트 |
| ParticipantLeft | OpenChatRoom | 퇴장 완료 | **OpenChatMessageService** | SYSTEM 메시지 저장 + 브로드캐스트 |
| HostTransferred | OpenChatRoom | 방장 이전 완료 | (Phase 3) FCM | 신규 방장 알림 |
| RoomDeleted | OpenChatRoom | 방 삭제 | OpenChatMessage | 연관 메시지 cascade 삭제 |
| MessageSent | OpenChatMessage | 메시지 저장 완료 | SimpMessagingTemplate | `/sub/openchat/{roomId}` 브로드캐스트 |

**Phase 2 이벤트 발행 전략**: 도메인 이벤트 대신 `OpenChatRoomService`에서 `OpenChatMessageService`를 직접 호출하는 방식으로 구현 (단순성 우선, Phase 3에서 이벤트 기반으로 전환 가능).

---

## 6. 도메인 서비스

### OpenChatRoomHostTransferService

- **책임**: 방장 나가기 시 단일 트랜잭션 내 방장 이전 또는 방 삭제 결정
- **관여 애그리거트**: OpenChatRoom, OpenChatParticipant
- **로직 요약**:
  1. 나가는 유저가 방장인지 확인 (`room.hostUserId == leavingUserId`)
  2. 방장이면: 남은 참여자를 `joinedAt ASC` 정렬하여 첫 번째에게 방장 이전 (`room.hostUserId` 업데이트)
  3. 참여자가 0명이고 `is_official = FALSE`이면: 방 삭제
  4. `is_official = TRUE`이면: 삭제 없이 방장 이전만 수행
- **트랜잭션 전략**: 단일 트랜잭션 (OpenChatRoom + OpenChatParticipant 동시 변경)

### OpenChatMessageService (Phase 2 신규)

- **책임**: STOMP 메시지 발행 처리, 시스템 메시지 생성, 브로드캐스트, 읽음 상태 갱신, 커서 기반 채팅 내역 조회
- **관여 애그리거트**: OpenChatMessage, OpenChatRoom(lastMessage 갱신), OpenChatParticipant(lastReadMessageId 갱신)
- **로직 요약**:
  1. **sendMessage**: 발행자 참여자 검증 → OpenChatMessage 저장 → OpenChatRoom lastMessage/lastMessageAt 갱신 → 발신자 lastReadMessageId 갱신 → `/sub/openchat/{roomId}` 브로드캐스트
  2. **sendSystemMessage**: SYSTEM 타입 메시지 저장 + 브로드캐스트 (senderId=0)
  3. **getMessages**: 참여자 검증 → 커서 기반 조회 → 조회자 lastReadMessageId 갱신 → 응답 반환
  4. **updateLastRead**: `SessionSubscribeEvent` 시 lastReadMessageId를 방의 최신 메시지 ID로 갱신
- **트랜잭션 전략**: sendMessage는 단일 트랜잭션. STOMP 브로드캐스트는 트랜잭션 커밋 후 수행

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 애그리거트 | 일관성 전략 | 이유 |
|------|----------------|-------------|------|
| 방 입장 시 인원 초과 체크 | OpenChatRoom → OpenChatParticipant | 단일 트랜잭션 + 비관적 락 | INV-01 보장 |
| 방장 나가기 → 방장 이전 | OpenChatRoom + OpenChatParticipant | 단일 트랜잭션 | 방장 단일성(INV-02) 보장 |
| 메시지 전송 → lastMessage/lastMessageAt 갱신 | OpenChatMessage → OpenChatRoom | 단일 트랜잭션 (직접 호출) | 목록 미리보기 즉시 반영 |
| 메시지 전송 → 발신자 lastReadMessageId 갱신 | OpenChatMessage → OpenChatParticipant | 단일 트랜잭션 | 발신자 미읽음 카운트 0 보장 |
| 구독(SessionSubscribeEvent) → lastReadMessageId 갱신 | 없음(서비스 직접) → OpenChatParticipant | 별도 트랜잭션 (이벤트 리스너) | 구독 시점 읽음 상태 최신화 |
| 채팅 내역 조회 → lastReadMessageId 갱신 | 없음(서비스 직접) → OpenChatParticipant | 단일 트랜잭션 | 조회 = 읽음 처리로 간주 |
| 입장/퇴장 → SYSTEM 메시지 발행 | OpenChatRoom → OpenChatMessage | 별도 트랜잭션 (OpenChatRoomService → OpenChatMessageService 직접 호출) | 입장/퇴장 트랜잭션과 분리, 실패해도 입장/퇴장 롤백 불필요 |

---

## 8. 레포지토리 인터페이스

### OpenChatRoomRepository

```
// 3탭 조회
findMyRooms(userId): List<OpenChatRoom>
findByCreatorDormitory(dormType): List<OpenChatRoom>
findAllPublic(): List<OpenChatRoom>

// 입장 시 비관적 락
findByIdWithLock(roomId): Optional<OpenChatRoom>
```

### OpenChatParticipantRepository

```
// 방장 이전용 — joinedAt ASC 정렬, 나가는 유저 제외
findOldestParticipantExcluding(roomId, excludeUserId): Optional<OpenChatParticipant>

// 멱등 입장 체크
existsByRoomIdAndUserId(roomId, userId): boolean

// 현재 인원 수
countByRoomId(roomId): int

// 나가기
findByRoomIdAndUserId(roomId, userId): Optional<OpenChatParticipant>
```

### OpenChatParticipantRepository (Phase 2 추가)

```
// 읽지 않은 사람 수 계산: 방의 참여자 중 lastReadMessageId < messageId인 수
countUnreadParticipants(roomId, messageId): int

// lastReadMessageId 갱신
updateLastReadMessageId(roomId, userId, messageId): void

// MY 탭 unreadCount 계산: 방별 id > lastReadMessageId인 메시지 수
findMyRoomsWithUnreadCount(userId): List<OpenChatParticipantWithUnread>
```

### OpenChatMessageRepository (Phase 2 구현)

```
// 커서 기반 페이징 (lastMessageId 미전달 시 최신 30건)
findByRoomIdWithCursor(roomId, lastMessageId, size): List<OpenChatMessage>

// 방의 최신 메시지 ID (구독 시 lastReadMessageId 갱신용)
findLatestMessageIdByRoomId(roomId): Optional<Long>

// unreadCount 계산용
countByRoomIdAndIdGreaterThan(roomId, lastReadMessageId): long
```

---

## 9. 패키지 구조 제안

```
com.example.appcenter_project
└── domain/
    └── openChat/
        ├── controller/
        │   ├── OpenChatRoomController.java
        │   └── OpenChatRoomApiSpecification.java
        ├── dto/
        │   ├── request/
        │   │   ├── RequestCreateOpenChatRoomDto.java
        │   │   └── RequestJoinOpenChatRoomDto.java  (roomId PathVariable 사용 시 불필요)
        │   └── response/
        │       ├── ResponseOpenChatRoomDto.java      (목록용 — isJoined 포함)
        │       └── ResponseOpenChatRoomDetailDto.java (방 입장 후 상세)
        ├── entity/
        │   ├── OpenChatRoom.java
        │   ├── OpenChatParticipant.java
        │   └── OpenChatMessage.java
        ├── enums/
        │   ├── OpenChatRoomScope.java   (DORMITORY, ALL)
        │   └── OpenChatMessageType.java (TEXT, IMAGE)
        ├── repository/
        │   ├── OpenChatRoomRepository.java
        │   ├── OpenChatParticipantRepository.java
        │   └── OpenChatMessageRepository.java
        ├── service/
        │   ├── OpenChatRoomService.java
        │   ├── OpenChatRoomHostTransferService.java
        │   └── OpenChatMessageService.java          ← Phase 2 신규
        └── config/ (global/config/)
            └── OpenChatWebSocketEventListener.java  ← Phase 2 신규 (global 패키지)
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-01: OpenChatParticipant를 OpenChatRoom 애그리거트 내부에 귀속

- **결정**: OpenChatParticipant는 OpenChatRoom의 자식 엔티티
- **이유**: 인원 초과 체크(INV-01)와 방장 이전(INV-02)이 단일 트랜잭션으로 처리되어야 하며, 두 엔티티의 변경이 항상 함께 발생
- **trade-off**: 참여자 수가 매우 많아지면 애그리거트 로딩 비용 증가. 현재 MVP 규모에서 무방하며, 이후 필요 시 별도 애그리거트로 분리 가능

### ADR-02: 방장 정보를 OpenChatRoom.hostUserId 필드로 관리

- **결정**: `OpenChatRoom`에 `hostUserId` 컬럼 추가
- **이유**: 방장 확인이 매 요청마다 필요한데, Room 조회 한 번으로 방장 식별 가능. 방장 이전 시 Room + Participant 동시 업데이트가 단일 트랜잭션 내에서 처리됨
- **trade-off**: 방장 이전 시 두 엔티티를 동시에 업데이트해야 하지만, 애그리거트 내부 작업이므로 일관성 보장

### ADR-03: 방 입장 시 비관적 락 적용

- **결정**: `findByIdWithLock` (SELECT FOR UPDATE)으로 OpenChatRoom 행 락 획득 후 인원 체크
- **이유**: 마지막 자리 동시 입장 시 `maxParticipants` 초과를 완전히 차단. MVP에서 입장 요청 빈도가 높지 않아 락 경합 위험 낮음
- **trade-off**: 동시 입장 폭증 시 DB 락 경합. 향후 트래픽 증가 시 낙관적 락 또는 Redis 분산 락으로 전환 검토

### ADR-04: lastReadMessageId를 OpenChatParticipant에 보관 (Phase 2)

- **결정**: 별도 읽음 추적 테이블(`open_chat_message_read`) 없이 `OpenChatParticipant.lastReadMessageId` 단일 컬럼으로 관리
- **이유**: N명 × M메시지 규모의 읽음 테이블은 불필요한 복잡성. 오픈채팅 특성상 "커서 기준 이후 메시지 수"가 unreadCount로 충분히 근사
- **trade-off**: 메시지를 순서대로 읽지 않은 케이스(스크롤 점프)에서 unreadCount 부정확 가능. 허용 가능한 수준으로 판단

### ADR-05: SYSTEM 메시지 senderId=0 고정 (Phase 2)

- **결정**: 시스템 메시지의 `senderId`를 0(존재하지 않는 유저 ID)으로 고정
- **이유**: NULL 허용 시 스키마 변경 필요, senderId가 외부 컨텍스트 User ID 참조이므로 0을 시스템 예약으로 사용
- **trade-off**: 클라이언트에서 senderId=0 체크로 시스템 메시지 판별 필요. `type=SYSTEM` 필드로 이중 판별 가능

### ADR-06: 입장/퇴장 SYSTEM 메시지를 별도 트랜잭션으로 발행 (Phase 2)

- **결정**: `joinRoom()`, `leaveRoom()` 트랜잭션 커밋 후 `OpenChatMessageService.sendSystemMessage()` 별도 호출
- **이유**: SYSTEM 메시지 저장 실패 시 입장/퇴장 자체가 롤백되는 것은 과도한 결합. 시스템 메시지는 부가 정보
- **trade-off**: 입장 성공 후 시스템 메시지 저장 실패 시 알림 누락. 허용 가능한 수준

---

## 11. 아키텍처 위험 요소

- **방장 이전 실패 시 무방장 상태**: `OpenChatRoomHostTransferService`가 트랜잭션 실패 시 방장 없는 방이 생길 수 있음 → 단일 트랜잭션 보장 필수
- **is_official 방의 방장 지정**: `created_by=NULL` 공식 방의 `hostUserId`가 NULL이면 방장 확인 로직에서 NPE 가능 → 첫 입장 유저를 방장으로 설정하거나 hostUserId NULL 허용으로 명시적 처리 필요
- **DORMITORY scope 필터링 정확성**: `creatorDormitory` 컬럼이 NULL인 ALL 방을 기숙사 탭에 노출하지 않도록 WHERE 조건 명시 필요
- **[Phase 2] lastReadMessageId NULL 초기값**: 신규 참여자의 lastReadMessageId가 NULL인 경우 unreadCount 계산 시 NPE 또는 전체 메시지를 unread로 처리하는 로직 필요 → NULL = 0으로 간주, COUNT(id > 0) = 전체 메시지 수
- **[Phase 2] STOMP 세션과 HTTP 세션의 userId 불일치**: WebSocketAuthInterceptor에서 세션에 저장한 userId가 올바르게 전파되는지 확인 필요 (룸메이트 채팅의 기존 패턴 재사용으로 위험 낮음)
- **[Phase 2] 메시지 전송 후 STOMP 브로드캐스트 타이밍**: `@Transactional` 메서드 내부에서 `messagingTemplate.convertAndSend()` 호출 시 트랜잭션 롤백 후에도 WebSocket 메시지가 이미 전송될 수 있음 → 룸메이트 채팅과 동일 패턴으로 허용

---

## 12. TBD

- [x] 방 목록 정렬 기준: `last_message_at DESC` (Phase 2에서 확정)
- [x] 페이지네이션 방식: 채팅 내역은 커서 기반, 방 목록은 offset 유지
- [ ] 방 상세 조회 API 별도 필요 여부
- [ ] `notification_enabled` ON/OFF 변경 API Phase 1 포함 여부
- [ ] is_official 방 hostUserId NULL 처리 전략
- [ ] [Phase 2] unreadCount 계산에서 SYSTEM 메시지 포함 여부 (현재: 포함)
- [ ] [Phase 3] FCM 오프라인 알림 발송 전략
- [ ] [Phase 3] 이미지 메시지 지원

# 도메인 모델

> 기반 요구사항: `docs/requirements.md`
> 기능: 오픈채팅 읽음 처리 (Read Receipt)

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| 읽음 처리 (Mark as Read) | 특정 참여자의 `lastReadMessageId`를 메시지 ID로 갱신하는 행위 |
| 미읽음 수 (unreadCount) | 전체 참여자 중 해당 메시지를 아직 읽지 않은 참여자 수. `전체 참여자 수 - lastReadMessageId >= messageId 인 참여자 수` |
| READ 이벤트 | `/sub/openchat/{roomId}/read` 토픽으로 전파되는 `{messageId, unreadCount}` 페이로드 |
| 구독자 (Subscriber) | 현재 WebSocket `/sub/openchat/{roomId}`를 구독 중인 사용자. 메시지 수신 즉시 자동 읽음 처리됨 |
| 세션 레지스트리 (SessionRegistry) | `roomId → Set<userId>` 를 in-memory로 유지하는 단일 서버 컴포넌트 |
| 오프라인 읽음 | WebSocket 미연결 상태에서 `getMessages()` API 호출로 발생하는 읽음 처리 |

---

## 2. 바운디드 컨텍스트

단일 컨텍스트: `openChat`

외부 컨텍스트 의존:
- `user` — userId → 사용자 존재 확인 (기존 의존 유지)
- WebSocket 인프라 (`SimpMessagingTemplate`) — READ 이벤트 전파

---

## 3. 애그리거트

### Aggregate: OpenChatRoom (기존, 수정 없음)

읽음 처리 기능에서 `OpenChatRoom` 엔티티 자체에는 변경이 없습니다. `lastMessage`, `lastMessageAt` 갱신은 기존과 동일합니다.

---

### Aggregate: OpenChatParticipant (기존, DB 스키마 변경 없음)

#### 책임
각 참여자의 읽음 커서(`lastReadMessageId`)를 보호하여 `unreadCount` 계산의 정확성을 보장한다.

#### 애그리거트 루트
`OpenChatParticipant`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | `OpenChatParticipant` | id, roomId, userId, **lastReadMessageId**, isHost, notificationEnabled, joinedAt | `lastReadMessageId`: 마지막으로 읽은 메시지 ID. NULL = 한 번도 읽지 않음 |

#### 비즈니스 불변식 (Invariants)

- **INV-01**: `lastReadMessageId`는 한 번 갱신되면 이전 값으로 되돌릴 수 없다 (단조 증가)
  - 위반 시: 무시 (서비스 레이어에서 `messageId <= lastReadMessageId` 이면 업데이트 생략)

- **INV-02**: 구독 중인 사용자의 `lastReadMessageId`는 해당 방의 최신 메시지 ID보다 작을 수 없다
  - 보장 방법: 메시지 저장 완료 직후 구독자 전원을 일괄 업데이트

#### 트랜잭션 경계

| 유스케이스 | 트랜잭션 범위 |
|-----------|-------------|
| 메시지 전송 | 메시지 저장 + 구독자 `lastReadMessageId` bulk update + READ 이벤트 전파 (단일 트랜잭션, WebSocket 전파는 트랜잭션 내 허용) |
| `getMessages()` 호출 | 최신 메시지 id로 `lastReadMessageId` 단건 update + READ 이벤트 전파 (단일 트랜잭션) |
| WebSocket subscribe | 최신 메시지 id로 `lastReadMessageId` 단건 update + READ 이벤트 전파 (단일 트랜잭션) |

#### 동시성 고려사항

- **구독자 일괄 업데이트**: `UPDATE ... WHERE roomId = :roomId AND userId IN :userIds` JPQL bulk update 사용
  - 영속성 컨텍스트를 우회하지만, 업데이트 직후 재조회하지 않으므로 무해
- **SessionRegistry**: `ConcurrentHashMap<Long, Set<Long>>` 기반으로 thread-safe 보장
  - Set은 `ConcurrentHashMap.newKeySet()`으로 생성

#### 도메인 이벤트

- `OpenChatReadUpdated`: 읽음 상태 변경 시 발행 → `/sub/openchat/{roomId}/read` 토픽으로 전파

---

### Non-Entity Component: OpenChatSessionRegistry

#### 책임
현재 WebSocket으로 특정 채팅방을 구독 중인 사용자 집합을 in-memory로 유지한다. DB 저장 없음.

#### 상태 구조

```
sessionRoomMap:  Map<String sessionId, Long roomId>
sessionUserMap:  Map<String sessionId, Long userId>
roomSubscribers: Map<Long roomId, Set<Long userId>>   ← 신규
```

#### 오퍼레이션

| 메서드 | 설명 |
|--------|------|
| `subscribe(sessionId, roomId, userId)` | 세 맵 동시 등록 |
| `unsubscribe(sessionId)` | 세 맵에서 sessionId 제거 |
| `getSubscriberUserIds(roomId): Set<Long>` | 해당 방 구독자 userId 집합 반환 (defensive copy) |

#### 라이프사이클

```
[서버 시작] → registry 초기화 (empty)
[SessionSubscribeEvent] → subscribe(sessionId, roomId, userId)
[SessionDisconnectEvent] → unsubscribe(sessionId)
[서버 재시작] → 모든 상태 초기화 → 클라이언트 재연결 시 re-subscribe
```

---

## 4. 애그리거트 관계도

```
OpenChatRoom --1:N--> OpenChatMessage (roomId)
OpenChatRoom --1:N--> OpenChatParticipant (roomId)
OpenChatParticipant.lastReadMessageId --> OpenChatMessage.id (soft reference)

[인프라]
OpenChatSessionRegistry (in-memory) -- roomId --> Set<userId>
OpenChatReadUpdated --발행→ SimpMessagingTemplate (/sub/openchat/{roomId}/read)
```

---

## 5. 도메인 이벤트

| 이벤트명 | 발행 주체 | 발행 시점 | 구독 주체 | 처리 내용 |
|----------|-----------|-----------|-----------|-----------|
| `OpenChatReadUpdated` | OpenChatMessageService | 읽음 상태 변경 후 | WebSocket 인프라 | `/sub/openchat/{roomId}/read`로 `{messageId, unreadCount}` 전파 |

---

## 6. 도메인 서비스

### ReadEventPublishService (또는 OpenChatMessageService 내 private 메서드)

- **책임**: 읽음 상태 변경 후 unreadCount 재계산 + READ 이벤트 WebSocket 전파
- **관여 컴포넌트**: `OpenChatParticipantRepository`, `SimpMessagingTemplate`
- **로직 요약**:
  1. `calculateUnreadCount(roomId, messageId)` 호출
  2. `messagingTemplate.convertAndSend("/sub/openchat/{roomId}/read", {messageId, unreadCount})`
- **트랜잭션 전략**: 호출자(sendMessage, getMessages, handleSubscribe) 트랜잭션에 합류

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 | 일관성 전략 | 이유 |
|------|------|------------|------|
| 메시지 전송 → 구독자 읽음 처리 | OpenChatMessage ← OpenChatParticipant | 강한 일관성 (단일 트랜잭션) | 메시지 저장과 읽음 처리가 원자적으로 완료돼야 unreadCount 오염 없음 |
| getMessages → READ 이벤트 전파 | OpenChatParticipant → WebSocket | 강한 일관성 (트랜잭션 내 전파) | 기존 sendMessage 방식과 일관성 유지 |

---

## 8. 레포지토리 인터페이스

### OpenChatParticipantRepository (신규 추가 메서드)

```
// 구독자 userId 목록으로 lastReadMessageId 일괄 업데이트 (JPQL bulk update)
updateLastReadMessageIdByRoomIdAndUserIdIn(roomId, userIds, messageId): void

// roomId의 최신 구독자 읽음 수 조회 (기존 유지)
countReadByRoomIdAndMessageId(roomId, messageId): long

// roomId 참여자 수 조회 (기존 유지)
countByRoomId(roomId): long
```

### OpenChatMessageRepository (기존 유지)

```
// 채팅방 최신 메시지 ID 조회 (기존 유지)
findLatestMessageIdByRoomId(roomId): Optional<Long>
```

---

## 9. 패키지 구조 제안

```
com.example.appcenter_project
└── domain/
    └── openChat/
        ├── entity/
        │   └── OpenChatParticipant.java        (변경 없음, lastReadMessageId 기존 필드)
        ├── dto/
        │   └── response/
        │       └── ResponseOpenChatReadEventDto.java  (신규: {messageId, unreadCount})
        ├── service/
        │   └── OpenChatMessageService.java     (수정: 구독자 bulk 읽음 처리 + READ 이벤트)
        └── repository/
            └── OpenChatParticipantRepository.java     (신규 메서드 추가)
            └── OpenChatParticipantQuerydslRepositoryImpl.java  (bulk update 구현)

global/
└── config/
    ├── OpenChatSessionRegistry.java            (신규: in-memory 구독자 관리)
    └── OpenChatWebSocketEventListener.java     (수정: SessionRegistry 사용 + READ 이벤트)
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-01: DB 스키마 변경 없이 기존 `lastReadMessageId` 컬럼 활용
- **결정**: `open_chat_participant.last_read_message_id` 컬럼은 이미 존재. Flyway 마이그레이션 불필요
- **이유**: 기존 필드가 읽음 커서 목적으로 설계됨
- **trade-off**: `lastReadMessageId`가 NULL인 경우(한 번도 읽지 않음)를 `unreadCount` 계산에서 반드시 처리해야 함 (현재 `countReadByRoomIdAndMessageId`에서 `isNotNull()` 조건으로 이미 처리 중)

### ADR-02: 구독자 일괄 업데이트에 JPQL bulk update 사용
- **결정**: `UPDATE OpenChatParticipant SET lastReadMessageId = :messageId WHERE roomId = :roomId AND userId IN :userIds`
- **이유**: 구독자 수에 비례한 N+1 쿼리 방지. 단일 쿼리로 처리
- **trade-off**: 영속성 컨텍스트 우회. 단, bulk update 후 같은 트랜잭션에서 해당 엔티티를 재조회하지 않으므로 1차 캐시 불일치 무해

### ADR-03: OpenChatSessionRegistry를 별도 Spring 컴포넌트로 분리
- **결정**: 기존 `OpenChatWebSocketEventListener`의 static map을 `OpenChatSessionRegistry` 빈으로 추출
- **이유**: `OpenChatMessageService`가 구독자 목록에 접근해야 하므로, static 접근보다 DI 방식이 테스트 가능성·교체 가능성에서 우수
- **trade-off**: 클래스 하나 추가. 멀티 서버 전환 시 인터페이스화 권장 (현재 범위 외)

### ADR-04: READ 이벤트를 별도 토픽 `/sub/openchat/{roomId}/read`로 분리
- **결정**: 기존 `/sub/openchat/{roomId}` 메시지 토픽과 READ 이벤트 토픽을 분리
- **이유**: 메시지 토픽 구독자가 READ 이벤트를 필터링하지 않아도 됨. 역할 명확 분리
- **trade-off**: 클라이언트가 두 토픽을 구독해야 함

---

## 11. 아키텍처 위험 요소

- **위험 1 — SessionRegistry 메모리 누수**: `SessionDisconnectEvent`가 정상 발생하지 않는 비정상 종료 시 구독자 정보가 registry에 잔류
  - 권고: `SimpUserRegistry` 또는 Spring WebSocket의 연결 감시 활용. 또는 heartbeat 기반 주기적 정리 고려

- **위험 2 — getMessages() 호출 후 READ 이벤트 전파 시 트랜잭션 충돌**: `@Transactional` 내부에서 `messagingTemplate.convertAndSend`를 호출하면 트랜잭션 커밋 전 이벤트가 전파될 수 있음
  - 권고: 기존 `sendMessage()`도 동일 방식으로 동작 중이므로 일관성 유지. 만약 커밋 후 전파가 필요하다면 `TransactionSynchronizationManager.registerSynchronization()` 적용

- **위험 3 — 대규모 방에서 구독자 목록 조회 성능**: `getSubscriberUserIds(roomId)` 가 `Set<Long>`을 defensive copy로 반환 시 대규모 구독자 방에서 오버헤드 가능
  - 권고: 오픈채팅 방 최대 참여 인원(`maxParticipants`) 범위 내에서는 허용 가능. 필요 시 unmodifiableSet으로 대체

---

## 12. TBD

- [ ] `OpenChatSessionRegistry`를 인터페이스로 분리하여 멀티 서버 전환 시 Redis 구현체로 교체 가능하도록 할지 (현재 범위 외)
- [ ] 이미지 메시지 전송 HTTP API(`POST /openchat/message/image`) 호출 시 READ 이벤트 전파 여부 (이 API는 WebSocket 경유하지 않으므로 `sendImageMessage` 내에서도 동일 처리 필요)
- [ ] 한 사용자가 여러 기기로 동시 접속 시 userId 단위 중복 처리 확인 (현재 `Set<userId>` 구조로 자연 중복 제거)

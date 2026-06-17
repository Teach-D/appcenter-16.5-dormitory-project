# 요구사항 명세서 — 오픈채팅 읽음 처리

## 1. 개요

- **서비스 목적**: 오픈채팅 채팅방 내 각 메시지에 안읽은 참여자 수(unreadCount)를 실시간으로 표시하고, 읽음 상태 변화를 WebSocket으로 즉시 전파
- **핵심 사용자**: 오픈채팅 채팅방 참여자(USER, DORMITORY, ADMIN)
- **범위**
  - In Scope: 메시지별 unreadCount 실시간 표시, WebSocket 구독 중 자동 읽음 처리, READ 이벤트 전파, 채팅방 목록 미읽 배지, 오프라인 사용자 입장 시 읽음 처리
  - Out of Scope: 메시지별 읽은 사람 목록 표시, 멀티 서버 분산 처리(Redis pub/sub), 읽음 확인 영수증(이중 체크 표시)

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 | 변경 여부 |
|--------|-----------|-----------|
| `OpenChatMessage` | id, roomId, senderId, content, type | 변경 없음 |
| `OpenChatParticipant` | id, roomId, userId, **lastReadMessageId**, isHost | 변경 없음 (기존 필드 활용) |
| `OpenChatRoom` | id, name, lastMessage, lastMessageAt | 변경 없음 |

### 신규 컴포넌트

| 컴포넌트 | 역할 |
|----------|------|
| `OpenChatSessionRegistry` | roomId → Set\<userId\> 를 in-memory로 관리. WebSocket 구독/해제 시 업데이트 |
| `ResponseOpenChatReadEventDto` | READ 이벤트 페이로드: `{messageId, unreadCount}` |

### 엔티티 간 관계

- `OpenChatRoom` 1 ↔ N `OpenChatParticipant`
- `OpenChatRoom` 1 ↔ N `OpenChatMessage`
- `OpenChatParticipant.lastReadMessageId` → 해당 참여자가 마지막으로 읽은 `OpenChatMessage.id`

---

## 3. 비즈니스 규칙

1. **BR-01** WebSocket으로 채팅방(`/sub/openchat/{roomId}`)을 구독 중인 사용자는 새 메시지가 저장되는 시점에 자동으로 읽음 처리된다.
   - 발신자 포함 모든 구독자의 `lastReadMessageId`를 해당 메시지 ID로 업데이트

2. **BR-02** 읽음 상태가 변경될 때마다 `/sub/openchat/{roomId}/read` 토픽으로 `{messageId, unreadCount}` READ 이벤트를 즉시 전파한다.
   - 트리거: 메시지 전송, 오프라인 사용자의 `getMessages()` 호출, WebSocket 입장(subscribe)

3. **BR-03** `unreadCount`는 `전체 참여자 수 - lastReadMessageId >= messageId 인 참여자 수`로 계산한다.
   - 메시지 전송 시점에 구독자 읽음 처리 후 계산하므로, 구독 중인 인원은 unreadCount에서 제외된다

4. **BR-04** SYSTEM 메시지(입장/퇴장 알림)도 TEXT, IMAGE 메시지와 동일하게 unreadCount 계산 및 읽음 처리 대상이다.

5. **BR-05** 오프라인 사용자가 `getMessages()` API를 호출하면, 조회된 메시지 중 가장 최신 메시지 ID로 `lastReadMessageId`를 업데이트하고, 이미 채팅방에 구독 중인 사용자들에게 READ 이벤트를 전파한다.

6. **BR-06** WebSocket 최초 구독(`SessionSubscribeEvent`) 시 채팅방의 최신 메시지 ID로 `lastReadMessageId`를 업데이트하고 READ 이벤트를 전파한다.

7. **BR-07** `OpenChatSessionRegistry`는 단일 서버 in-memory 기준으로 동작한다. 서버 재시작 시 구독 정보는 초기화되며, WebSocket 재연결 시 `SessionSubscribeEvent`로 재등록된다.

8. **BR-08** 채팅방 목록(`GET /openchat/rooms`) 응답의 `unreadCount` 필드는 해당 사용자의 `lastReadMessageId` 이후 메시지 수로 계산한다.

---

## 4. 사용자 & 권한

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` | 채팅방 구독, 메시지 조회(읽음 처리), READ 이벤트 수신 |
| `DORMITORY` | USER와 동일 |
| `ADMIN` | USER와 동일 |
| 비인증 | 없음 (WebSocket 인증 필수) |

---

## 5. 주요 시나리오

### Happy Path 1 — A, B, C 모두 구독 중, A가 메시지 전송

1. A가 `/pub/openchat/send`로 메시지 전송
2. 서버가 메시지 DB 저장
3. `OpenChatSessionRegistry`에서 해당 방 구독자 {A, B, C} 조회
4. A, B, C의 `lastReadMessageId` 일괄 업데이트
5. `unreadCount = 0` 계산
6. `/sub/openchat/{roomId}` → 메시지 + `unreadCount: 0` 브로드캐스트
7. `/sub/openchat/{roomId}/read` → `{messageId, unreadCount: 0}` 브로드캐스트

### Happy Path 2 — A, B 구독 중, C 오프라인, A가 메시지 전송

1. A, B의 `lastReadMessageId` 업데이트 → `unreadCount = 1`
2. `/sub/openchat/{roomId}` → 메시지 + `unreadCount: 1`
3. `/sub/openchat/{roomId}/read` → `{messageId, unreadCount: 1}`
4. (나중에) C가 채팅방 `getMessages()` 호출
5. C의 `lastReadMessageId` 업데이트 → `unreadCount = 0`
6. `/sub/openchat/{roomId}/read` → `{messageId, unreadCount: 0}` (A, B에게 전파)

### Happy Path 3 — C가 WebSocket으로 채팅방 재입장

1. C가 `/sub/openchat/{roomId}` 구독
2. `SessionSubscribeEvent` 발생
3. 최신 메시지 ID로 C의 `lastReadMessageId` 업데이트
4. `unreadCount` 재계산
5. `/sub/openchat/{roomId}/read` → `{messageId, newUnreadCount}` 전파

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| 채팅방에 참여자가 1명뿐일 때 메시지 전송 | `unreadCount = 0` (자기 자신만 있음) |
| 서버 재시작 후 WebSocket 재연결 | `SessionSubscribeEvent` 재발생 → `lastReadMessageId` 자동 갱신 |
| `getMessages()` 호출 시 메시지가 없는 경우 | 읽음 처리 및 READ 이벤트 전파 생략 |
| 이미 읽음 처리된 메시지의 READ 이벤트 중복 수신 | 클라이언트가 동일 값이면 무시 (idempotent) |

---

## 6. 비기능 요구사항

- **성능**: READ 이벤트 전파는 메시지 저장 트랜잭션 커밋 후 즉시 실행 (응답 지연 최소화)
- **동시성**: `OpenChatSessionRegistry`는 `ConcurrentHashMap` 기반으로 thread-safe 보장
- **데이터 보존**: `lastReadMessageId`는 기존 `open_chat_participant` 테이블 컬럼 활용, DB 스키마 변경 없음
- **외부 연동**: 없음 (단일 서버 in-memory)
- **확장성**: 멀티 서버 전환 시 Redis pub/sub으로 `OpenChatSessionRegistry` 교체 가능하도록 인터페이스 분리 권장 (이번 구현 범위 외)

---

## 7. 미결 사항 (TBD)

- [ ] 한 사용자가 여러 기기/탭으로 동일 채팅방에 동시 접속한 경우, userId 기준으로 중복 처리 여부 (현재 in-memory map은 userId Set으로 관리 → 동일 userId 중복 없음)
- [ ] 이미지 메시지 전송 HTTP API(`POST /openchat/message/image`) 응답에도 READ 이벤트를 전파할지 (WebSocket 경유하지 않으므로 별도 처리 필요)

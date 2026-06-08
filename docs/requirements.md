# 요구사항 명세서

> 기능: 오픈채팅 (Phase 1 — 채팅방 CRUD / Phase 2 — 실시간 채팅)

---

## 1. 개요

- **서비스 목적**: 기숙사생이 오픈 채팅방을 생성·입장·탈퇴하고 공개 범위에 따른 3탭 목록을 조회할 수 있는 기능 (실시간 채팅은 Phase 2)
- **핵심 사용자**: 기숙사 입주 학생 (USER)
- **범위**
  - In Scope: 채팅방 생성, 방 목록 3탭 조회(내 방 / 내 기숙사 / 전체), 방 입장, 방 나가기, 초기 9개 공식 방 Flyway 삽입
  - Out of Scope: 실시간 채팅 메시지 전송·수신 (Phase 2), 채팅 신고, 채팅방 검색

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 |
|--------|-----------|
| `OpenChatRoom` | id, name, description, scope(DORMITORY/ALL), maxParticipants, creatorDormitory(DormType), lastMessageAt, isOfficial, createdBy(User FK, nullable), createdAt |
| `OpenChatParticipant` | id, room(FK), user(FK), notificationEnabled, joinedAt |
| `OpenChatMessage` | id, room(FK), sender(FK), content, type(TEXT/IMAGE), createdAt |

### 엔티티 간 관계

- `OpenChatRoom` 1 ↔ N `OpenChatParticipant`
- `OpenChatRoom` 1 ↔ N `OpenChatMessage`
- `User` 1 ↔ N `OpenChatParticipant` (한 유저가 여러 방에 참여 가능)
- `User` 1 ↔ N `OpenChatRoom` (방 생성자, nullable — 공식 방은 NULL)

### 서비스 생성 방 식별자

- `created_by = NULL` + `is_official = TRUE` 조합이 공식 방의 식별자
- 공식 방은 방장 이전·자동 삭제 로직에서 제외, seed 유저 의존 없음

---

## 3. 비즈니스 규칙

1. **BR-01** USER만 채팅방 생성 가능
   - 위반 시: 403 FORBIDDEN

2. **BR-02** `scope = DORMITORY`인 방은 `creator_dormitory = 요청자 DormType`인 경우에만 "내 기숙사 탭" 목록에 노출
   - `DormType = NONE` 유저의 내 기숙사 탭: 빈 목록 반환 (오류 아님)

3. **BR-03** `scope = DORMITORY`인 방에 직접 입장 시 요청자 DormType ≠ creator_dormitory → 입장 차단
   - 위반 시: 403 FORBIDDEN (링크 우회 차단)

4. **BR-04** 현재 참여 인원 ≥ max_participants인 방에 입장 시도 → 입장 불가
   - 위반 시: 400 BAD_REQUEST (OPEN_CHAT_ROOM_FULL)

5. **BR-05** 이미 참여 중인 방에 재입장 요청 → 멱등 처리 (에러 없이 roomId 정상 응답)
   - 앱 재시작·탭 이동 등 중복 요청이 자연 발생하는 환경 고려

6. **BR-06** 방장이 나가기 → `joined_at` 기준 가장 오래된 참여자에게 방장 자동 이전
   - 참여자가 0명이 되면 방 자동 삭제 (단, `is_official = TRUE` 방은 삭제 보호)

7. **BR-07** `is_official = TRUE` 방은 방장 이전·자동 삭제 로직 적용 제외
   - `is_official` 방에서 방장이 나가도 삭제되지 않고 다음 참여자에게 방장 이전만 수행

8. **BR-08** 방 삭제 권한: 방장 또는 ADMIN
   - `is_official = TRUE` 방은 ADMIN만 삭제 가능
   - 위반 시: 403 FORBIDDEN

9. **BR-09** 방 목록 응답에 `isJoined` 필드 포함
   - "내 기숙사 탭"과 "전체 탭"에서 이미 참여한 방 구분 표시

---

## 4. 사용자 & 권한

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` | 방 생성(본인), 방 목록 3탭 조회, 방 입장, 방 나가기, 방 삭제(본인이 방장인 경우) |
| `ADMIN` | 방 삭제 전체 (is_official 포함) |
| 비인증 | 없음 (전체 인증 필요) |

---

## 5. 주요 시나리오

### Happy Path

1. USER가 오픈 채팅방을 생성한다 (`scope=ALL`, `maxParticipants=50`)
2. 다른 USER가 "전체 탭"에서 해당 방을 발견하고 입장한다
3. 입장 후 "내 방 탭"에 해당 방이 추가되고 `isJoined=true`로 표시된다
4. 방장(생성자)이 나가기를 요청하면 다음 참여자(joined_at 가장 오래된)에게 방장이 이전된다

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| DORM_1 유저가 `scope=DORMITORY` + `creator_dormitory=DORM_2` 방 입장 시도 | 403 FORBIDDEN (BR-03) |
| 최대 인원이 가득 찬 방에 입장 시도 | 400 OPEN_CHAT_ROOM_FULL (BR-04) |
| 이미 참여 중인 방에 재입장 요청 | 멱등 처리, roomId 정상 응답 (BR-05) |
| 유일한 참여자(방장)가 나가기 | 참여자 0명 → 방 자동 삭제 (is_official=FALSE인 경우만) |
| is_official 방에서 방장 나가기 | 방장 이전만 수행, 방 삭제 없음 (BR-07) |
| DormType=NONE 유저의 내 기숙사 탭 요청 | 빈 목록 반환 (BR-02) |

---

## 6. 비기능 요구사항

- **성능**: 목록 조회 응답 200ms 이내
- **정렬 기준**: TBD (last_message_at DESC vs 참여자 수 vs 생성 시간)
- **페이지네이션**: TBD (offset vs cursor)
- **데이터 보존**: `is_official = TRUE` 방은 영구 보존, 일반 방은 참여자 0명 시 자동 삭제
- **외부 연동**: 없음 (Phase 2에서 WebSocket 연동 예정)

---

## 7. 미결 사항 (TBD)

- [ ] 방 목록 정렬 기준: `last_message_at DESC` vs 참여자 수 DESC vs 생성 시간 DESC
- [ ] 페이지네이션 방식: offset 기반 vs cursor 기반
- [ ] 방 상세 조회 API 별도 필요 여부 (입장 응답에 상세 정보 포함 가능)
- [ ] `notification_enabled` ON/OFF 변경 API Phase 1 포함 여부
- [ ] `open_chat_message` 테이블 Phase 1에서 실제 사용 여부 (테이블 생성만, API 없음)
- [ ] 방 검색 기능 (이름 키워드) Phase 1 포함 여부

---

## 8. 초기 데이터 (Flyway 삽입 대상)

| 방 이름 | scope | creator_dormitory | is_official | created_by |
|---------|-------|-------------------|-------------|------------|
| 1긱 오픈채팅 | DORMITORY | DORM_1 | TRUE | NULL |
| 2긱 오픈채팅 | DORMITORY | DORM_2 | TRUE | NULL |
| 3긱 오픈채팅 | DORMITORY | DORM_3 | TRUE | NULL |
| 1긱 공동구매방 | DORMITORY | DORM_1 | TRUE | NULL |
| 2긱 공동구매방 | DORMITORY | DORM_2 | TRUE | NULL |
| 3긱 공동구매방 | DORMITORY | DORM_3 | TRUE | NULL |
| 기숙사 생활 질문방 | ALL | NULL | TRUE | NULL |
| 기숙사 입사 준비방 | ALL | NULL | TRUE | NULL |
| 여름방학 심심한 사람들 | ALL | NULL | TRUE | NULL |

---

---

# Phase 2 — 실시간 채팅 (WebSocket + STOMP)

## 1. 개요

- **서비스 목적**: 오픈채팅 방 참여자들이 실시간으로 메시지를 송수신하고, 채팅 내역을 커서 기반으로 조회하며, 내 채팅방 목록에서 최신 메시지 미리보기를 확인
- **핵심 사용자**: 기숙사 입주 학생 (USER)
- **범위**
  - In Scope: STOMP `@MessageMapping` 메시지 발행·브로드캐스트, 메시지 DB 저장, 시스템 메시지(입장/퇴장), 채팅 내역 커서 기반 페이징 조회, 메시지별 읽지 않은 사람 수, 내 채팅방 목록 최신 메시지 정렬·미리보기
  - Out of Scope: FCM 오프라인 알림(Phase 3), 이미지 메시지(Phase 3), 메시지 삭제·수정, 공지 고정

---

## 2. 도메인 모델 변경

### 엔티티 변경 사항

| 엔티티 | 추가 컬럼 | 비고 |
|--------|-----------|------|
| `OpenChatRoom` | `lastMessage VARCHAR(500)` | 방 목록 미리보기용 |
| `OpenChatParticipant` | `lastReadMessageId BIGINT` | 읽지 않은 메시지 수 산정용 |
| `OpenChatMessageType` | `SYSTEM` 타입 추가 | 입장/퇴장 시스템 메시지 |

### 읽지 않은 사람 수 산정 방식

메시지 M의 **읽지 않은 사람 수** = 방의 총 참여자 수 − (lastReadMessageId ≥ M.id 인 참여자 수)

### 새 컴포넌트

- `OpenChatWebSocketEventListener`: 구독 이벤트 감지, `lastReadMessageId` 갱신 (룸메이트의 `RoommateWebSocketEventListener`와 동일 패턴)
- `OpenChatMessageService`: 메시지 발행·저장·브로드캐스트 담당

---

## 3. 비즈니스 규칙 (Phase 2)

1. **BR-P2-01** 메시지 발행 엔드포인트: STOMP `/pub/openchat/socketchat` (`@MessageMapping`만 사용)
2. **BR-P2-02** 발행자는 해당 방의 `OpenChatParticipant`로 등록된 참여자여야 함
   - 위반 시: 메시지 처리 중단 (STOMP ERROR 프레임 또는 무시)
3. **BR-P2-03** 메시지 저장 후 방 구독자 전체에 브로드캐스트 (`/sub/openchat/{roomId}`)
4. **BR-P2-04** 메시지 저장 후 `OpenChatRoom.lastMessage`(내용 최대 500자 truncate), `lastMessageAt` 즉시 갱신
5. **BR-P2-05** 참여자가 `/sub/openchat/{roomId}` 구독 시 (`SessionSubscribeEvent`) `lastReadMessageId`를 현재 방의 최신 메시지 ID로 갱신
6. **BR-P2-06** 메시지 발송 후 발신자의 `lastReadMessageId`도 해당 메시지 ID로 갱신
7. **BR-P2-07** 입장 시 SYSTEM 메시지(`{닉네임}님이 입장했습니다`) 저장 + `/sub/openchat/{roomId}` 브로드캐스트
   - Phase 1의 `joinRoom()` 서비스에서 직접 발행
8. **BR-P2-08** 퇴장 시 SYSTEM 메시지(`{닉네임}님이 퇴장했습니다`) 저장 + 브로드캐스트
   - Phase 1의 `leaveRoom()` 서비스에서 직접 발행
9. **BR-P2-09** 채팅 내역 조회: 커서 기반 페이징, 기본 30건, 오래된 방향(ID 오름차순)으로 반환
   - `lastMessageId` 미전달 시 가장 최신 30건 반환
   - 조회 후 해당 참여자의 `lastReadMessageId` 갱신 (응답에 포함된 최신 메시지 ID로)
10. **BR-P2-10** 내 채팅방 목록(MY 탭): `lastMessageAt` DESC 정렬, 응답에 `lastMessage`, `lastMessageAt`, `unreadCount` 포함
    - `unreadCount` = 방의 메시지 중 id > 내 `lastReadMessageId` 인 건수
11. **BR-P2-11** 채팅 내역 조회는 해당 방의 참여자만 가능
    - 위반 시: 403 FORBIDDEN

---

## 4. 사용자 & 권한 (Phase 2)

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` (참여자) | WebSocket 메시지 발행, 채팅 내역 조회, 내 방 목록 조회 |
| `USER` (비참여자) | 채팅 내역 조회 불가, 메시지 발행 불가 |
| 비인증 | WebSocket 연결 거부 (WebSocketAuthInterceptor) |

---

## 5. 주요 시나리오 (Phase 2)

### Happy Path — 메시지 발행

1. USER가 STOMP CONNECT → JWT 검증 → 세션에 `userId` 저장
2. `/sub/openchat/{roomId}` 구독 → `lastReadMessageId` 갱신
3. `/pub/openchat/socketchat`으로 메시지 발행 (`roomId`, `content` 포함)
4. 서버: 참여자 검증 → `OpenChatMessage` 저장 → `lastMessage`, `lastMessageAt` 갱신 → 발신자 `lastReadMessageId` 갱신
5. `/sub/openchat/{roomId}` 전체 구독자에게 메시지 응답 브로드캐스트

### Happy Path — 채팅 내역 조회

1. `GET /openchat/{roomId}/messages?size=30` (첫 조회)
2. 참여자 검증 후 최신 30건 반환
3. 응답 후 `lastReadMessageId` 갱신
4. 이전 메시지 조회: `GET /openchat/{roomId}/messages?lastMessageId={id}&size=30`

### Happy Path — 내 채팅방 목록(MY 탭)

1. `GET /openchat/rooms?tab=MY`
2. 참여 중인 방을 `lastMessageAt` DESC 정렬
3. 각 방의 `lastMessage`, `lastMessageAt`, `unreadCount`(내 lastReadMessageId 기준) 포함 응답

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| WebSocket 미인증 발행 | 세션 userId 없음 → 처리 중단 |
| 참여자가 아닌 사용자의 발행 | 참여자 검증 실패 → 처리 중단 |
| 존재하지 않는 roomId로 발행 | 방 조회 실패 → 처리 중단 |
| lastMessageId가 실제 존재하지 않는 경우 | 해당 ID 미만 최신 30건 반환 |
| 방 삭제 후 구독자가 메시지 수신 | 브로드캐스트 수신 없음 (정상) |

---

## 6. 비기능 요구사항 (Phase 2)

- **성능**: 채팅 내역 조회 200ms 이내, 커서 기반 인덱스(`room_id, id DESC`) 활용
- **WebSocket**: 기존 `/ws-stomp` 엔드포인트 재사용 (설정 변경 없음)
- **세션 관리**: `OpenChatWebSocketEventListener` 신규 생성, prefix `/sub/openchat/` 로 분리 관리
- **데이터 보존**: 메시지 hard delete 없음, 방 삭제 시 메시지 cascade 삭제

---

## 7. 미결 사항 (Phase 2 TBD)

- [ ] 오프라인 참여자 FCM 알림 발송 (Phase 3)
- [ ] 이미지 메시지 지원 (Phase 3)
- [ ] 메시지 최대 길이 제한 (현재 TEXT 타입, 필요 시 500자 논의)
- [ ] 입장/퇴장 시스템 메시지의 `unreadCount` 포함 여부

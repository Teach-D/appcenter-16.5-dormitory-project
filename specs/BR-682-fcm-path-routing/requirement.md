# BR-682 FCM path 라우팅 및 채팅 알림 지연 해소

## 기능 요약

FCM 푸시 data 페이로드에 앱 내 이동 경로(`path`) 필드를 추가하고, iOS 알림 센터 그룹화를 위한 `apns.thread-id`를 타입별로 동적 지정한다.
채팅 알림의 최대 30초 발송 지연을 5초 이내로 단축한다.

---

## 동작 명세

### 1. FcmRoutingType 확장

기존 `NOTICE`, `CHAT` 두 가지를 아래와 같이 분리·확장한다.

| 신규 타입       | 대체하는 기존 타입 | thread-id 값         | data.path 값                |
|----------------|------------------|----------------------|-----------------------------|
| `CHAT_OPEN`    | `CHAT`           | `chat_room_{id}`     | `/chat/open/{id}`           |
| `CHAT_PERSONAL`| (신규)            | `chat_room_{id}`     | `/chat/open/{id}`           |
| `ANNOUNCEMENT` | `NOTICE`         | `notice` (고정)       | `/announcements/{id}`       |
| `COMPLAINT`    | (신규)            | `complaint` (고정)    | `/complain/{id}`            |
| `ROOMMATE_POST`| (신규)            | `roommate` (고정)     | `/roommate/list/{id}`       |

각 열거값에 `path(Long id)` 메서드를 추가한다. 기존 `threadId(Long id)`, `dataKey()`, `dataType()` 메서드는 유지한다.

### 2. 채팅방 타입별 routing 선택

`OpenChatNotificationService`에서 FCM Outbox를 생성할 때:
- `OpenChatRoom.roomType == PERSONAL` → `FcmRoutingType.CHAT_PERSONAL`
- `OpenChatRoom.roomType == OPEN | DERIVED` → `FcmRoutingType.CHAT_OPEN`

`sendImmediateNotifications(roomId, …)` 와 `sendHourlyUnreadNotifications()` 모두 적용.
`sendHourlyUnreadNotifications()`에서 roomId → `OpenChatRoom` 조회가 이미 있으므로 roomType을 꺼내면 된다.

### 3. FCM 페이로드 변경

`FcmAsyncSender.sendOutboxBatch()` 에서 `routingType != null && routingId != null` 일 때:
```
data.path   = routingType.path(routingId)      // 신규 추가
data.type   = routingType.dataType()           // 기존 유지 (하위 호환)
data.[dataKey] = String.valueOf(routingId)     // 기존 유지 (하위 호환)
apns.thread-id = routingType.threadId(routingId)  // 기존 유지
```

`routingType` 또는 `routingId`가 null이면 path·thread-id 없이 기본 알림만 발송한다 (기존 동작 유지).

### 4. 채팅 알림 30초 지연 해소

`FcmOutboxProcessor.process()`의 `@Scheduled(fixedDelay = 30_000)` → `fixedDelay = 5_000`으로 변경.
EVERY 모드 즉시 알림 기준 최대 5초 이내 발송을 목표로 한다.

---

## 도메인 데이터

- **FcmRoutingType** (enum): CHAT → CHAT_OPEN + CHAT_PERSONAL로 분리, NOTICE → ANNOUNCEMENT로 이름 변경, COMPLAINT·ROOMMATE_POST 신규 추가
- **FcmOutbox** 엔티티: 변경 없음 (`routingType`, `routingId` 기존 컬럼 활용)
- **DB 스키마**: 변경 없음

### 기존 코드 교체 대상

| 파일 | 기존 사용 | 교체 후 |
|------|----------|--------|
| `OpenChatNotificationService` | `FcmRoutingType.CHAT` | roomType 보고 `CHAT_OPEN` / `CHAT_PERSONAL` |
| (공지 발송 코드 — 추후 확인) | `FcmRoutingType.NOTICE` | `ANNOUNCEMENT` |

---

## 비즈니스 규칙 / 제약

- `FcmRoutingType` 이름 변경(`CHAT`→`CHAT_OPEN`, `NOTICE`→`ANNOUNCEMENT`)으로 기존 참조 코드가 컴파일 오류를 낸다 — 빠짐없이 교체해야 한다.
- DERIVED 방은 OPEN과 동일하게 `CHAT_OPEN`으로 취급한다.
- `data.type` 값(문자열)은 기존 클라이언트 하위 호환을 위해 유지한다. `CHAT_OPEN` → `"CHAT_OPEN"`, `ANNOUNCEMENT` → `"ANNOUNCEMENT"` 등 enum name() 그대로 사용.
- DB에 이미 저장된 `FcmOutbox` 레코드의 `routingType` 컬럼 값이 `CHAT`, `NOTICE`인 경우 발송 시 path를 생성할 수 없다 → `null` 처리로 기존 동작 유지 (마이그레이션 불필요).

---

## 예외 · 경계 상황

- `routingId`가 null인데 `path(null)` 호출 → `routingType`·`routingId` null 체크를 호출 전에 하므로 발생하지 않는다.
- `sendHourlyUnreadNotifications()`에서 roomId에 해당하는 `OpenChatRoom`이 조회되지 않는 경우 → 현재 코드에서 `roomNameMap.getOrDefault(...)` 사용 중, roomType 조회 시에도 같은 Map을 활용해 없으면 `CHAT_OPEN` 기본값 사용.

---

## 비목표 (Non-goals)

- `path` 필드를 DB 컬럼으로 저장 (enum 메서드로 발송 시점에 동적 생성)
- 민원·룸메이트 게시글·공지 FCM 발송 로직 신규 구현 (기존 발송 코드에 routingType 전달 추가는 별도 작업)
- Android 그룹화 추가 구현 (기존 `android.tag` 처리 코드 그대로)
- 1초 미만 즉시 발송 (5초 이내를 목표)
- `sendOne()` · `sendBatch()` 메서드에 path 추가 (routing 정보 없이 호출하는 전체 발송 경로)

---

## 수용 기준 (Acceptance Criteria)

| # | Given | When | Then |
|---|-------|------|------|
| AC-1 | `routingType = CHAT_OPEN`, `routingId = 1234` | `sendOutboxBatch` 호출 | FCM data에 `path = "/chat/open/1234"` 포함 |
| AC-2 | `routingType = CHAT_PERSONAL`, `routingId = 99` | `sendOutboxBatch` 호출 | FCM data에 `path = "/chat/roommate/99"` 포함 |
| AC-3 | `routingType = ANNOUNCEMENT`, `routingId = 7` | `sendOutboxBatch` 호출 | FCM data에 `path = "/announcements/7"`, `apns.thread-id = "notice"` 포함 |
| AC-4 | `routingType = COMPLAINT`, `routingId = 5` | `sendOutboxBatch` 호출 | FCM data에 `path = "/complain/5"` 포함 |
| AC-5 | `routingType = ROOMMATE_POST`, `routingId = 3` | `sendOutboxBatch` 호출 | FCM data에 `path = "/roommate/list/3"` 포함 |
| AC-6 | `routingType = null` | `sendOutboxBatch` 호출 | FCM data에 `path` 키 없음 |
| AC-7 | `OpenChatRoom.roomType = PERSONAL` | `sendImmediateNotifications` 호출 | 생성된 `FcmOutbox.routingType = CHAT_PERSONAL` |
| AC-8 | `OpenChatRoom.roomType = OPEN` | `sendImmediateNotifications` 호출 | 생성된 `FcmOutbox.routingType = CHAT_OPEN` |
| AC-9 | `FcmOutboxProcessor` | 애플리케이션 구동 후 Outbox 적재 | `fixedDelay = 5000` ms 이내에 다음 처리 사이클 시작 |

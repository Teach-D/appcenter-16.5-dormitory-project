# BR-682 FCM path 라우팅 및 채팅 알림 지연 해소 — 도메인 설계

---

## 엔티티 / 값 객체

### FcmRoutingType (enum 교체)

기존 `NOTICE`, `CHAT` 두 값을 제거하고 아래 5개로 대체한다.

| 값             | threadId(Long id)      | path(Long id)              | dataKey()        |
|---------------|------------------------|----------------------------|------------------|
| `CHAT_OPEN`   | `"chat_room_" + id`    | `"/chat/open/" + id`       | `"chatRoomId"`   |
| `CHAT_PERSONAL`| `"chat_room_" + id`   | `"/chat/open/" + id`       | `"chatRoomId"`   |
| `ANNOUNCEMENT`| `"notice"`             | `"/announcements/" + id`   | `"noticeId"`     |
| `COMPLAINT`   | `"complaint"`          | `"/complain/" + id`        | `"complaintId"`  |
| `ROOMMATE_POST`| `"roommate"`          | `"/roommate/list/" + id`   | `"roommatePostId"`|

- `dataType()`: `this.name()` 그대로 (클라이언트 하위 호환 — 새 enum name 값 그대로 전달)
- 기존 `ANNOUNCEMENT`의 `threadId`는 `"notice"` 고정 (구분자 없음). 기존 `NOTICE`는 `"notice_" + id`였으나 명세에서 변경 지정.

### FcmRoutingTypeConverter (신규)

DB 컬럼에 남아있는 구 값(`"CHAT"`, `"NOTICE"`)을 읽을 때 `IllegalArgumentException` 없이 `null`을 반환하는 `AttributeConverter<FcmRoutingType, String>`.
`FcmOutbox.routingType` 필드의 `@Enumerated(EnumType.STRING)` 어노테이션을 `@Convert(converter = FcmRoutingTypeConverter.class)`로 교체한다.

---

## 애그리거트 경계

- `FcmOutbox` 단독 애그리거트. `routingType` · `routingId`는 FK 없는 숫자 참조 — 엔티티 참조 없음.
- 기존 경계 변경 없음.

---

## 연관관계

신규 연관관계 없음. 기존 엔티티 참조 구조 유지.

---

## DB 스키마 변경

없음.

- `fcm_outbox.routing_type` 컬럼은 `VARCHAR(20)`, 신규 enum 값 최대 길이 `ROOMMATE_POST` (13자) ← 20자 이내.
- 기존 `"CHAT"`, `"NOTICE"` 레코드는 `FcmRoutingTypeConverter`가 `null`로 처리하여 path 없이 기본 알림 발송.

---

## 도메인 계층 구조

```
domain/fcm/
├── entity/
│   └── FcmOutbox.java                     ← @Enumerated → @Convert 교체
├── enums/
│   └── FcmRoutingType.java                ← 값 교체 + path() 메서드 추가
├── converter/                             ← [신규 패키지]
│   └── FcmRoutingTypeConverter.java       ← [신규] unknown 값 → null
└── service/
    ├── FcmAsyncSender.java                ← sendOutboxBatch()에 data.path 추가
    └── FcmOutboxProcessor.java            ← fixedDelay 30_000 → 5_000

domain/openChat/service/
└── OpenChatNotificationService.java       ← roomType 분기 로직 (CHAT → CHAT_OPEN/CHAT_PERSONAL)

domain/notification/service/
├── AnnouncementNotificationService.java   ← NOTICE → ANNOUNCEMENT
└── NotificationReadService.java           ← NOTICE → ANNOUNCEMENT 참조 변경
```

### 신규 생성 클래스

| 클래스 | 위치 |
|-------|------|
| `FcmRoutingTypeConverter` | `domain/fcm/converter/` |

### 수정할 기존 클래스

| 클래스 | 변경 내용 |
|-------|----------|
| `FcmRoutingType` | 값 교체, `path(Long id)` 추가, `threadId` / `dataKey` 수정 |
| `FcmOutbox` | `@Enumerated(EnumType.STRING)` → `@Convert(converter = FcmRoutingTypeConverter.class)` |
| `FcmAsyncSender` | `sendOutboxBatch()`: `putData("path", rt.path(rid))` 추가 |
| `FcmOutboxProcessor` | `@Scheduled(fixedDelay = 30_000)` → `fixedDelay = 5_000` |
| `OpenChatNotificationService` | `sendImmediateNotifications` 시그니처에 `OpenChatRoomType roomType` 추가; `sendHourlyUnreadNotifications`에 roomTypeMap 추가; 두 메서드 모두 roomType 분기로 `CHAT_OPEN` / `CHAT_PERSONAL` 선택 |
| `OpenChatMessageService` | `sendImmediateNotifications` 호출 2곳에 `room.getRoomType()` 추가 전달 |
| `AnnouncementNotificationService` | `FcmRoutingType.NOTICE` → `FcmRoutingType.ANNOUNCEMENT` |
| `NotificationReadService` | `FcmRoutingType.NOTICE` → `FcmRoutingType.ANNOUNCEMENT` |

---

## 핵심 설계 결정

### sendImmediateNotifications 시그니처 변경

`OpenChatMessageService.sendMessage` / `sendImageMessage` 두 호출부에서 이미 `OpenChatRoom room` 객체를 보유하고 있다. 시그니처에 `OpenChatRoomType roomType`을 추가해 `room.getRoomType()`을 전달하면 `OpenChatNotificationService` 안에서 추가 DB 쿼리 없이 처리 가능.

```
// 변경 전
sendImmediateNotifications(Long roomId, Set<Long> onlineUserIds, String title, String body)

// 변경 후
sendImmediateNotifications(Long roomId, OpenChatRoomType roomType,
                           Set<Long> onlineUserIds, String title, String body)
```

### sendHourlyUnreadNotifications roomType 조회

기존 `openChatRoomRepository.findAllById(roomIds)`로 roomNameMap을 만드는 코드를 확장해 roomTypeMap도 함께 생성. 별도 쿼리 없음.

```
Map<Long, OpenChatRoomType> roomTypeMap = openChatRoomRepository.findAllById(roomIds).stream()
    .collect(Collectors.toMap(OpenChatRoom::getId, OpenChatRoom::getRoomType));
```

`roomTypeMap`에 없는 roomId는 `CHAT_OPEN` 기본값 사용 (명세 §예외·경계 상황).

### FcmAsyncSender path 삽입 위치

기존 `putData("type", ...)` 바로 앞에 추가:

```java
if (rt != null && rid != null) {
    builder
        .putData("path", rt.path(rid))    // 신규
        .putData("type", rt.dataType())   // 기존 유지
        .putData(rt.dataKey(), String.valueOf(rid))  // 기존 유지
        .setApnsConfig(...)
        .setAndroidConfig(...);
}
```

---

## 비목표

- `path` 필드를 `FcmOutbox` 엔티티 컬럼으로 추가하지 않는다.
- 민원·룸메이트·공지 FCM 발송 로직 신규 구현 없음 (`AnnouncementNotificationService.NOTICE → ANNOUNCEMENT` 교체만).
- Android 그룹화 추가 구현 없음.
- `sendOne()` · `sendBatch()` 메서드에 path 추가 없음.
- `FcmOutboxProcessor.cleanup()` 주기 변경 없음 (`process()`만 변경).
- 구 enum 값(`CHAT`, `NOTICE`)에 대한 DB 마이그레이션 없음 (Converter로 null 처리).

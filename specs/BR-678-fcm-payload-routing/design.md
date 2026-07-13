# BR-678 — FCM 페이로드 라우팅 설계

## 엔티티 / 값 객체

### FcmOutbox (기존 수정)

기존 필드 유지. 아래 두 필드를 추가한다.

| 필드 | 타입 | 어노테이션 | 설명 |
|------|------|-----------|------|
| `routingType` | `FcmRoutingType` | `@Enumerated(STRING)`, nullable | `NOTICE` / `CHAT` / null |
| `routingId` | `Long` | nullable | 공지 ID 또는 채팅방 ID |

팩토리 메서드 오버로드:

```java
// 기존 — routing 없는 경우 (그대로 유지)
public static FcmOutbox create(String token, String title, String body)

// 신규 — routing 있는 경우
public static FcmOutbox create(String token, String title, String body,
                               FcmRoutingType routingType, Long routingId)
```

기존 `create(token, title, body)`는 삭제하지 않는다. 내부적으로 `create(token, title, body, null, null)`를 위임하거나 독립 유지.

### FcmRoutingType (신규 enum)

```java
public enum FcmRoutingType {
    NOTICE, CHAT;

    public String threadId(Long id) {
        return switch (this) {
            case NOTICE -> "notice_" + id;
            case CHAT   -> "chat_room_" + id;
        };
    }

    public String dataKey() {
        return switch (this) {
            case NOTICE -> "noticeId";
            case CHAT   -> "chatRoomId";
        };
    }

    public String dataType() {
        return this.name(); // "NOTICE" 또는 "CHAT"
    }
}
```

FCM 페이로드 조립에 필요한 prefix/key 로직을 enum 안에 캡슐화해 `FcmAsyncSender`에서 switch 없이 호출한다.

## 애그리거트 경계

`FcmOutbox`는 단독 애그리거트 루트. routing 필드는 값(Long ID, enum)이므로 별도 연관 엔티티 불필요.

## 연관관계

변경 없음. `FcmOutbox`는 외래키 없이 token 문자열과 routing 값만 보유한다.

## DB 스키마 변경

```sql
ALTER TABLE fcm_outbox
    ADD COLUMN routing_type VARCHAR(20) NULL,
    ADD COLUMN routing_id   BIGINT      NULL;
```

- 인덱스 추가 없음 — routing 필드는 조회 필터 아닌 페이로드 조립용.
- Flyway/Liquibase 스크립트는 이번 범위 밖 (Non-goal).

## 도메인 계층 구조

```
domain/fcm/
├── entity/
│   └── FcmOutbox.java            ← 수정 (routingType, routingId 필드 + create 오버로드)
├── enums/
│   ├── OutboxStatus.java         ← 변경 없음
│   └── FcmRoutingType.java       ← 신규
└── service/
    ├── FcmAsyncSender.java       ← 수정 (sendOutboxBatch 내부 — APNS/Android/data 조립)
    ├── FcmOutboxProcessor.java   ← 수정 (그룹화 키에 routingType+routingId 추가)
    └── FcmMessageService.java    ← 변경 없음

domain/notification/
└── service/
    └── AnnouncementNotificationService.java  ← 수정 (bulkEnqueueOutbox에 announcementId 전달)

domain/openChat/
└── service/
    └── OpenChatNotificationService.java      ← 수정 (FcmOutbox.create에 CHAT routing 전달)
```

## 변경 상세

### FcmOutbox.java

```java
@Enumerated(EnumType.STRING)
@Column(length = 20)
private FcmRoutingType routingType; // nullable

private Long routingId; // nullable

public static FcmOutbox create(String token, String title, String body,
                               FcmRoutingType routingType, Long routingId) {
    FcmOutbox outbox = new FcmOutbox();
    outbox.token = token;
    outbox.title = title;
    outbox.body = body;
    outbox.routingType = routingType;
    outbox.routingId = routingId;
    outbox.status = OutboxStatus.PENDING;
    outbox.retryCount = 0;
    outbox.maxRetry = 3;
    outbox.nextRetryAt = LocalDateTime.now();
    outbox.expiredAt = LocalDateTime.now().plusHours(TTL_HOURS);
    return outbox;
}
```

### FcmAsyncSender.java — sendOutboxBatch 내부

배치 내 outbox는 그룹화 보장으로 동일한 routingType + routingId를 가진다. `batch.get(0)`에서 읽어 MulticastMessage 빌더에 적용한다.

```java
// routing 있는 경우만 APNS/Android/data 설정
FcmOutbox rep = batch.get(0);
FcmRoutingType rt = rep.getRoutingType();
Long rid = rep.getRoutingId();

MulticastMessage.Builder builder = MulticastMessage.builder()
    .addAllTokens(tokens)
    .setNotification(Notification.builder().setTitle(title).setBody(body).build());

if (rt != null && rid != null) {
    String groupKey = rt.threadId(rid);
    builder
        .setApnsConfig(ApnsConfig.builder()
            .setAps(Aps.builder().setSound("default").setThreadId(groupKey).build())
            .build())
        .setAndroidConfig(AndroidConfig.builder()
            .setNotification(AndroidNotification.builder()
                .setSound("default").setTag(groupKey).build())
            .build())
        .putData("type", rt.dataType())
        .putData(rt.dataKey(), String.valueOf(rid));
}

MulticastMessage message = builder.build();
```

### FcmOutboxProcessor.java — 그룹화 키

```java
// 변경 전
Collectors.groupingBy(o -> o.getTitle() + "\0" + o.getBody())

// 변경 후
Collectors.groupingBy(o -> o.getTitle() + "\0" + o.getBody()
    + "\0" + o.getRoutingType() + "\0" + o.getRoutingId())
```

null은 `String.valueOf(null)` → `"null"` 문자열로 자동 직렬화되므로 routing 없는 그룹과 섞이지 않는다.

### AnnouncementNotificationService.java

`sendNotification(announcement, notificationType)` → `bulkEnqueueOutbox` 호출 시 `announcement.getId()` 전달.

```java
// 변경 전
private void bulkEnqueueOutbox(List<User> users, String title, String body)

// 변경 후
private void bulkEnqueueOutbox(List<User> users, String title, String body, Long announcementId)
// 내부: FcmOutbox.create(token, title, body, FcmRoutingType.NOTICE, announcementId)
```

호출 체인: `sendNotification` → `sendMessagesTo(…, announcement.getId())` → `bulkEnqueueOutbox(…, announcementId)`.

### OpenChatNotificationService.java

**sendHourlyUnreadNotifications()**:
```java
// 변경 전
FcmOutbox.create(token, roomName, body)

// 변경 후
FcmOutbox.create(token, roomName, body, FcmRoutingType.CHAT, info.roomId())
```

**sendImmediateNotifications()**:
```java
// 변경 전
FcmOutbox.create(tokenMap.get(userId), title, body)

// 변경 후
FcmOutbox.create(tokenMap.get(userId), title, body, FcmRoutingType.CHAT, roomId)
```

## 비목표

- `FcmAsyncSender.sendOne()`, `sendBatch()` 변경 없음 — 직접 발송 경로이며 routing 대상 아님.
- `FcmMessageService.sendNotification*` 계열 변경 없음.
- `sendNotificationToAllUsers` 변경 없음.
- 인앱 알림(`UserNotification`) 스키마 변경 없음.
- Flyway/Liquibase 마이그레이션 스크립트.

# BR-672 채팅 FCM 알림 모드 설정 — Design

## 엔티티 / 값 객체

### ChatNotificationMode (신규 enum)

```
EVERY   — 메시지 수신 시 즉시 FCM
BUNDLED — 매시 정각 묶음 FCM
OFF     — FCM 없음
```

`@Enumerated(EnumType.STRING)` 으로 저장.

### OpenChatParticipant (수정)

| 변경 | 필드 | 타입 | 제약 |
|------|------|------|------|
| 제거 | `notificationEnabled` | boolean | — |
| 추가 | `notificationMode` | `ChatNotificationMode` | NOT NULL, default `EVERY` |

메서드 변경:
- 제거: `updateNotificationEnabled(boolean enabled)`
- 추가: `updateNotificationMode(ChatNotificationMode mode)`
- 기존 `create(...)` 팩토리 3종: `notificationEnabled = true` → `notificationMode = ChatNotificationMode.EVERY`

## 애그리거트 경계

`OpenChatParticipant`는 독립 애그리거트 루트.
`roomId`, `userId`는 ID 참조 (기존과 동일).

## 연관관계

변경 없음. 기존 연관관계 그대로 유지.

## DB 스키마 변경

```sql
-- 1. 새 컬럼 추가
ALTER TABLE open_chat_participant
    ADD COLUMN notification_mode VARCHAR(10) NOT NULL DEFAULT 'EVERY';

-- 2. 기존 데이터 마이그레이션
UPDATE open_chat_participant
    SET notification_mode = CASE
        WHEN notification_enabled = true THEN 'EVERY'
        ELSE 'OFF'
    END;

-- 3. 기존 컬럼 제거
ALTER TABLE open_chat_participant DROP COLUMN notification_enabled;
```

인덱스: 추가 불필요. `roomId` 조건은 기존 인덱스 활용.

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatRoomController.java          [수정] updateNotification() 파라미터 변경
│   └── OpenChatRoomApiSpecification.java    [수정] 동일
├── service/
│   ├── OpenChatRoomService.java             [수정] updateNotificationMode() 시그니처 변경
│   ├── OpenChatNotificationService.java     [수정] sendImmediateNotifications() 추가, BUNDLED 쿼리 조건 교체
│   └── OpenChatMessageService.java          [수정] sendMessage/sendImageMessage 에서 즉시 FCM 호출 추가
├── repository/
│   ├── OpenChatParticipantRepository.java         [수정] findAllByRoomIdAndNotificationMode() 추가
│   ├── OpenChatParticipantQuerydslRepository.java  [수정] findUnreadCountsForNotification() 시그니처 유지, 구현 변경
│   └── OpenChatParticipantQuerydslRepositoryImpl  [수정] 쿼리 조건 notificationEnabled → notificationMode = BUNDLED
├── entity/
│   └── OpenChatParticipant.java             [수정] 위 엔티티 변경 적용
├── dto/
│   └── request/
│       └── RequestUpdateNotificationModeDto.java  [신규]
└── enums/
    └── ChatNotificationMode.java            [신규]
```

### 주요 변경 흐름

**설정 변경 API**
```
Controller.updateNotification(roomId, dto.mode)
  → OpenChatRoomService.updateNotificationMode(userId, roomId, mode)
    → participant.updateNotificationMode(mode)
```

**EVERY 즉시 발송** (`sendMessage` / `sendImageMessage` 변경)
```
OpenChatMessageService.sendMessage(...)
  → (TEXT/IMAGE 저장 후)
  → OpenChatNotificationService.sendImmediateNotifications(
        roomId, onlineUserIds=sessionRegistry+sender, title, body)
    → participantRepository.findAllByRoomIdAndNotificationMode(roomId, EVERY)
    → userId NOT IN onlineUserIds 필터
    → fcmTokenRepository.findAllByUserIdIn(targetUserIds)
    → fcmOutboxRepository.saveAll(...)
```

`onlineUserIds`는 기존 `usersToRead` (sessionRegistry 연결자 + 발신자) 재사용.
SYSTEM 메시지는 `sendImmediateNotifications` 호출 안 함.

**BUNDLED 묶음 발송** (기존 스케줄러 유지)
```
OpenChatNotificationScheduler → OpenChatNotificationService.sendHourlyUnreadNotifications()
  → findUnreadCountsForNotification()  (notificationMode = BUNDLED 조건으로 교체)
```

### 신규 클래스 상세

**`ChatNotificationMode.java`**
```java
public enum ChatNotificationMode {
    EVERY, BUNDLED, OFF
}
```

**`RequestUpdateNotificationModeDto.java`**
```java
public class RequestUpdateNotificationModeDto {
    @NotNull
    private ChatNotificationMode mode;
}
```

**`findAllByRoomIdAndNotificationMode` (JPA 파생 쿼리)**
```java
List<OpenChatParticipant> findAllByRoomIdAndNotificationMode(Long roomId, ChatNotificationMode mode);
```

**`findUnreadCountsForNotification()` QueryDSL 조건 변경**
```java
// Before
.where(openChatParticipant.notificationEnabled.isTrue())
// After
.where(openChatParticipant.notificationMode.eq(ChatNotificationMode.BUNDLED))
```

## 비목표

- 현재 알림 모드 값을 참여자 목록 DTO에 노출하지 않음 (`ResponseOpenChatParticipantDto` 변경 없음)
- 전역(계정 단위) 알림 설정 신규 추가 없음
- EVERY 모드 FCM dedup 로직 없음

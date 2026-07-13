# BR-678 — FCM 페이로드 OS별 그룹화 필드 및 앱 내 라우팅 data 추가

## 기능 요약

FCM 푸시 알림 발송 시 OS별 알림 그룹화 제어 필드(`apns.thread-id`, `android.tag`)와 앱 내 상세화면 이동용 `data` payload를 페이로드에 포함한다. 공지사항은 공지별 개별 노출, 채팅은 채팅방별 묶음 표시가 목표다.

## 동작 명세

### 공지사항 알림 (`AnnouncementNotificationService`)

1. `sendDormitoryNotifications` / `sendSupportersNotifications` / `sendUnidormNotifications` 호출 시
2. `bulkEnqueueOutbox()`에서 `FcmOutbox.create(token, title, body, NOTICE, announcement.getId())` 호출
3. `FcmOutboxProcessor`가 Outbox를 꺼낼 때 `(title, body, routingType, routingId)`로 그룹화
4. `FcmAsyncSender.sendOutboxBatch()` 호출 시 routing 있으면 다음 필드 포함:
   - APNS: `thread-id = "notice_{routingId}"`
   - Android: `tag = "notice_{routingId}"`
   - data: `{"type": "NOTICE", "noticeId": "{routingId}"}`

### 채팅 즉시 알림 — EVERY 모드 (`OpenChatNotificationService.sendImmediateNotifications`)

1. 메시지 수신 시 EVERY 모드 참가자에게 발송 경로 진입
2. `FcmOutbox.create(token, title, body, CHAT, roomId)` 호출
3. FCM 발송 시:
   - APNS: `thread-id = "chat_room_{routingId}"`
   - Android: `tag = "chat_room_{routingId}"`
   - data: `{"type": "CHAT", "chatRoomId": "{routingId}"}`

### 채팅 묶음 알림 — BUNDLED 모드 (`OpenChatNotificationService.sendHourlyUnreadNotifications`)

1. 시간마다 미읽음 정보(`UnreadNotificationInfo`)를 조회해 발송
2. 각 `info.roomId()`를 routingId로 해 `FcmOutbox.create(token, title, body, CHAT, info.roomId())` 호출
3. FCM 발송 시 위와 동일한 CHAT 필드 구성

### routing 없는 발송 (기존 유지)

`sendNotificationToAllUsers`, `sendNotification*` 계열(FcmMessageService)은 routing 없이 기존 방식대로 발송. `FcmOutbox.create(token, title, body)` 시그니처는 유지.

## 도메인 데이터

### FcmOutbox (기존 컬럼 유지, 추가)

| 컬럼 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `routing_type` | VARCHAR(20) | NULLABLE | `NOTICE` \| `CHAT` \| null(generic) |
| `routing_id` | BIGINT | NULLABLE | 공지 ID 또는 채팅방 ID |

### FcmRoutingType (신규 enum)

```
NOTICE  — 공지사항, data.type = "NOTICE", data.noticeId
CHAT    — 채팅, data.type = "CHAT", data.chatRoomId
```

### FCM 페이로드 구조 (공지사항 예시)

```json
{
  "apns": { "payload": { "aps": { "sound": "default", "thread-id": "notice_5678" } } },
  "android": { "notification": { "sound": "default", "tag": "notice_5678" } },
  "data": { "type": "NOTICE", "noticeId": "5678" }
}
```

### FCM 페이로드 구조 (채팅 예시)

```json
{
  "apns": { "payload": { "aps": { "sound": "default", "thread-id": "chat_room_1234" } } },
  "android": { "notification": { "sound": "default", "tag": "chat_room_1234" } },
  "data": { "type": "CHAT", "chatRoomId": "1234" }
}
```

## 비즈니스 규칙 / 제약

- `routingType`이 null이면 APNS, Android config, data 필드를 추가하지 않는다 (기존 동작 유지).
- `routingType`이 있으면 `routingId`도 반드시 존재해야 한다 (not null 쌍).
- `FcmOutboxProcessor` 그룹화 키: `title + "\0" + body + "\0" + routingType + "\0" + routingId`. routing null은 "null" 문자열로 처리하여 기존 그룹과 섞이지 않게 한다.
- `FcmAsyncSender.sendOutboxBatch()` 호출 시 batch 내 모든 outbox는 동일한 `routingType` + `routingId`를 가진다 (그룹화 보장). 따라서 group 대표 값(`batch.get(0)`)에서 읽어 MulticastMessage에 한 번 설정.
- data 필드의 value는 문자열(`String.valueOf(routingId)`)로 전달한다 (FCM data map 스펙).

## 예외 · 경계 상황

- `routingType != null && routingId == null`: 발생해서는 안 됨. 호출부(enqueueOutbox)에서 방어. 만약 발생 시 routing 없이 발송 (APNS/Android/data 생략).
- `sendHourlyUnreadNotifications`에서 동일 사용자가 여러 방의 미읽음을 가지면 방마다 별도 Outbox → 방마다 별도 FCM 메시지 → 클라이언트에서 방별로 그룹화됨.

## 비목표 (Non-goals)

- `sendNotificationToAllUsers` API 변경 안 함.
- `FcmMessageService.sendNotification*` 계열(개별 사용자 generic 알림) 변경 안 함.
- 클라이언트(iOS/Android) 딥링크 처리 로직 — 서버는 페이로드만 전달.
- 인앱 알림(UserNotification 테이블) 스키마 변경 없음.
- 알림 수신 설정(ChatNotificationMode, NotificationType) 변경 없음.
- Flyway/Liquibase 마이그레이션 스크립트 작성 (별도 작업).

## 수용 기준 (Acceptance Criteria)

### AC-1: 공지사항 Outbox에 routing 저장
- **Given** Announcement 엔티티(id=5678)로 `bulkEnqueueOutbox` 호출
- **When** FcmOutbox 레코드 생성
- **Then** `routingType = NOTICE`, `routingId = 5678`

### AC-2: 채팅 즉시 알림 Outbox에 routing 저장
- **Given** roomId=1234, EVERY 모드 참가자 대상으로 `sendImmediateNotifications` 호출
- **When** FcmOutbox 레코드 생성
- **Then** `routingType = CHAT`, `routingId = 1234`

### AC-3: 채팅 묶음 알림 Outbox에 routing 저장
- **Given** `UnreadNotificationInfo(userId=1, roomId=999, unreadCount=3)` 조회
- **When** `sendHourlyUnreadNotifications` 실행 후 FcmOutbox 생성
- **Then** `routingType = CHAT`, `routingId = 999`

### AC-4: 공지사항 FCM 메시지에 APNS/Android/data 포함
- **Given** routingType=NOTICE, routingId=5678인 FcmOutbox 배치
- **When** `FcmAsyncSender.sendOutboxBatch` 호출
- **Then** 빌드되는 MulticastMessage에 `apns.aps.thread-id = "notice_5678"`, `android.notification.tag = "notice_5678"`, `data.type = "NOTICE"`, `data.noticeId = "5678"` 포함

### AC-5: 채팅 FCM 메시지에 APNS/Android/data 포함
- **Given** routingType=CHAT, routingId=1234인 FcmOutbox 배치
- **When** `FcmAsyncSender.sendOutboxBatch` 호출
- **Then** `apns.aps.thread-id = "chat_room_1234"`, `android.notification.tag = "chat_room_1234"`, `data.type = "CHAT"`, `data.chatRoomId = "1234"` 포함

### AC-6: routing 없는 Outbox — 기존 동작 유지
- **Given** routingType=null인 FcmOutbox 배치
- **When** `FcmAsyncSender.sendOutboxBatch` 호출
- **Then** MulticastMessage에 APNS config, Android config, data 미포함

### AC-7: FcmOutboxProcessor 그룹화 — routing 다른 메시지 분리
- **Given** routingType=NOTICE/routingId=1 Outbox 2건 + routingType=CHAT/routingId=2 Outbox 1건
- **When** `FcmOutboxProcessor.process` 실행
- **Then** 두 그룹이 별도 `sendOutboxBatch` 호출로 분리됨

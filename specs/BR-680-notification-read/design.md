## 엔티티 / 값 객체

신규 엔티티 없음. 기존 엔티티의 필드만 변경한다.

| 엔티티 | 변경 필드 | 변경 내용 |
|---|---|---|
| `UserNotification` | `isRead` | NOTICE 타입 요청 시 `false → true` (기존 `changeReadStatus()` 활용) |
| `OpenChatParticipant` | `lastReadMessageId` | CHAT 타입 요청 시 해당 방 최신 메시지 ID로 업데이트 (기존 `updateLastReadMessageId()` 활용) |

---

## 애그리거트 경계

- `UserNotification`은 `Notification`을 `@ManyToOne(LAZY)`로 참조한다. 조회 시 `notification.boardId`와 `notification.apiType`에 접근하므로 JOIN FETCH 또는 QueryDSL join이 필요하다.
- `OpenChatParticipant`는 독립 애그리거트 루트이며 `roomId`, `userId`로 식별된다. `OpenChatMessage`는 별도 조회한다.

---

## 연관관계

변경 없음. 기존 연관관계를 그대로 사용한다.

---

## DB 스키마 변경

없음.

---

## 도메인 계층 구조

### 신규 생성 클래스

```
domain/notification/
├── controller/
│   (수정) NotificationController.java        — PATCH /notifications/read 엔드포인트 추가
│   (수정) NotificationApiSpecification.java  — markAsRead 메서드 시그니처 추가
├── service/
│   (신규) NotificationReadService.java       — NOTICE/CHAT 분기 처리 서비스
├── repository/
│   (수정) UserNotificationQuerydslRepository.java  — findByUserIdAndBoardIdAndApiType 추가
│   (수정) UserNotificationRepositoryImpl.java      — QueryDSL 구현 추가
└── dto/
    ├── request/
    │   (신규) RequestNotificationReadDto.java — type(FcmRoutingType), targetId(String)
    └── response/
        (신규) ResponseNotificationReadDto.java — success(boolean), message(String)

domain/openChat/
└── service/
    (수정) OpenChatMessageService.java         — markChatRoomAsRead(roomId, userId) 추가
```

### 클래스별 역할

**`RequestNotificationReadDto`**
```
- type: FcmRoutingType  (@NotNull, Jackson이 역직렬화 시 잘못된 값은 400 자동 처리)
- targetId: String      (@NotBlank)
```

**`ResponseNotificationReadDto`**
```
- success: boolean  (항상 true)
- message: String   ("성공적으로 읽음 처리되었습니다." 고정)
```

**`NotificationReadService`**
```
의존:
  - UserNotificationQuerydslRepository (NOTICE 조회)
  - OpenChatMessageService            (CHAT 처리 위임, 도메인 간 service 호출)

메서드:
  markAsRead(Long userId, FcmRoutingType type, Long targetId): ResponseNotificationReadDto
  - NOTICE → findByUserIdAndBoardIdAndApiType 조회 후 isRead = true
  - CHAT   → openChatMessageService.markChatRoomAsRead(targetId, userId)
```

**`UserNotificationQuerydslRepository` 추가 메서드**
```
List<UserNotification> findByUserIdAndBoardIdAndApiType(Long userId, Long boardId, ApiType apiType)
  - JOIN notification ON userNotification.notification = notification
  - WHERE user.id = userId AND notification.boardId = boardId AND notification.apiType = apiType
```

**`OpenChatMessageService` 추가 메서드**
```
markChatRoomAsRead(Long roomId, Long userId): void
  1. findByRoomIdAndUserId → 없으면 OPEN_CHAT_NOT_PARTICIPANT 예외
  2. findLatestMessageIdByRoomId → Optional<Long>
  3. 최신 메시지 있으면 updateLastReadMessageId(roomId, userId, latestId)
```

---

## 흐름 요약

```
NotificationController.markAsRead(userId, dto)
  └─ NotificationReadService.markAsRead(userId, type, targetId)
       ├─ [NOTICE] UserNotificationQuerydslRepository.findByUserIdAndBoardIdAndApiType()
       │            → forEach: userNotification.changeReadStatus(true)
       └─ [CHAT]   OpenChatMessageService.markChatRoomAsRead(targetId, userId)
                    ├─ OpenChatParticipantRepository.findByRoomIdAndUserId() [없으면 예외]
                    └─ OpenChatMessageQuerydslRepository.findLatestMessageIdByRoomId()
                         → OpenChatParticipantRepository.updateLastReadMessageId()
```

---

## 비목표

- `FcmOutbox` 상태 변경: 이번 범위 외
- 배지 카운트 재계산: 이번 범위 외
- CHAT 요청 시 `UserNotification.isRead` 변경: 명세상 비목표
- `OpenChatMessage` 도메인 신규 엔티티·테이블: 기존 코드 재사용만

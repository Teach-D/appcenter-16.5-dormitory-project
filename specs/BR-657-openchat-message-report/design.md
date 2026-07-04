# BR-657 — 오픈채팅 메시지 신고 기능 설계

---

## 엔티티 / 값 객체

### `OpenChatMessageReport` (신규)

| 필드 | Java 타입 | DB 타입 | 제약 |
|------|-----------|---------|------|
| id | Long | BIGINT PK | AUTO_INCREMENT |
| messageId | Long | BIGINT | NOT NULL |
| roomId | Long | BIGINT | NOT NULL |
| reporterId | Long | BIGINT | NOT NULL |
| targetUserId | Long | BIGINT | NOT NULL |
| reason | String | TEXT | NOT NULL |
| createdDate | LocalDateTime | DATETIME | BaseTimeEntity |
| modifiedDate | LocalDateTime | DATETIME | BaseTimeEntity |

패턴: `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + 정적 팩토리 `create(...)`.

---

## 애그리거트 경계

`OpenChatMessageReport`는 단독 애그리거트 루트다.
- `OpenChatMessage`, `OpenChatParticipant`, `User`는 ID 참조만 사용 (객체 참조 없음).
- 기존 openChat 도메인 엔티티를 내부 객체로 포함하지 않는다.

---

## 연관관계

연관관계 없음. 모든 참조는 Long ID 컬럼으로만 유지한다 (기존 openChat 엔티티 패턴과 동일).

---

## DB 스키마 변경

```sql
CREATE TABLE open_chat_message_report (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    message_id     BIGINT       NOT NULL,
    room_id        BIGINT       NOT NULL,
    reporter_id    BIGINT       NOT NULL,
    target_user_id BIGINT       NOT NULL,
    reason         TEXT         NOT NULL,
    created_date   DATETIME(6)  NOT NULL,
    modified_date  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_ocmr_message_id  ON open_chat_message_report (message_id);
CREATE INDEX idx_ocmr_reporter_id ON open_chat_message_report (reporter_id);
```

### ErrorCode 추가 (ErrorCode.java)

기존 OPEN_CHAT 블록 마지막(`22017` 다음)에 삽입:

```java
OPEN_CHAT_MESSAGE_NOT_FOUND(NOT_FOUND,  22018, "[OpenChat] 메시지를 찾을 수 없습니다."),
OPEN_CHAT_REPORT_SELF(BAD_REQUEST,      22019, "[OpenChat] 자신의 메시지는 신고할 수 없습니다."),
```

비참여자 케이스는 기존 `OPEN_CHAT_NOT_PARTICIPANT(FORBIDDEN, 22005)` 재사용.

---

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatMessageReportController.java        [신규]
│   └── OpenChatMessageReportApiSpecification.java  [신규]
├── service/
│   └── OpenChatMessageReportService.java           [신규]
├── repository/
│   └── OpenChatMessageReportRepository.java        [신규]
├── entity/
│   └── OpenChatMessageReport.java                  [신규]
└── dto/
    └── request/
        └── RequestReportOpenChatMessageDto.java    [신규]
```

기존 파일 수정:
- `global/exception/ErrorCode.java` — `OPEN_CHAT_MESSAGE_NOT_FOUND`, `OPEN_CHAT_REPORT_SELF` 추가

### 각 클래스 책임

**`OpenChatMessageReport`**
- `create(messageId, roomId, reporterId, targetUserId, reason)` 정적 팩토리

**`OpenChatMessageReportRepository`**
- `JpaRepository<OpenChatMessageReport, Long>` 상속
- 추가 메서드 없음 (단순 저장만 필요)

**`OpenChatMessageReportService`**
- `@Transactional` 적용
- `reportMessage(Long reporterId, Long messageId, RequestReportOpenChatMessageDto dto)`
  1. `OpenChatMessage` 조회 → 없으면 `OPEN_CHAT_MESSAGE_NOT_FOUND` (404)
  2. `openChatParticipantRepository.existsByRoomIdAndUserId(message.getRoomId(), reporterId)` → false이면 `OPEN_CHAT_NOT_PARTICIPANT` (403)
  3. `message.getSenderId().equals(reporterId)` → true이면 `OPEN_CHAT_REPORT_SELF` (400)
  4. `OpenChatMessageReport.create(...)` → `save`

**`RequestReportOpenChatMessageDto`**
- `@NotBlank String reason`

**`OpenChatMessageReportController`**
- `@RequestMapping("/open-chat-rooms")`
- `POST /open-chat-rooms/messages/{messageId}/reports` → `201 Created`, 바디 없음

> **URL 주의**: requirement.md의 `/open-chat/messages/{messageId}/reports`는 기존 컨트롤러 컨벤션(`/open-chat-rooms`)과 다르다.
> `/open-chat-rooms/messages/{messageId}/reports`로 통일한다.

---

## 비목표

- 관리자 신고 조회 API (`GET /open-chat-rooms/messages/{messageId}/reports`) → 범위 외
- N회 신고 자동 숨김·차단 로직 → 범위 외
- FCM 푸시 알림 → 범위 외
- `OpenChatMessageReportQuerydslRepository` (단순 저장이므로 QueryDSL 불필요)
- 신고 취소 기능 → 범위 외

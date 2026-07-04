# BR-659 — 도메인 설계

## 엔티티 / 값 객체

엔티티 변경 없음. 신규 enum 1개만 추가.

### KickReason (신규 enum)

```java
package com.example.appcenter_project.domain.openChat.enums;

public enum KickReason {
    SPAM,
    ABUSE,
    IMPERSONATION,
    REPORT_ACCUMULATED,
    OTHER
}
```

기존 enum(`OpenChatRoomScope`, `OpenChatRoomType`, `OpenChatRoomTab`, `OpenChatMessageType`)과
동일한 스타일 — 어노테이션 없이 상수만 선언.

---

## 애그리거트 경계

변경 없음. 기존 구조 유지.

- `OpenChatRoom` — 애그리거트 루트
- `OpenChatParticipant` — `roomId` (Long) ID 참조로 방에 연결. JPA `@ManyToOne` 없음.

---

## 연관관계

변경 없음. 신규 연관관계 없음.

---

## DB 스키마 변경

없음. 엔티티 필드 변경 없으므로 마이그레이션 스크립트 불필요.

---

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatRoomController.java         ← 수정: kickParticipant 파라미터 추가
│   └── OpenChatRoomApiSpecification.java   ← 수정: kickParticipant 시그니처 동기화
├── service/
│   └── OpenChatRoomService.java            ← 수정: kickParticipant, leaveRoom 로직 변경
├── enums/
│   └── KickReason.java                     ← 신규
global/exception/
└── ErrorCode.java                          ← 수정: OPEN_CHAT_NEW_HOST_REQUIRED 추가
```

### 신규 생성

| 파일 | 설명 |
|------|------|
| `domain/openChat/enums/KickReason.java` | 강제퇴장 사유 enum |

### 수정 대상

| 파일 | 변경 내용 |
|------|-----------|
| `global/exception/ErrorCode.java` | `OPEN_CHAT_NEW_HOST_REQUIRED(BAD_REQUEST, 22018, ...)` 추가 |
| `OpenChatRoomService.java` | `@Slf4j` 추가, `kickParticipant` 시그니처·로직 변경, `leaveRoom` 로직 변경 |
| `OpenChatRoomController.java` | `kickParticipant` — `@RequestParam KickReason reason`, `@RequestParam(required=false) Long newHostUserId` 추가 |
| `OpenChatRoomApiSpecification.java` | `kickParticipant` 시그니처 동기화 |

---

## 변경 상세

### ErrorCode 추가

```java
OPEN_CHAT_NEW_HOST_REQUIRED(BAD_REQUEST, 22018, "[OpenChat] 방장 강퇴 시 새 방장을 지정해야 합니다."),
```

기존 `OPEN_CHAT_KICK_FORBIDDEN(22017)` 바로 다음에 삽입.

---

### OpenChatRoomService — kickParticipant 시그니처

```java
// 기존
public void kickParticipant(Long actorId, Long roomId, Long targetUserId)

// 변경
public void kickParticipant(Long actorId, Long roomId, Long targetUserId,
                             KickReason reason, Long newHostUserId)
```

#### 로직 변경 포인트

1. `findByIdWithLock` 반환값을 `room` 변수에 저장 (기존 결과 버림 → 방 삭제에 활용)
2. ADMIN이 방장을 강퇴할 때 추가 분기 (기존 `if (!isAdmin)` 블록 이후):
   ```
   if (isAdmin && targetParticipant.isHost()) {
       List<OpenChatParticipant> allParticipants = openChatParticipantRepository.findAllByRoomId(roomId);
       boolean othersExist = allParticipants.size() > 1;
       if (othersExist) {
           if (newHostUserId == null) → OPEN_CHAT_NEW_HOST_REQUIRED
           OpenChatParticipant newHost = findByRoomIdAndUserId(roomId, newHostUserId) → OPEN_CHAT_PARTICIPANT_NOT_FOUND
           if (newHost.isHost()) → OPEN_CHAT_ALREADY_HOST
           newHost.grantHost()
       }
       openChatParticipantRepository.delete(targetParticipant)
       if (!othersExist) openChatRoomRepository.delete(room)   // 마지막 참여자 강퇴 → 방 삭제
   } else {
       openChatParticipantRepository.delete(targetParticipant) // 기존 흐름
   }
   ```
3. 처리 완료 후 `log.info()` 기록:
   ```java
   log.info("[OpenChat-Exit] exitType={} roomId={} targetUserId={} actorId={} reason={} processedAt={}",
       isAdmin ? "ADMIN_KICK" : "HOST_KICK", roomId, targetUserId, actorId, reason, Instant.now());
   ```
4. 시스템 메시지는 방이 삭제된 경우 생략 (참여자 없으므로 의미 없음)

---

### OpenChatRoomService — leaveRoom 로직 변경

변경 대상: `if (self.isHost())` → `hostCount == 1` 분기 내부.

```
// 기존
if (hostCount == 1) {
    throw new CustomException(ErrorCode.OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE);
}

// 변경
if (hostCount == 1) {
    if (lockedParticipants.size() == 1) {
        // 혼자 남은 방장 — 방 종류에 따라 분기
        OpenChatRoom room = openChatRoomRepository.findByIdWithLock(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.OPEN_CHAT_ROOM_NOT_FOUND));
        if (!room.isOfficial()) {
            openChatParticipantRepository.deleteAll(lockedParticipants);
            openChatRoomRepository.delete(room);
            log.info("[OpenChat-Exit] exitType=VOLUNTARY roomId={} targetUserId={} actorId={} processedAt={}",
                roomId, userId, userId, Instant.now());
            return ResponseLeaveOpenChatRoomDto.builder().roomDeleted(true).build();
        }
    }
    throw new CustomException(ErrorCode.OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE);
}
```

자진 퇴장 성공(방 삭제 없는 경우)에도 동일 필드 로그 추가:
```java
log.info("[OpenChat-Exit] exitType=VOLUNTARY roomId={} targetUserId={} actorId={} processedAt={}",
    roomId, userId, userId, Instant.now());
```

---

### OpenChatRoomController — kickParticipant 파라미터 추가

```java
@DeleteMapping("/{roomId}/participants/{targetUserId}")
public ResponseEntity<Void> kickParticipant(
        @AuthenticationPrincipal CustomUserDetails user,
        @PathVariable Long roomId,
        @PathVariable Long targetUserId,
        @RequestParam KickReason reason,
        @RequestParam(required = false) Long newHostUserId) {
    openChatRoomService.kickParticipant(user.getId(), roomId, targetUserId, reason, newHostUserId);
    return ResponseEntity.noContent().build();
}
```

`reason`은 `@RequestParam`(required 기본값 true) → 누락 시 Spring이 400 자동 반환.

---

## 비목표

requirement.md 비목표를 그대로 따른다.

- `OpenChatParticipant`에 `isBanned` 등 강퇴 상태 필드 없음
- `OpenChatRoom`에 `status(OPEN/CLOSED)` enum 없음
- `OpenChatExitLog` 엔티티·테이블 없음
- WebSocket STOMP 세션 강제 종료 없음
- `KickReason.OTHER` 선택 시 추가 텍스트 입력 없음
- 기존 권한 체크 조건(방장·ADMIN 판별) 변경 없음

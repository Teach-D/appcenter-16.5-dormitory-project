# BR-700 오픈채팅방 정보 수정 — 설계

## 엔티티 / 값 객체

### OpenChatRoom (기존 엔티티 수정)

새 필드 없음. 부분 업데이트를 위한 `update()` 메서드만 추가한다.

```java
// OpenChatRoom에 추가할 메서드
public void update(String name, String description, OpenChatRoomScope scope,
                   Integer maxParticipants, String password, Boolean isPublic) {
    if (name != null)           this.name = name;
    if (description != null)    this.description = description;
    if (scope != null)          this.scope = scope;
    if (maxParticipants != null) this.maxParticipants = maxParticipants;
    // null = 변경 안 함 / "" = 비밀번호 해제 / 非blank = 비밀번호 설정
    if (password != null)       this.password = password.isBlank() ? null : password;
    if (isPublic != null)       this.isPublic = isPublic;
}
```

### OpenChatParticipant (변경 없음)

`findByRoomIdAndUserId` + `isHost()` 조합으로 방장 여부를 확인한다. 엔티티 자체 변경 없음.

---

## 애그리거트 경계

- `OpenChatRoom` — 이번 기능의 애그리거트 루트. 수정 대상.
- `OpenChatParticipant` — 방장 검증에만 사용. 조회 후 상태 확인만 하고 변경 없음.
- 두 애그리거트 간 참조는 `roomId` / `userId` (Long) ID 참조 방식 그대로 유지.

---

## 연관관계

기존 연관관계 변경 없음. 이번 기능은 `OpenChatRoom` 단일 엔티티의 필드를 업데이트하는 것으로 연관관계를 새로 맺지 않는다.

---

## DB 스키마 변경

없음. `open_chat_room` 테이블의 기존 컬럼(`name`, `description`, `scope`, `max_participants`, `password`, `is_public`)을 그대로 사용한다.

---

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatRoomController.java          [수정] PATCH /{roomId} 엔드포인트 추가
│   └── OpenChatRoomApiSpecification.java    [수정] updateRoom() 메서드 명세 추가
├── service/
│   └── OpenChatRoomService.java             [수정] updateRoom() 메서드 추가
├── entity/
│   └── OpenChatRoom.java                    [수정] update() 메서드 추가
├── dto/
│   └── request/
│       └── RequestUpdateOpenChatRoomDto.java [신규]
└── (repository, enums 변경 없음)
```

### global/exception/
```
ErrorCode.java    [수정] OPEN_CHAT_NOT_HOST, OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL 추가
```

---

## 서비스 로직 상세

```
updateRoom(Long userId, Long roomId, RequestUpdateOpenChatRoomDto request):
  1. openChatRoomRepository.findById(roomId)
       → 없으면 OPEN_CHAT_ROOM_NOT_FOUND (404)
  2. openChatParticipantRepository.findByRoomIdAndUserId(roomId, userId)
       → 없으면 OPEN_CHAT_PARTICIPANT_NOT_FOUND (404)
  3. participant.isHost() == false
       → OPEN_CHAT_NOT_HOST (403)
  4. room.isOfficial() == true  OR  room.getRoomType() == PERSONAL
       → OPEN_CHAT_ROOM_FORBIDDEN (403)
  5. request.maxParticipants != null:
       currentCount = openChatParticipantRepository.countByRoomId(roomId)
       currentCount > request.maxParticipants
       → OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL (400)
  6. room.update(request의 각 필드) — null 필드는 update() 내부에서 건너뜀
```

기존 `openChatParticipantRepository.countByRoomId()` 메서드를 그대로 사용하므로 새 쿼리 불필요.

---

## 신규 클래스 명세

### RequestUpdateOpenChatRoomDto

```java
@Getter
public class RequestUpdateOpenChatRoomDto {

    @Size(max = 30)
    private String name;           // null = 변경 안 함

    @Size(max = 100)
    private String description;    // null = 변경 안 함

    private OpenChatRoomScope scope;  // null = 변경 안 함

    @Min(2) @Max(100)
    private Integer maxParticipants;  // null = 변경 안 함

    @Size(max = 50)
    private String password;       // null = 변경 안 함 / "" = 비밀번호 해제

    private Boolean isPublic;      // null = 변경 안 함
}
```

- `name`은 Bean Validation `@NotBlank` 없이 `@Size`만 적용 — null 허용(변경 안 함), 전달 시 blank 불가는 서비스에서 별도 검증하지 않고 `@NotBlank` 없이 `@Size(min=1)`으로 처리.
  - 단순화: `name`이 전달됐을 때(non-null) blank 검증은 `@Size(min=1, max=30)` 조합으로 처리.

---

## 비목표

- 새 테이블·컬럼·인덱스 없음.
- QueryDSL 신규 메서드 없음 (기존 `findById`, `findByRoomIdAndUserId`, `countByRoomId` 재사용).
- 수정 이벤트 WebSocket 브로드캐스트 없음.
- `roomType`, `isOfficial`, `createdBy`, `creatorDormitory` 수정 없음.
- PERSONAL·OFFICIAL 방 지원 없음.

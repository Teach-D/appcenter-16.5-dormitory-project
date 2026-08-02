# BR-720 — 도메인 설계

## 엔티티 / 값 객체

### OpenChatRoom (기존 엔티티 — 필드 추가)

| 필드 | 타입 | 제약 | 설명 |
|------|------|------|------|
| `targetDorm` | `DormType` | nullable, `@Enumerated(STRING)` | 공식 기숙사 방이 대표하는 기숙사. 일반 방은 null |

그 외 기존 필드(`isOfficial`, `scope`, `maxParticipants`, `roomType`) 그대로 사용.
신규 팩토리 메서드 `OpenChatRoom.createDormOfficial(name, description, createdBy, targetDorm)` 추가.

### OpenChatParticipant (변경 없음)

기존 엔티티 그대로. 벌크 생성 시 기존 `create(roomId, userId, joinedAt)` 팩토리 사용.

---

## 애그리거트 경계

| 애그리거트 루트 | 내부 객체 | 경계 간 참조 방식 |
|----------------|----------|-----------------|
| `OpenChatRoom` | — | — |
| `OpenChatParticipant` | — | `roomId`, `userId` (ID 참조) |
| `User` | — | — |

도메인 간 호출: `OpenChatDormOfficialRoomService`(openChat 도메인) ← `UserService`(user 도메인)가 호출.
역방향(`openChat → user`)은 `UserRepository`를 직접 주입하는 방식으로 처리 (현재 `OpenChatRoomService`도 동일 패턴).

---

## 연관관계

신규 연관관계 없음. `targetDorm`은 단순 컬럼 필드이며 `User.dormType`(DormType enum)과 값 비교만 한다.

---

## DB 스키마 변경

```sql
ALTER TABLE open_chat_room
    ADD COLUMN target_dorm VARCHAR(20) NULL;
```

- `UNIQUE` 인덱스는 추가하지 않는다. 유일성은 서비스 레벨에서 보장 (NULL 다중 허용 때문에 DB 제약이 의도대로 동작하지 않을 수 있음).
- 기존 행은 `target_dorm = NULL`로 초기화됨(마이그레이션 불필요).

---

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatRoomAdminController.java          [신규] POST /admin/open-chat-rooms/dorm
│   └── OpenChatRoomAdminApiSpecification.java    [신규] Swagger 인터페이스
├── service/
│   ├── OpenChatDormOfficialRoomService.java      [신규] 방 생성·재배정 핵심 로직
│   └── OpenChatRoomService.java                  [수정 없음]
├── repository/
│   └── OpenChatRoomRepository.java               [수정] findByTargetDorm() 추가
├── entity/
│   └── OpenChatRoom.java                         [수정] targetDorm 필드, createDormOfficial() 팩토리
├── dto/
│   └── request/
│       └── RequestCreateDormOfficialRoomDto.java  [신규]
└── enums/ (변경 없음)

domain/user/
├── service/
│   └── UserService.java                          [수정] updateUser() — 기숙사 변경 감지 후 재배정 호출
└── repository/
    └── UserRepository.java                       [수정] findAllByDormType() 추가

global/
├── exception/
│   └── ErrorCode.java                            [수정] OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS 추가
└── config/
    └── SecurityConfig.java                       [수정] /admin/open-chat-rooms/** ADMIN 권한 추가
```

---

## 클래스별 설계 상세

### OpenChatRoom.java (수정)

```java
@Enumerated(EnumType.STRING)
@Column(name = "target_dorm")
private DormType targetDorm;   // nullable

public static OpenChatRoom createDormOfficial(String name, String description, Long createdBy, DormType targetDorm) {
    OpenChatRoom room = new OpenChatRoom();
    room.name = name;
    room.description = description;
    room.scope = OpenChatRoomScope.DORMITORY;
    room.maxParticipants = Integer.MAX_VALUE;
    room.isOfficial = true;
    room.createdBy = createdBy;
    room.roomType = OpenChatRoomType.OPEN;
    room.isPublic = true;
    room.targetDorm = targetDorm;
    room.creatorDormitory = targetDorm.name();
    return room;
}
```

### OpenChatRoomRepository.java (수정)

```java
Optional<OpenChatRoom> findByTargetDorm(DormType targetDorm);
```

### UserRepository.java (수정)

```java
List<User> findAllByDormType(DormType dormType);
```

### RequestCreateDormOfficialRoomDto.java (신규)

```java
@NotBlank @Size(max = 30)
private String name;

@Size(max = 100)
private String description;

@NotNull
private DormType dormType;   // DORM_1·DORM_2·DORM_3만 허용, NONE → 서비스에서 400
```

### OpenChatDormOfficialRoomService.java (신규)

의존: `OpenChatRoomRepository`, `OpenChatParticipantRepository`, `UserRepository`

```
createDormOfficialRoom(Long adminId, RequestCreateDormOfficialRoomDto request)
  1. dormType == NONE → throw INVALID_DORM_TYPE (400)
  2. findByTargetDorm(request.dormType).isPresent() → throw OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS (409)
  3. OpenChatRoom.createDormOfficial(...) → save
  4. userRepository.findAllByDormType(request.dormType) → 벌크 OpenChatParticipant 생성 → saveAll
  5. return roomId

reassignDormRoom(Long userId, DormType oldDorm, DormType newDorm)
  // oldDorm 처리
  if oldDorm is DORM_X:
    findByTargetDorm(oldDorm) 존재 시:
      openChatParticipantRepository.findByRoomIdAndUserId(room.id, userId)
        .ifPresent(participant → openChatParticipantRepository.delete(participant))
  // newDorm 처리
  if newDorm is DORM_X:
    findByTargetDorm(newDorm) 존재 시:
      if NOT existsByRoomIdAndUserId(room.id, userId):
        openChatParticipantRepository.save(OpenChatParticipant.create(room.id, userId, false))
```

### UserService.updateUser() (수정)

```java
public ResponseUserDto updateUser(Long userId, RequestUserDto request) {
    User user = findUserById(userId);
    DormType oldDorm = user.getDormType();          // ← 변경 전 캡처
    user.update(request);
    DormType newDorm = user.getDormType();
    if (oldDorm != newDorm) {
        openChatDormOfficialRoomService.reassignDormRoom(userId, oldDorm, newDorm);
    }
    // ... 기존 로직 이어서
}
```

### OpenChatRoomAdminController.java (신규)

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/open-chat-rooms")
public class OpenChatRoomAdminController implements OpenChatRoomAdminApiSpecification {

    private final OpenChatDormOfficialRoomService dormOfficialRoomService;

    @PostMapping("/dorm")
    public ResponseEntity<Map<String, Long>> createDormOfficialRoom(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody @Valid RequestCreateDormOfficialRoomDto request) {
        Long roomId = dormOfficialRoomService.createDormOfficialRoom(user.getId(), request);
        return ResponseEntity.status(CREATED).body(Map.of("roomId", roomId));
    }
}
```

### SecurityConfig.java (수정)

기존 오픈채팅 블록에 추가:
```java
.requestMatchers(POST, "/admin/open-chat-rooms/dorm").hasRole("ADMIN")
```

### ErrorCode.java (수정)

```java
OPEN_CHAT_DORM_OFFICIAL_ROOM_ALREADY_EXISTS(CONFLICT, 22027, "[OpenChat] 해당 기숙사의 공식 오픈채팅방이 이미 존재합니다.")
```

---

## 의존 관계 요약

```
OpenChatRoomAdminController
    └── OpenChatDormOfficialRoomService
            ├── OpenChatRoomRepository
            ├── OpenChatParticipantRepository
            └── UserRepository

UserService
    └── OpenChatDormOfficialRoomService  (reassignDormRoom 호출)
```

`UserService` ↔ `OpenChatDormOfficialRoomService` 단방향. 역방향 의존 없으므로 순환 참조 없음.

---

## 비목표

- `OpenChatRoomType`에 `DORMITORY_OFFICIAL` 값 추가 — `isOfficial=true` + `targetDorm NOT NULL`로 식별
- DB 레벨 UNIQUE 제약 추가 — 서비스 레벨 중복 검사로 대체
- 공식 기숙사 방 수정/삭제 API — 기존 `isOfficial=true` 방지 로직이 이미 적용됨
- 자동 입·퇴장 시 FCM 발송, WebSocket 이벤트, 시스템 메시지
- 관리자 공식 기숙사 방 목록 조회 API

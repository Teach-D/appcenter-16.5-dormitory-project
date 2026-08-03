# BR-668 도메인 설계 — 단체·개인 채팅방 생성 (비밀번호·공개 여부)

---

## 엔티티 / 값 객체

### OpenChatRoom (기존, 수정 없음)

`password`(VARCHAR 50, nullable), `isPublic`(BOOLEAN, default true) 컬럼이 이미 존재한다.
팩토리 메서드 시그니처만 변경·추가된다.

| 필드 | 타입 | 변경 여부 |
|---|---|---|
| id | Long | 유지 |
| name | VARCHAR(30) | 유지 |
| description | VARCHAR(100), nullable | 유지 |
| scope | OpenChatRoomScope | 유지 |
| maxParticipants | int | 유지 |
| creatorDormitory | VARCHAR, nullable | 유지 |
| isOfficial | boolean | 유지 |
| createdBy | Long | 유지 |
| roomType | OpenChatRoomType | 유지 (enum 값만 추가) |
| password | VARCHAR(50), nullable | 유지 (OPEN 타입도 사용 시작) |
| isPublic | boolean, default true | 유지 (OPEN 타입도 사용 시작) |
| lastMessage, lastMessageAt | — | 유지 |

---

## 애그리거트 경계

- `OpenChatRoom`: 애그리거트 루트
- `OpenChatParticipant`: `roomId`(Long) ID 참조로 연결 — 기존 구조 유지

---

## 연관관계

변경 없음. 기존 `OpenChatRoom`·`OpenChatParticipant` 관계(ID 참조, LAZY) 유지.

---

## DB 스키마 변경

없음.

`roomType` 컬럼은 `@Enumerated(EnumType.STRING)` → VARCHAR 저장.
`PERSONAL` 값 추가는 Java enum 변경만으로 충분하며 DDL 변경 불필요.

---

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatRoomController.java          [수정] POST /personal 엔드포인트 추가
│   ├── OpenChatRoomApiSpecification.java    [수정] personal 엔드포인트 명세 추가
│   └── OpenChatDerivedRoomController.java   [유지]
├── service/
│   └── OpenChatRoomService.java             [수정] createRoom 수정, createPersonalRoom 추가, joinRoom 수정
├── repository/
│   ├── OpenChatRoomQuerydslRepositoryImpl.java  [수정] findAllPublicRooms — OPEN 타입 isPublic 조건 추가
│   └── (나머지 유지)
├── entity/
│   └── OpenChatRoom.java                    [수정] create() 시그니처 변경, createPersonal() 추가
├── dto/
│   ├── request/
│   │   ├── RequestCreateOpenChatRoomDto.java    [수정] isPublic, password 필드 추가
│   │   └── RequestCreatePersonalRoomDto.java    [신규] name, password
│   └── response/
│       └── ResponsePersonalRoomCreatedDto.java  [신규] roomId + of(Long)
└── enums/
    └── OpenChatRoomType.java                [수정] PERSONAL 값 추가
```

---

## 상세 설계

### 1. `OpenChatRoomType` — PERSONAL 추가

```java
public enum OpenChatRoomType {
    OPEN,
    DERIVED,
    PERSONAL   // 신규
}
```

---

### 2. `RequestCreateOpenChatRoomDto` — 필드 추가

기존 `@Builder` 유지. 새 필드는 nullable로 추가한다.

```java
@Size(max = 50)
private String password;     // nullable

private Boolean isPublic;    // nullable — 서비스에서 null → true 처리
```

---

### 3. `RequestCreatePersonalRoomDto` — 신규

```java
@Getter
@NoArgsConstructor
public class RequestCreatePersonalRoomDto {

    @NotBlank
    @Size(max = 30)
    private String name;

    @NotNull
    private Long targetUserId;

    @Size(max = 50)
    private String password;   // nullable
}
```

---

### 4. `ResponsePersonalRoomCreatedDto` — 신규

`ResponseDerivedRoomCreatedDto`와 동일한 구조. 타입 구분을 위해 별도 클래스로 생성.

```java
@Getter
public class ResponsePersonalRoomCreatedDto {

    private final Long roomId;

    private ResponsePersonalRoomCreatedDto(Long roomId) { this.roomId = roomId; }

    public static ResponsePersonalRoomCreatedDto of(Long roomId) {
        return new ResponsePersonalRoomCreatedDto(roomId);
    }
}
```

---

### 5. `OpenChatRoom` — 팩토리 메서드 변경

**`create()` 시그니처 변경** (기존 호출부 `OpenChatRoomService.createRoom` 수정 필요):

```java
public static OpenChatRoom create(String name, String description, OpenChatRoomScope scope,
                                   int maxParticipants, Long createdBy,
                                   String creatorDormitory, boolean isOfficial,
                                   String password, boolean isPublic) { ... }
```

**`createPersonal()` 신규**:

```java
public static OpenChatRoom createPersonal(String name, Long createdBy, String password) {
    OpenChatRoom room = new OpenChatRoom();
    room.name = name;
    room.scope = OpenChatRoomScope.ALL;
    room.maxParticipants = 2;
    room.isOfficial = false;
    room.createdBy = createdBy;
    room.roomType = OpenChatRoomType.PERSONAL;
    room.isPublic = false;
    room.password = (password != null && !password.isBlank()) ? password : null;
    return room;
}
```

> `password` 빈 문자열은 null로 정규화 (명세 규칙 반영).

---

### 6. `OpenChatRoomService` — 변경 3곳

**`createRoom()` — isPublic/password 처리 추가**

```java
boolean pub = Boolean.FALSE.equals(request.getIsPublic()) ? false : true;
String pwd = (request.getPassword() != null && !request.getPassword().isBlank())
             ? request.getPassword() : null;

OpenChatRoom room = OpenChatRoom.create(
    request.getName(), request.getDescription(), request.getScope(),
    request.getMaxParticipants(), userId, creatorDormitory, false,
    pwd, pub
);
```

**`createPersonalRoom()` — 신규**

```java
@Transactional
public ResponsePersonalRoomCreatedDto createPersonalRoom(Long userId,
                                                          RequestCreatePersonalRoomDto request) {
    if (userId.equals(request.getTargetUserId())) {
        throw new CustomException(ErrorCode.OPEN_CHAT_SELF_PERSONAL_FORBIDDEN);
    }
    userRepository.findById(request.getTargetUserId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

    OpenChatRoom room = OpenChatRoom.createPersonal(request.getName(), userId, request.getPassword());
    OpenChatRoom saved = openChatRoomRepository.save(room);
    openChatParticipantRepository.save(OpenChatParticipant.create(saved.getId(), userId, true));
    openChatParticipantRepository.save(OpenChatParticipant.create(saved.getId(), request.getTargetUserId(), false));
    return ResponsePersonalRoomCreatedDto.of(saved.getId());
}
```

**`joinRoom()` — PERSONAL 분기 불필요**

생성 시 이미 생성자·targetUser 2명이 등록되어 정원(2명)이 차므로,
`joinRoom`에서 PERSONAL 타입 별도 분기를 추가하지 않는다.
3번째 입장 시도는 기존 `OPEN_CHAT_ROOM_FULL` 검사로 자연스럽게 차단된다.

---

### 7. `OpenChatRoomQuerydslRepositoryImpl.findAllPublicRooms()` — OPEN 타입에 isPublic 조건 추가

```java
// 변경 전
openChatRoom.roomType.eq(OpenChatRoomType.OPEN)
    .and(openChatRoom.scope.eq(OpenChatRoomScope.ALL))

// 변경 후
openChatRoom.roomType.eq(OpenChatRoomType.OPEN)
    .and(openChatRoom.scope.eq(OpenChatRoomScope.ALL))
    .and(openChatRoom.isPublic.isTrue())
```

PERSONAL 타입은 `isPublic=false` 고정이므로 별도 필터 불필요.

---

### 8. `OpenChatRoomController` — `/personal` 엔드포인트 추가

```java
@PostMapping("/personal")
public ResponseEntity<ResponsePersonalRoomCreatedDto> createPersonalRoom(
        @AuthenticationPrincipal CustomUserDetails user,
        @RequestBody @Valid RequestCreatePersonalRoomDto request) {
    ResponsePersonalRoomCreatedDto result = openChatRoomService.createPersonalRoom(user.getId(), request);
    return ResponseEntity.status(CREATED).body(result);
}
```

---

## ErrorCode 추가

| ErrorCode | HTTP | code | 메시지 |
|---|---|---|---|
| `OPEN_CHAT_SELF_PERSONAL_FORBIDDEN` | 400 | 22019 | [OpenChat] 자기 자신과 개인 채팅방을 생성할 수 없습니다. |

> `global/exception/ErrorCode.java`에 추가한다.

---

## 비목표

- 비밀번호 해시 저장
- OPEN 타입 입장(`joinRoom`) 비밀번호 검증
- 채팅방 수정 API (비밀번호·공개여부 변경)
- 개인 채팅방 강퇴·방장 위임 관리
- targetUser에게 초대 알림 발송
- PERSONAL 전용 탭 추가 (MY 탭 통합 노출로 충분)

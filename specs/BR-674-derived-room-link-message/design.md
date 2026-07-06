# BR-674 — 설계 문서

## 엔티티 / 값 객체

### OpenChatMessage (기존, 변경 없음)
| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long | PK, AUTO |
| roomId | Long | NOT NULL |
| senderId | Long | NOT NULL — ROOM_LINK는 요청자 ID |
| content | TEXT | NOT NULL — ROOM_LINK는 JSON 문자열 |
| type | OpenChatMessageType | NOT NULL |

ROOM_LINK content 예시:
```json
{"derivedRoomId":42,"roomName":"토론방","description":"자유 토론","maxParticipants":30}
```
description이 null이면 `"description":null`로 직렬화. content 컬럼(TEXT) 500자 초과 없음.

## 애그리거트 경계

- `OpenChatRoom` 애그리거트: originRoom, derivedRoom 각각 독립 루트. 서로 ID 참조.
- `OpenChatMessage` 애그리거트: `roomId`로 방을 ID 참조. ROOM_LINK 메시지는 `content` JSON에 derivedRoomId 내장.
- 연관된 방 정보(name 등)를 DB에서 재조회하지 않고 생성 시점에 JSON으로 스냅샷 — 이후 derivedRoom 이름이 바뀌어도 메시지 내용 불변.

## 연관관계

기존 연관관계 변경 없음. 모든 참조는 ID 참조(roomId, senderId) — 엔티티 간 @ManyToOne 없음.

## DB 스키마 변경

없음. 기존 `open_chat_message.content` TEXT 컬럼에 JSON 저장.

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   └── OpenChatDerivedRoomController.java        (변경 없음)
├── service/
│   ├── OpenChatRoomService.java                  [수정] createDerivedRoom()
│   └── OpenChatMessageService.java               [수정] sendRoomLinkMessage() 추가, getMessages() 수정
├── dto/
│   ├── request/
│   │   └── RequestCreateDerivedRoomDto.java      [수정] originRoomId 필드 추가
│   └── response/
│       └── ResponseOpenChatMessageDto.java       [수정] linkedRoom* 필드 + fromRoomLink() 추가
└── enums/
    └── OpenChatMessageType.java                  [수정] ROOM_LINK 추가
```

### 수정 상세

#### `OpenChatMessageType` (enums)
```
TEXT, IMAGE, SYSTEM, ROOM_LINK   ← ROOM_LINK 추가
```

#### `RequestCreateDerivedRoomDto` (request DTO)
```java
@NotNull
private Long originRoomId;       // 새 필드 추가
```
기존 필드(name, description, maxParticipants, isPublic, password) 변경 없음.

#### `ResponseOpenChatMessageDto` (response DTO)
아래 4개 필드 추가 — ROOM_LINK 타입일 때만 non-null, 나머지 타입은 null:
```java
private Long linkedRoomId;
private String linkedRoomName;
private String linkedRoomDescription;
private Integer linkedRoomMaxParticipants;
```
새 팩토리 메서드 추가:
```java
public static ResponseOpenChatMessageDto fromRoomLink(
    OpenChatMessage message, String senderNickname, int unreadCount,
    Long linkedRoomId, String linkedRoomName, String linkedRoomDescription, Integer linkedRoomMaxParticipants)
```

#### `OpenChatMessageService` (service)
신규 메서드 `sendRoomLinkMessage()` 추가:
```java
public void sendRoomLinkMessage(
    Long originRoomId, Long senderId,
    Long derivedRoomId, String roomName, String description, int maxParticipants)
```
- `senderId`로 `userRepository`에서 nickname 조회
- content JSON 생성 후 `OpenChatMessage.create(originRoomId, senderId, content, ROOM_LINK)` 저장
- `originRoom.updateLastMessage()` 호출
- `/sub/openchat/{originRoomId}`로 브로드캐스트 (기존 sendSystemMessage() 패턴 동일)
- `ObjectMapper` 주입 필요 (`@Autowired` 또는 생성자 주입)

`getMessages()` 수정:
- DTO 빌드 루프에서 type == ROOM_LINK인 경우 content JSON 파싱 후 `fromRoomLink()` 호출
- JSON 파싱을 위한 private record `RoomLinkPayload(Long derivedRoomId, String roomName, String description, Integer maxParticipants)` 추가 (inner class)
- ROOM_LINK senderId는 실제 사용자 ID이므로 기존 nicknameByUserId 맵에서 조회 가능 (senderIds 필터 조건 변경 불필요)

#### `OpenChatRoomService` (service)
`createDerivedRoom()` 수정:
```
1. originRoom 존재 확인 → OPEN_CHAT_ROOM_NOT_FOUND(404)
2. 요청자가 originRoom 참여자인지 확인 → OPEN_CHAT_PARTICIPANT_NOT_FOUND(403)
3. 기존 derivedRoom 생성 로직 (변경 없음)
4. openChatMessageService.sendRoomLinkMessage(
       originRoomId, userId,
       savedRoom.getId(), request.getName(), request.getDescription(), request.getMaxParticipants())
```

## 비목표
- `OpenChatMessage`에 `linkedRoomId` 컬럼 별도 추가 — JSON content로 대체
- originRoom 참여자 전체 FCM 알림
- ROOM_LINK 클릭 시 입장 API 변경
- derivedRoom의 isPublic/password 메시지에 포함

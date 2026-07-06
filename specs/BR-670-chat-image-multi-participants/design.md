# BR-670 설계 — 채팅방 이미지 다중 전송 및 참여자 단순 목록 API

---

## 엔티티 / 값 객체

신규 엔티티 없음. 기존 엔티티를 그대로 사용한다.

### OpenChatMessage (기존, 변경 없음)

| 필드 | 타입 | 제약 |
|------|------|------|
| id | Long | PK, AUTO_INCREMENT |
| roomId | Long | NOT NULL |
| senderId | Long | NOT NULL |
| content | String (TEXT) | NOT NULL, 이미지 메시지는 `""` |
| type | OpenChatMessageType | NOT NULL, IMAGE |

이미지 파일은 별도 `image` 테이블에 `ImageType.OPEN_CHAT_MESSAGE` + `entityId = message.getId()` 로 저장된다.  
이번 변경 후 이미지 메시지 1개당 연결되는 이미지 레코드는 정확히 1건이다.

### OpenChatParticipant (기존, 변경 없음)

참여자 단순 목록 조회 시 userId를 추출하는 용도로만 사용. 구조 변경 없음.

---

## 애그리거트 경계

- `OpenChatMessage` — 단독 애그리거트 루트. Image 파일은 `ImageType + entityId`로 참조하며 JPA 연관관계 없음.
- `OpenChatRoom` — 단독 애그리거트 루트. `lastMessage`·`lastMessageTime` 갱신은 `OpenChatRoom.updateLastMessage()` 통해서만.
- `OpenChatParticipant` — `OpenChatRoom`의 ID를 `roomId`로만 참조 (JPA 연관관계 없음).

경계를 넘는 참조는 모두 ID 참조. 도메인 간 서비스 호출은 `OpenChatMessageService` → `OpenChatRoomService` 없이, 각자 레포지토리 직접 접근으로 처리.

---

## 연관관계

변경 없음. 기존 연관관계 유지.

- `OpenChatMessage.roomId` — `OpenChatRoom.id` 외래키 (JPA 미매핑, Long 컬럼)
- `OpenChatMessage.senderId` — `User.id` 외래키 (JPA 미매핑, Long 컬럼)
- Image ↔ OpenChatMessage — `ImageType.OPEN_CHAT_MESSAGE + entityId` 로 느슨하게 연결

---

## DB 스키마 변경

없음.

---

## 도메인 계층 구조

```
domain/openChat/
├── controller/
│   ├── OpenChatMessageController.java        [수정] sendImageMessage() 응답 타입 변경
│   ├── OpenChatMessageApiSpecification.java  [수정] sendImageMessage() 시그니처 변경
│   ├── OpenChatRoomController.java           [수정] GET /{roomId}/participants/simple 엔드포인트 추가
│   └── OpenChatRoomApiSpecification.java     [수정] getSimpleParticipants() 시그니처 추가
├── service/
│   ├── OpenChatMessageService.java           [수정] sendImageMessage() 로직·반환 타입 변경
│   └── OpenChatRoomService.java              [수정] getSimpleParticipants() 메서드 추가
└── dto/
    └── response/
        ├── ResponseSimpleParticipantDto.java      [신규]
        └── ResponseSimpleParticipantListDto.java  [신규]
```

### 신규 클래스

**`ResponseSimpleParticipantDto`**
```
- userId : Long
- name   : String
정적 팩토리: of(Long userId, String name)
```

**`ResponseSimpleParticipantListDto`**
```
- roomId       : Long
- participants : List<ResponseSimpleParticipantDto>
정적 팩토리: of(Long roomId, List<ResponseSimpleParticipantDto> participants)
```

### 수정 클래스 상세

#### OpenChatMessageService — `sendImageMessage()`

```
변경 전: 이미지 N장 → OpenChatMessage 1개 → imageService.saveImages(messageId, images[N장])
변경 후:
  for each image in images:
    message = OpenChatMessage.create(roomId, userId, "", IMAGE)
    openChatMessageRepository.save(message)
    imageService.saveImages(OPEN_CHAT_MESSAGE, message.getId(), List.of(image))
    imageUrl = imageService.findStaticImageUrls(OPEN_CHAT_MESSAGE, message.getId(), request)
    dto = ResponseOpenChatMessageDto.from(message, senderName, unreadCount, imageUrl)
    messagingTemplate.convertAndSend("/sub/openchat/{roomId}", dto)
    messagingTemplate.convertAndSend("/sub/openchat/{roomId}/read", readEvent)
    results.add(dto)

  // 루프 종료 후
  room.updateLastMessage("[이미지]", lastMessage.getCreatedDate())
  lastReadMessageId 갱신 (마지막 메시지 기준)
  return List<ResponseOpenChatMessageDto>
```

반환 타입: `ResponseOpenChatMessageDto` → `List<ResponseOpenChatMessageDto>`

#### OpenChatRoomService — `getSimpleParticipants()` 신규 메서드

```
@Transactional(readOnly = true)
public ResponseSimpleParticipantListDto getSimpleParticipants(Long roomId, Long requesterId):
  1. openChatRoomRepository.findById(roomId) → 없으면 OPEN_CHAT_ROOM_NOT_FOUND
  2. openChatParticipantRepository.existsByRoomIdAndUserId(roomId, requesterId) → false면 OPEN_CHAT_ROOM_FORBIDDEN
  3. participants = openChatParticipantRepository.findAllByRoomId(roomId)
  4. userIds 추출 → userRepository.findAllById(userIds)
  5. nameMap 구성: ROLE_ADMIN → "관리자", 그 외 → user.getName() (null이면 "")
  6. dtos = participants.stream()
       .map(p -> ResponseSimpleParticipantDto.of(p.getUserId(), nameMap.get(p.getUserId())))
       .toList()
  7. return ResponseSimpleParticipantListDto.of(roomId, dtos)
```

#### OpenChatMessageController / ApiSpecification

- `sendImageMessage()` 반환 타입: `ResponseEntity<ResponseOpenChatMessageDto>` → `ResponseEntity<List<ResponseOpenChatMessageDto>>`

#### OpenChatRoomController / ApiSpecification

- `GET /{roomId}/participants/simple` 엔드포인트 추가
- `getSimpleParticipants()` 호출 후 `ResponseEntity.ok(result)` 반환

---

## 비목표

- `OpenChatMessage` 엔티티 구조 변경 없음 (content, type 필드 그대로)
- 기존 `sendImageMessage()` 의 유효성 검사 로직(`validateImageFiles`) 변경 없음
- 기존 `GET /open-chat-rooms/{roomId}/participants` API 변경 없음
- `ResponseSimpleParticipantDto` 에 isHost·isAdmin·joinedAt 미포함
- 이미지 개수·크기 제한값 변경 없음
- 메시지 읽음 처리(`lastReadMessageId`) 로직 변경 없음
- WebSocket 메시지 포맷(`ResponseOpenChatMessageDto`) 변경 없음

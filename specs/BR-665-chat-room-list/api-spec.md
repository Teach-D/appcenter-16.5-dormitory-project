# BR-665 — 채팅방 목록 API 명세서

> Base URL: `/open-chat-rooms`
> 인증: 모든 엔드포인트에 Bearer Token(JWT) 필요

---

## 채팅방 목록 조회

| 항목 | 내용 |
|------|------|
| **메서드** | `GET` |
| **경로** | `/open-chat-rooms` |
| **인증** | Bearer Token 필요 |
| **설명** | 탭(MY/ALL/DORMITORY)에 따라 채팅방 목록을 조회한다. MY 탭은 내가 참여한 openChat 방 + 룸메 매칭 채팅방을 통합 반환한다. |
| **변경 사항 (BR-665)** | ① `keyword` 파라미터 추가 ② 응답 타입 `Page<ResponseOpenChatRoomDto>` → `ResponseChatRoomListDto` ③ MY 탭에 룸메 방 통합 ④ `chatCategory` 필드 추가 |

### Request

#### Query Parameters

| 파라미터 | 타입 | 필수 | 기본값 | 설명 |
|---------|------|------|--------|------|
| `tab` | `OpenChatRoomTab` | ✅ | — | `MY` \| `ALL` \| `DORMITORY` |
| `keyword` | `String` | ❌ | — | 검색어. 빈 문자열이면 전체 조회와 동일 |
| `page` | `Int` | ❌ | `0` | 페이지 번호 (0-based) |
| `size` | `Int` | ❌ | `20` | 페이지 크기 |

#### 탭별 검색 동작

| `tab` | `keyword` 적용 대상 |
|-------|-------------------|
| `ALL` | `OpenChatRoom.name` 또는 `OpenChatRoom.description` LIKE |
| `DORMITORY` | `OpenChatRoom.name` 또는 `OpenChatRoom.description` LIKE |
| `MY` (openChat 방) | `OpenChatRoom.name` 또는 `OpenChatRoom.description` LIKE |
| `MY` (룸메 방) | 상대방 `User.name` LIKE |

---

### Response

#### 성공 응답 — `200 OK`

##### `ResponseChatRoomListDto`

| 필드 | 타입 | 설명 |
|------|------|------|
| `content` | `List<ResponseOpenChatRoomDto>` | 채팅방 목록 |
| `totalElements` | `Long` | 전체 항목 수 |
| `totalPages` | `Int` | 전체 페이지 수 |
| `pageNumber` | `Int` | 현재 페이지 번호 (0-based) |
| `pageSize` | `Int` | 페이지 크기 |
| `totalUnreadCount` | `Int` | 내 모든 채팅방 안읽음 메시지 합계. MY 탭에서만 유의미; ALL·DORMITORY = 0 |

##### `ResponseOpenChatRoomDto`

| 필드 | 타입 | 설명 |
|------|------|------|
| `roomId` | `Long` | 채팅방 ID |
| `name` | `String` | 방 이름 (룸메 방은 상대방 실명) |
| `description` | `String?` | 방 설명 (룸메 방은 `null`) |
| `scope` | `OpenChatRoomScope?` | `DORMITORY` \| `ALL`. 룸메 방은 `null` |
| `roomType` | `OpenChatRoomType?` | `OPEN` \| `DERIVED`. 룸메 방은 `null` |
| `chatCategory` | `ChatCategory` | ⭐ **NEW** `OPEN_CHAT` \| `ROOMMATE` |
| `isPublic` | `Boolean` | 공개 여부. 룸메 방은 항상 `false` |
| `hasPassword` | `Boolean` | 비밀번호 설정 여부. 룸메 방은 항상 `false` |
| `currentParticipants` | `Int` | 현재 참여 인원. 룸메 방은 항상 `2` |
| `maxParticipants` | `Int` | 최대 참여 인원. 룸메 방은 항상 `2` |
| `isJoined` | `Boolean` | 내가 참여 중 여부. MY 탭은 항상 `true` |
| `lastMessageAt` | `LocalDateTime?` | 마지막 메시지 전송 시각 (null = 메시지 없음) |
| `lastMessage` | `String?` | 마지막 메시지 내용 미리보기 (null = 메시지 없음) |
| `unreadCount` | `Int` | 이 방의 안읽음 메시지 수 |

##### 응답 예시 — MY 탭 (통합 목록)

```json
{
  "content": [
    {
      "roomId": 12,
      "name": "김철수",
      "description": null,
      "scope": null,
      "roomType": null,
      "chatCategory": "ROOMMATE",
      "isPublic": false,
      "hasPassword": false,
      "currentParticipants": 2,
      "maxParticipants": 2,
      "isJoined": true,
      "lastMessageAt": "2026-07-05T13:20:00",
      "lastMessage": "안녕하세요!",
      "unreadCount": 3
    },
    {
      "roomId": 7,
      "name": "공부방",
      "description": "조용히 공부해요",
      "scope": "ALL",
      "roomType": "OPEN",
      "chatCategory": "OPEN_CHAT",
      "isPublic": true,
      "hasPassword": false,
      "currentParticipants": 15,
      "maxParticipants": 50,
      "isJoined": true,
      "lastMessageAt": "2026-07-05T12:00:00",
      "lastMessage": "오늘 스터디 있어요",
      "unreadCount": 1
    }
  ],
  "totalElements": 2,
  "totalPages": 1,
  "pageNumber": 0,
  "pageSize": 20,
  "totalUnreadCount": 4
}
```

##### 응답 예시 — ALL 탭 (keyword 검색 포함)

```json
{
  "content": [
    {
      "roomId": 3,
      "name": "공부방",
      "description": "함께 공부해요",
      "scope": "ALL",
      "roomType": "OPEN",
      "chatCategory": "OPEN_CHAT",
      "isPublic": true,
      "hasPassword": false,
      "currentParticipants": 8,
      "maxParticipants": 30,
      "isJoined": false,
      "lastMessageAt": "2026-07-05T10:00:00",
      "lastMessage": "오늘 날씨 좋다",
      "unreadCount": 0
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "pageNumber": 0,
  "pageSize": 20,
  "totalUnreadCount": 0
}
```

---

#### 에러 응답

에러 응답 공통 형식:

```json
{
  "code": 22001,
  "name": "OPEN_CHAT_ROOM_NOT_FOUND",
  "message": "[OpenChat] 채팅방을 찾을 수 없습니다.",
  "errors": null
}
```

| 상태 코드 | ErrorCode | 발생 조건 |
|-----------|-----------|-----------|
| `400 Bad Request` | `VALIDATION_FAILED` (5001) | `tab` 파라미터 누락 또는 잘못된 enum 값 |
| `401 Unauthorized` | `JWT_ENTRY_POINT` (1007) | 인증 토큰 없음 또는 만료 |
| `403 Forbidden` | `JWT_ACCESS_DENIED` (1008) | 권한 없음 |
| `500 Internal Server Error` | `UNHANDLED_EXCEPTION` (99999) | 서버 내부 오류 |

---

## Enum 정의

### `OpenChatRoomTab`

| 값 | 설명 |
|----|------|
| `MY` | 내가 참여한 방 목록 (openChat + 룸메 통합) |
| `ALL` | 전체 공개 openChat 방 목록 |
| `DORMITORY` | 내 기숙사 scope 방 목록 (dormType NONE인 경우 빈 목록) |

### `ChatCategory` ⭐ NEW

| 값 | 설명 |
|----|------|
| `OPEN_CHAT` | `OpenChatRoom` 기반 방 (OPEN 또는 DERIVED 타입) |
| `ROOMMATE` | `RoommateChattingRoom` 기반 룸메 매칭 1:1 방 |

### `OpenChatRoomType`

| 값 | 설명 |
|----|------|
| `OPEN` | 일반 오픈채팅방 |
| `DERIVED` | 파생 톡방 (비공개 소규모 방) |

### `OpenChatRoomScope`

| 값 | 설명 |
|----|------|
| `ALL` | 전체 공개 |
| `DORMITORY` | 특정 기숙사 전용 |

---

## 추론 항목

> 코드에서 명시적으로 확인되지 않아 관례·패턴으로 추론한 항목.

- `pageSize` 기본값 `20`: Spring Boot Pageable 기본값 적용 추론
- `lastMessageAt` 직렬화 형식: `LocalDateTime` → JSON에서 배열 형태 `[2026, 7, 5, 13, 20, 0]` 또는 ISO 문자열. 프로젝트 내 다른 DTO의 Jackson 설정에 따라 달라질 수 있음 (`ResponseRoommateChatRoomDto`에 `@JsonFormat` 사용됨). 구현 시 `@JsonFormat(shape = STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")` 통일 권장

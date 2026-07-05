# BR-665 — openChat 채팅방 목록 검색·메타데이터·전체 안읽음 수 제공

## 기능 요약

`GET /open-chat-rooms` 엔드포인트에 (1) 이름/설명 키워드 검색, (2) MY 탭에 룸메 매칭 채팅방 통합, (3) 응답에 전체 안읽음 메시지 합계를 추가한다.

---

## 동작 명세

### 입력
| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `tab` | `OpenChatRoomTab` (MY/ALL/DORMITORY) | O | 기존 동일 |
| `keyword` | String | X | 검색어. 없으면 전체 조회 |
| `page`, `size` | Pageable | X | 기존 동일 |

### 처리 — 탭별 동작

**ALL 탭**
- 기존 공개 openChat 방 목록 조회
- `keyword` 있으면 `name LIKE %keyword%` OR `description LIKE %keyword%` 필터 추가

**DORMITORY 탭**
- 기존 기숙사 scope 방 목록 조회
- `keyword` 있으면 `name LIKE %keyword%` OR `description LIKE %keyword%` 필터 추가

**MY 탭**  
1. 사용자가 참여한 openChat 방 조회 (`OpenChatParticipant` 기반)
   - `keyword` 있으면 `name LIKE %keyword%` OR `description LIKE %keyword%` 필터
2. 사용자가 참여한 룸메 매칭 채팅방 조회 (`RoommateChattingRoom` 기반)
   - 본인이 나간 방(`hostLeft`/`guestLeft` = true) 제외
   - `keyword` 있으면 상대방 `User.name LIKE %keyword%` 필터
3. 두 목록을 `lastMessageAt` 내림차순 정렬 후 병합(nulls last)
4. 병합 결과에 페이지네이션 적용
5. `totalUnreadCount` 계산:
   - openChat: 각 방의 `unreadCount` 합산 (lastReadMessageId 이후 메시지 수)
   - roommate: 각 방에서 `member.id != userId AND readByReceiver = false` 인 메시지 수 합산

### 출력

**모든 탭** — 기존 `Page<ResponseOpenChatRoomDto>` 대신 `ResponseChatRoomListDto` 반환:
```
{
  "content": [ ResponseOpenChatRoomDto... ],
  "totalElements": long,
  "totalPages": int,
  "pageNumber": int,
  "pageSize": int,
  "totalUnreadCount": int   // MY 탭에서만 유의미; ALL/DORMITORY = 0
}
```

**ResponseOpenChatRoomDto 변경** — `chatCategory` 필드 추가:
```
기존 필드 유지 +
"chatCategory": "OPEN_CHAT" | "ROOMMATE"
```

---

## 도메인 데이터

### OpenChat 쪽 (기존)
- `OpenChatRoom`: id, name, description, scope, roomType(OPEN/DERIVED), isPublic, lastMessageAt, lastMessage
- `OpenChatParticipant`: roomId, userId, lastReadMessageId
- `OpenChatMessage`: id, roomId, senderId (unread 계산용)

### Roommate 쪽 (신규 통합)
- `RoommateChattingRoom`: id, host(User), guest(User), hostLeft, guestLeft
- `RoommateChattingChat`: id, roommateChattingRoom, member(User), content, readByReceiver, createdDate

### 신규 Enum
```java
// openChat 도메인 내 신규
public enum ChatCategory {
    OPEN_CHAT, ROOMMATE
}
```

---

## 비즈니스 규칙 / 제약

- `keyword`가 null 또는 빈 문자열이면 필터링 없이 전체 조회
- MY 탭 룸메 방은 `chatCategory = ROOMMATE`로 표시; `roomType`, `scope` 필드는 null
- 룸메 방의 `currentParticipants`, `maxParticipants`는 항상 2
- 룸메 방의 `isPublic = false`, `hasPassword = false`, `isJoined = true`
- 룸메 방의 `name` = 상대방 `User.name` (실명)
- 룸메 방의 `lastMessageAt` = 해당 방 마지막 `RoommateChattingChat.createdDate` (메시지 없으면 null)
- `totalUnreadCount`는 MY 탭에서만 계산; ALL/DORMITORY 탭은 항상 0 반환
- DORMITORY 탭은 유저 dormType이 NONE이면 빈 목록 반환 (기존 동일)
- **N+1 방지**: 룸메 방 lastMessage·unreadCount는 루프 내 개별 조회 금지 → bulk 쿼리로 처리

---

## 예외 · 경계 상황

| 상황 | 기대 동작 |
|---|---|
| keyword 파라미터가 공백 문자열 | 전체 조회 (trim 후 빈 문자열 처리) |
| MY 탭인데 참여 방이 0개 | `content: []`, `totalUnreadCount: 0` 반환 |
| 룸메 방에 메시지가 없음 | `lastMessageAt: null`, `lastMessage: null`, `unreadCount: 0` |
| 룸메 방 상대방이 탈퇴한 유저 | `name` = User.name (탈퇴 여부 무관, 저장된 이름 그대로 반환) |
| ALL/DORMITORY 탭에서 `totalUnreadCount` | 항상 0; 계산 로직 수행 안 함 |

---

## 비목표 (Non-goals)

- 룸메 도메인의 기존 API(`GET /roommate/chat-rooms` 등) 변경 없음
- "N분 전" 포맷팅 — `lastMessageAt` timestamp를 내려주고 클라이언트에서 계산
- 안읽음 메시지 읽음 처리(마킹) 로직 변경 없음
- openChat 방 상세 조회 API 변경 없음
- 캐싱, 알림 로직 추가 없음
- 정렬 방식 선택(최신 메시지순 고정) — 정렬 파라미터 추가 안 함

---

## 수용 기준 (Acceptance Criteria)

### AC-1 키워드 없이 ALL 탭 조회
- **Given** 공개 openChat 방 3개 존재
- **When** `GET /open-chat-rooms?tab=ALL` 호출
- **Then** 3개 방 모두 반환, `totalUnreadCount = 0`, 각 항목에 `chatCategory = OPEN_CHAT`

### AC-2 ALL 탭 키워드 이름 검색
- **Given** 공개 방 이름이 "자유채팅", "공부방", "영화방"
- **When** `GET /open-chat-rooms?tab=ALL&keyword=공부`
- **Then** "공부방" 1개만 반환

### AC-3 ALL 탭 키워드 설명 검색
- **Given** 방 이름은 "채팅방A", 설명은 "공부 얘기하는 방"
- **When** `GET /open-chat-rooms?tab=ALL&keyword=공부`
- **Then** "채팅방A" 반환 (설명 매칭)

### AC-4 DORMITORY 탭 키워드 검색
- **Given** 기숙사 방 2개, 이름 "새벽방"·"조용한방"
- **When** `GET /open-chat-rooms?tab=DORMITORY&keyword=조용`
- **Then** "조용한방" 1개만 반환

### AC-5 MY 탭 — openChat + 룸메 방 통합 반환
- **Given** 유저A가 openChat 방 1개 참여 + 룸메 채팅방 1개 존재(나가지 않음)
- **When** `GET /open-chat-rooms?tab=MY`
- **Then** `content` 길이 = 2, `chatCategory`가 각각 OPEN_CHAT·ROOMMATE

### AC-6 MY 탭 — 나간 룸메 방 제외
- **Given** 유저A가 룸메 채팅방에서 나감(hostLeft=true)
- **When** `GET /open-chat-rooms?tab=MY`
- **Then** 해당 룸메 방 목록에 미포함

### AC-7 MY 탭 — 키워드로 openChat 방 검색
- **Given** MY 탭에 openChat 방 "공부방"·"놀이방", 룸메 방(상대방 "홍길동")
- **When** `GET /open-chat-rooms?tab=MY&keyword=공부`
- **Then** "공부방"만 반환 (룸메 방·"놀이방" 제외)

### AC-8 MY 탭 — 키워드로 룸메 방 상대방 이름 검색
- **Given** MY 탭에 룸메 방(상대방 User.name="홍길동"), openChat 방 "채팅방"
- **When** `GET /open-chat-rooms?tab=MY&keyword=홍길동`
- **Then** 룸메 방만 반환

### AC-9 MY 탭 — lastMessageAt 기준 내림차순 정렬
- **Given** openChat 방 lastMessageAt=T1, 룸메 방 lastMessageAt=T2 (T2 > T1)
- **When** `GET /open-chat-rooms?tab=MY`
- **Then** 룸메 방이 첫 번째 항목

### AC-10 MY 탭 — totalUnreadCount 계산
- **Given** openChat 방 unreadCount=3, 룸메 방 안읽음 2개
- **When** `GET /open-chat-rooms?tab=MY`
- **Then** `totalUnreadCount = 5`

### AC-11 MY 탭 — roommate 방 DTO 필드값
- **Given** 룸메 방 상대방 이름="이순신", 메시지 없음
- **When** `GET /open-chat-rooms?tab=MY`
- **Then** roommate 항목의 `name="이순신"`, `isPublic=false`, `currentParticipants=2`, `maxParticipants=2`, `lastMessageAt=null`

### AC-12 keyword 공백 처리
- **Given** 방 3개
- **When** `GET /open-chat-rooms?tab=ALL&keyword= ` (공백)
- **Then** 3개 모두 반환 (필터링 없음)

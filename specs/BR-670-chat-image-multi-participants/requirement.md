# BR-670 채팅방 이미지 다중 전송 및 참여자 단순 목록 API

## 기능 요약

채팅방에서 이미지 여러 장을 한 번의 요청으로 전송할 때 **이미지 1장 = 메시지 1개**로 저장·브로드캐스트하도록 변경하고,
채팅방 참여자의 이름과 id만 반환하는 단순 목록 API를 새로 추가한다.

---

## 동작 명세

### 1. 이미지 다중 전송 (기존 API 동작 변경)

**엔드포인트**: `POST /open-chat-rooms/{roomId}/messages/image`

**정상 흐름**
1. 클라이언트가 `images` 파트에 1~5장의 이미지를 담아 요청
2. 서버가 이미지 유효성 검사 (개수·크기·확장자·MIME)
3. 이미지 N장 → `OpenChatMessage` N개 생성 (각 메시지의 `content = ""`, `type = IMAGE`)
4. 각 메시지에 이미지 1장씩 저장 (`ImageType.OPEN_CHAT_MESSAGE`)
5. 각 메시지마다 순서대로 `/sub/openchat/{roomId}` 브로드캐스트
6. 마지막 메시지 기준으로 `lastMessage = "[이미지]"`, `lastMessageTime` 갱신
7. WebSocket 구독자 + 발신자의 `lastReadMessageId` → 마지막 메시지 ID로 갱신
8. 응답: 생성된 메시지 DTO 리스트 (순서: 전송 순)

**응답 타입 변경**
- 기존: `ResponseOpenChatMessageDto` (단건)
- 변경: `List<ResponseOpenChatMessageDto>` (N건)

---

### 2. 참여자 단순 목록 API (신규)

**엔드포인트**: `GET /open-chat-rooms/{roomId}/participants/simple`

**정상 흐름**
1. JWT 인증된 사용자가 요청
2. 채팅방 존재 확인
3. 요청자가 해당 채팅방 참여자인지 확인
4. `OpenChatParticipant` 목록 조회 → userId 리스트 추출
5. `User` 테이블에서 name 조회
6. 응답: `{ roomId, participants: [{ userId, name }] }`

---

## 도메인 데이터

### 이미지 다중 전송

| 변경 대상 | 기존 | 변경 후 |
|-----------|------|---------|
| `OpenChatMessage` 저장 수 | 이미지 N장 → 1개 메시지 | 이미지 N장 → N개 메시지 |
| 이미지 연결 | 1 메시지 : N 이미지 | 1 메시지 : 1 이미지 |
| HTTP 응답 타입 | `ResponseOpenChatMessageDto` | `List<ResponseOpenChatMessageDto>` |
| WebSocket 브로드캐스트 | 1회 | N회 (이미지마다) |

### 참여자 단순 목록 DTO

```
ResponseSimpleParticipantDto
  - userId  : Long
  - name    : String

ResponseSimpleParticipantListDto
  - roomId       : Long
  - participants : List<ResponseSimpleParticipantDto>
```

---

## 비즈니스 규칙 / 제약

### 이미지 전송
- 이미지 최소 1장, 최대 5장 (현행 유지)
- 파일당 최대 10 MB (현행 유지)
- 허용 확장자: `.jpg`, `.jpeg`, `.png`, `.gif`, `.webp` (현행 유지)
- 허용 MIME: `image/jpeg`, `image/png`, `image/gif`, `image/webp` (현행 유지)
- 이미지 저장 순서 = 요청에서의 파일 순서

### 참여자 단순 목록
- JWT 인증 필수
- 요청자가 해당 채팅방 참여자여야 함 (비참여자 → 403)
- `ROLE_ADMIN` 유저의 name은 기존 `getParticipants` 규칙과 동일하게 `"관리자"` 반환

---

## 예외 · 경계 상황

| 상황 | 응답 |
|------|------|
| `images` 파트 없음 또는 빈 리스트 | 400 `OPEN_CHAT_IMAGE_EMPTY` |
| 이미지 6장 이상 | 400 `OPEN_CHAT_IMAGE_COUNT_EXCEEDED` |
| 파일 크기 초과·형식 불일치 | 400 `IMAGE_INVALID_FORMAT` |
| 채팅방 없음 | 404 `OPEN_CHAT_ROOM_NOT_FOUND` |
| 발신자가 참여자 아님 | 403 `OPEN_CHAT_NOT_PARTICIPANT` |
| 참여자 조회 시 채팅방 없음 | 404 `OPEN_CHAT_ROOM_NOT_FOUND` |
| 참여자 조회 요청자가 비참여자 | 403 `OPEN_CHAT_ROOM_FORBIDDEN` |

---

## 비목표 (Non-goals)

- 기존 `GET /open-chat-rooms/{roomId}/participants` API 변경 없음
- 이미지 전송 개수 제한값(5) 또는 크기 제한(10MB) 변경 없음
- 이미지 삭제·수정 기능
- 메시지 읽음 처리 로직 변경
- WebSocket 메시지 포맷 변경 (기존 `ResponseOpenChatMessageDto` 그대로)
- 참여자 단순 목록에 isHost·isAdmin·joinedAt 포함

---

## 수용 기준 (Acceptance Criteria)

### 이미지 다중 전송

**AC-1** Given 참여자가 이미지 3장을 전송하면  
When `POST /open-chat-rooms/{roomId}/messages/image` 호출 시  
Then `OpenChatMessage` 3개가 생성되고, 각각 이미지 1장씩 연결되며, 응답은 크기 3인 리스트이다.

**AC-2** Given 참여자가 이미지 1장을 전송하면  
When 요청 시  
Then 메시지 1개 생성, 응답은 크기 1인 리스트이다.

**AC-3** Given 이미지 6장을 전송하면  
When 요청 시  
Then 400 `OPEN_CHAT_IMAGE_COUNT_EXCEEDED` 반환, 메시지 저장 없음.

**AC-4** Given 비참여자가 이미지를 전송하면  
When 요청 시  
Then 403 `OPEN_CHAT_NOT_PARTICIPANT` 반환.

**AC-5** Given 이미지 3장을 전송하면  
When 처리 완료 후  
Then WebSocket `/sub/openchat/{roomId}` 로 브로드캐스트가 3회 발생하며, 마지막 메시지 기준으로 룸의 `lastMessage`가 `[이미지]`로 갱신된다.

### 참여자 단순 목록

**AC-6** Given 참여자가 요청하면  
When `GET /open-chat-rooms/{roomId}/participants/simple` 호출 시  
Then `{ roomId, participants: [{ userId, name }] }` 형태의 응답이 반환된다.

**AC-7** Given 비참여자가 요청하면  
When 호출 시  
Then 403 `OPEN_CHAT_ROOM_FORBIDDEN` 반환.

**AC-8** Given 존재하지 않는 roomId로 요청하면  
When 호출 시  
Then 404 `OPEN_CHAT_ROOM_NOT_FOUND` 반환.

**AC-9** Given ROLE_ADMIN 유저가 참여자 목록에 있으면  
When 호출 시  
Then 해당 유저의 name은 `"관리자"`로 반환된다.

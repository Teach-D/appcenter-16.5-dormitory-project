# BR-700 오픈채팅방 정보 수정 (방장 전용)

## 기능 요약

방장(`isHost = true`)인 참여자가 자신이 속한 오픈채팅방(OPEN·DERIVED 타입)의 이름·설명·범위·최대인원·비밀번호·공개여부를 수정할 수 있다.

---

## 동작 명세

1. 클라이언트가 `PATCH /open-chat-rooms/{roomId}` 로 수정 요청을 보낸다.
2. 서버는 요청자가 해당 방의 참여자인지 확인한다.
3. 참여자라면 `isHost` 여부를 확인한다.
4. 방 타입이 OPEN 또는 DERIVED인지 확인한다.
5. `maxParticipants` 감소 요청 시 현재 참여자 수보다 작은지 검증한다.
6. 검증 통과 시 전달된 필드만 엔티티에 반영하고 저장한다.
7. `204 No Content` 응답을 반환한다.

### 부분 업데이트 처리

- 요청 DTO에서 `null`인 필드는 수정하지 않는다 (부분 업데이트).
- `password`는 빈 문자열(`""`)을 보내면 비밀번호 해제(null로 변경)로 간주한다.
- `isPublic`은 null이면 현행 유지한다.

---

## 도메인 데이터

**OpenChatRoom** 엔티티에서 수정 가능한 필드:

| 필드            | 타입               | 제약               |
|-----------------|--------------------|-------------------|
| `name`          | String             | NotBlank, max 30  |
| `description`   | String             | nullable, max 100 |
| `scope`         | OpenChatRoomScope  | DORMITORY \| ALL   |
| `maxParticipants` | int              | 2 이상 100 이하, 현재 참여자 수 이상 |
| `password`      | String             | nullable, max 50  |
| `isPublic`      | boolean            | -                 |

**OpenChatParticipant** — `isHost` 필드로 방장 여부 식별.

---

## 비즈니스 규칙 / 제약

1. 요청자는 해당 방의 참여자(`OpenChatParticipant`)여야 한다.
2. 요청자는 방장(`isHost = true`)이어야 한다.
3. 대상 방은 `roomType`이 `OPEN` 또는 `DERIVED`여야 한다. (`PERSONAL`, `OFFICIAL` 수정 불가)
4. `maxParticipants` 는 현재 실제 참여자 수 이상이어야 한다.
5. `name` 은 null 또는 공백만으로 구성될 수 없다.
6. `scope`는 `OpenChatRoomScope` 열거값(`DORMITORY`, `ALL`)만 허용한다.

---

## 예외 · 경계 상황

| 상황                                         | 기대 동작                                          |
|----------------------------------------------|----------------------------------------------------|
| 방이 존재하지 않음                           | `OPEN_CHAT_ROOM_NOT_FOUND` (404)                   |
| 요청자가 참여자가 아님                       | `OPEN_CHAT_PARTICIPANT_NOT_FOUND` (404)            |
| 요청자가 방장이 아님                         | `OPEN_CHAT_NOT_HOST` (403) — 신규 ErrorCode        |
| `roomType`이 PERSONAL 또는 OFFICIAL          | `OPEN_CHAT_ROOM_FORBIDDEN` (403)                   |
| `maxParticipants` < 현재 참여자 수           | `OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL` (400) — 신규 ErrorCode |
| `name`이 blank                               | 400 BAD_REQUEST (Bean Validation)                  |
| `maxParticipants` < 2 또는 > 100             | 400 BAD_REQUEST (Bean Validation)                  |
| 수정 필드 전부 null (아무 변경 없음)         | 200/204 정상 처리 (no-op)                          |

### 신규 ErrorCode

| 코드                              | HTTP  | 번호  | 메시지                                     |
|-----------------------------------|-------|-------|--------------------------------------------|
| `OPEN_CHAT_NOT_HOST`              | 403   | 22025 | [OpenChat] 방장만 채팅방 정보를 수정할 수 있습니다. |
| `OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL` | 400 | 22026 | [OpenChat] 최대 인원은 현재 참여자 수 이상이어야 합니다. |

---

## 비목표 (Non-goals)

- 방 타입(`roomType`) 변경 불가.
- `isOfficial` 변경 불가.
- `createdBy`, `creatorDormitory` 변경 불가.
- PERSONAL·OFFICIAL 방 수정 지원 안 함.
- 수정 이벤트 WebSocket 브로드캐스트 안 함.
- 수정 이력 저장 안 함.
- 권한 위임(방장 변경) 로직은 기존 host transfer API 그대로.

---

## 수용 기준 (Acceptance Criteria)

- **AC-1** Given 방장이 OPEN 방의 name을 수정할 때, When PATCH 요청을 보내면, Then 204를 반환하고 DB의 name이 변경된다.
- **AC-2** Given 방장이 DERIVED 방의 description·maxParticipants를 수정할 때, When PATCH 요청을 보내면, Then 204를 반환하고 해당 필드가 변경된다.
- **AC-3** Given 방장이 아닌 일반 참여자가 수정을 시도할 때, When PATCH 요청을 보내면, Then 403 OPEN_CHAT_NOT_HOST를 반환한다.
- **AC-4** Given 참여자가 아닌 사용자가 수정을 시도할 때, When PATCH 요청을 보내면, Then 404 OPEN_CHAT_PARTICIPANT_NOT_FOUND를 반환한다.
- **AC-5** Given roomType이 PERSONAL 또는 OFFICIAL인 방에 방장이 수정을 시도할 때, When PATCH 요청을 보내면, Then 403 OPEN_CHAT_ROOM_FORBIDDEN을 반환한다.
- **AC-6** Given 현재 참여자 3명인 방에 maxParticipants=2로 줄이려 할 때, When PATCH 요청을 보내면, Then 400 OPEN_CHAT_MAX_PARTICIPANTS_TOO_SMALL을 반환한다.
- **AC-7** Given 요청 DTO에서 name만 전달할 때 (나머지 null), When PATCH 요청을 보내면, Then name만 변경되고 나머지 필드는 그대로다.
- **AC-8** Given password에 빈 문자열을 전달할 때, When PATCH 요청을 보내면, Then password가 null로 변경(비밀번호 해제)된다.
- **AC-9** Given 존재하지 않는 방 ID로 요청할 때, When PATCH 요청을 보내면, Then 404 OPEN_CHAT_ROOM_NOT_FOUND를 반환한다.

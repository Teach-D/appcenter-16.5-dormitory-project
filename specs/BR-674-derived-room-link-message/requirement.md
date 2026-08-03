# BR-674 — 단체 채팅방 생성 시 원본 톡방에 채팅방 링크 메시지 전송

## 기능 요약
단체 채팅방(DERIVED)을 생성할 때 `originRoomId`를 필수로 받아, 원본 톡방에 새 방의 정보(이름·설명·최대인원)와 입장 링크를 담은 `ROOM_LINK` 타입 메시지를 WebSocket으로 브로드캐스트한다.

## 동작 명세

**정상 흐름:**
1. 클라이언트가 `POST /open-chat-rooms/derived` 요청에 `originRoomId`(필수) 포함
2. originRoom 존재 여부 확인 → 없으면 404
3. 요청자가 originRoom의 참여자인지 확인 → 아니면 403
4. 파생 채팅방(DERIVED) 생성 (기존 로직 동일)
5. `OpenChatMessage`를 `roomId=originRoomId`, `senderId=요청자ID`, `type=ROOM_LINK`, `content=JSON` 으로 저장
   - content JSON: `{"derivedRoomId":N,"roomName":"...","description":"...","maxParticipants":N}`
6. originRoom의 `lastMessage` / `lastMessageAt` 업데이트
7. `/sub/openchat/{originRoomId}`로 `ResponseOpenChatMessageDto` 브로드캐스트
   - `linkedRoomId`, `linkedRoomName`, `linkedRoomDescription`, `linkedRoomMaxParticipants` 필드 포함

**응답:** `ResponseDerivedRoomCreatedDto` (roomId) — 기존과 동일

## 도메인 데이터

- `RequestCreateDerivedRoomDto`: 기존 필드에 `@NotNull Long originRoomId` 추가
- `OpenChatMessageType`: `ROOM_LINK` 값 추가
- `ResponseOpenChatMessageDto`: 아래 필드 추가 (ROOM_LINK 타입일 때만 값, 나머지는 null)
  - `Long linkedRoomId`
  - `String linkedRoomName`
  - `String linkedRoomDescription`
  - `Integer linkedRoomMaxParticipants`
- content 저장 형식: JSON 문자열, 기존 TEXT 컬럼 그대로 사용

## 비즈니스 규칙 / 제약
- `originRoomId`는 required — null이면 400 BAD_REQUEST (`@NotNull` 검증)
- 요청자가 originRoom 참여자가 아니면 403 (`OPEN_CHAT_PARTICIPANT_NOT_FOUND`)
- originRoom이 존재하지 않으면 404 (`OPEN_CHAT_ROOM_NOT_FOUND`)
- 파생 방 생성 실패 시 originRoom 메시지도 저장하지 않음 (동일 트랜잭션)
- originRoom의 방 타입(OPEN/DERIVED/PERSONAL) 제한 없음 — 어느 방에서든 파생 방 생성 가능

## 예외 · 경계 상황
- description이 null이어도 JSON content에 `"description":null`로 직렬화 → content 500자 초과 없음 (description 최대 100자)
- 파생 방 생성 후 WebSocket 전송 실패: 메시지는 DB에 저장되어 있으므로 REST 조회로 복구 가능

## 비목표 (Non-goals)
- originRoom 참여자들에게 FCM 푸시 알림 전송
- ROOM_LINK 메시지 클릭 시 입장 검증·처리 (프론트엔드 라우팅 및 기존 입장 API 재사용)
- 파생 방 정보(isPublic/password) originRoom 메시지에 노출

## 수용 기준 (Acceptance Criteria)

- Given: 유저A가 originRoom(id=1) 참여자이고 `POST /open-chat-rooms/derived` with originRoomId=1  
  When: 파생 방 생성 성공  
  Then: `/sub/openchat/1`로 type=ROOM_LINK, linkedRoomId=생성된방ID 포함 메시지 브로드캐스트

- Given: originRoomId가 누락된(null) 요청  
  When: `POST /open-chat-rooms/derived`  
  Then: 400 BAD_REQUEST

- Given: 요청자가 originRoom 참여자가 아닐 때  
  When: `POST /open-chat-rooms/derived`  
  Then: 403 FORBIDDEN

- Given: originRoomId가 존재하지 않는 방 ID일 때  
  When: `POST /open-chat-rooms/derived`  
  Then: 404 NOT_FOUND

- Given: 파생 방 생성 성공  
  When: originRoom 메시지 목록 조회  
  Then: type=ROOM_LINK, linkedRoomName·linkedRoomDescription·linkedRoomMaxParticipants 포함 메시지 존재

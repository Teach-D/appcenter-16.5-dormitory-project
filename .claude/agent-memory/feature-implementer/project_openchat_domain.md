---
name: openchat-domain-implementation
description: openChat 도메인 Phase 1+2 구현 완료 기록 — ErrorCode, Flyway, Entity, Repository, DTO, Service, Controller, WebSocket
metadata:
  type: project
---

openChat 도메인 Phase 1 구현 완료 (2026-06-07).
openChat 도메인 Phase 2 구현 완료 (2026-06-08) — WebSocket + STOMP 실시간 채팅.
openChat 도메인 Phase 3 구현 완료 (2026-06-08) — 파생 톡방(비공개 그룹 채팅) + 초대 기능.

## ErrorCode 추가 범위 (코드 22001~22010)
- OPEN_CHAT_ROOM_NOT_FOUND (NOT_FOUND, 22001)
- OPEN_CHAT_ROOM_FORBIDDEN (FORBIDDEN, 22002)
- OPEN_CHAT_ROOM_FULL (BAD_REQUEST, 22003)
- OPEN_CHAT_PARTICIPANT_NOT_FOUND (NOT_FOUND, 22004)
- OPEN_CHAT_NOT_PARTICIPANT (FORBIDDEN, 22005) — 채팅 내역 조회 권한 없음
- OPEN_CHAT_INVITATION_NOT_FOUND (NOT_FOUND, 22006) — Phase 3
- OPEN_CHAT_INVITATION_INVALID_TARGET (BAD_REQUEST, 22007) — invitee가 부모 방 비참여자
- OPEN_CHAT_INVITATION_ALREADY_EXISTS (CONFLICT, 22008) — PENDING 중복 초대
- OPEN_CHAT_PARTICIPANT_ALREADY_EXISTS (CONFLICT, 22009) — 이미 채팅방 참여자
- VALIDATION_ERROR (BAD_REQUEST, 22010) — 유효하지 않은 상태 전이 등

## Flyway 마이그레이션
- V7__open_chat_schema.sql: open_chat_room, open_chat_participant, open_chat_message 테이블 생성 + 공식 방 9개 INSERT
- V8__open_chat_phase2.sql: last_message, last_message_at 컬럼 + last_read_message_id 컬럼 추가
- V9__open_chat_derived_room.sql: open_chat_room에 room_type/parent_room_id 컬럼 추가, open_chat_invitation 테이블 생성

## 패키지 구조
- com.example.appcenter_project.domain.openChat.*
- enums: OpenChatRoomScope (DORMITORY, ALL), OpenChatMessageType (TEXT, IMAGE, SYSTEM)
- entity: OpenChatRoom (updateLastMessage), OpenChatParticipant (lastReadMessageId, updateLastReadMessageId), OpenChatMessage
- repository: OpenChatRoomRepository (findByIdWithLock)
- repository: OpenChatParticipantRepository (@Modifying updateLastReadMessageId JPQL 추가)
- repository: OpenChatMessageRepository (extends OpenChatMessageQuerydslRepository 추가)
- dto/request: RequestCreateOpenChatRoomDto, RequestOpenChatMessageDto
- dto/response: ResponseOpenChatRoomDto (lastMessage+unreadCount 필드, from() 오버로드)
- dto/response: ResponseOpenChatMessageDto, ResponseOpenChatMessageListDto

## QueryDSL Repository 구조
### OpenChatParticipantQuerydslRepository
- countByRoomIds(roomIds): roomId → count 맵
- findJoinedRoomIds(userId, roomIds): Set<Long>
- findLastReadMessageIdsByUserId(userId, roomIds): roomId → lastReadMessageId 맵
- countReadByRoomIdAndMessageId(roomId, messageId): lastReadMessageId >= messageId 인 참가자 수

### OpenChatMessageQuerydslRepository (신규)
- findByRoomIdWithCursor(roomId, lastMessageId, size): 커서 기반, DESC 조회 후 reverse (ASC 반환)
- findLatestMessageIdByRoomId(roomId): Optional<Long>
- countByRoomIdAndIdGreaterThan(roomId, lastReadMessageId): null이면 전체 count

### OpenChatRoomQuerydslRepositoryImpl
- findMyRooms: lastMessageAt DESC NULLS LAST, createdDate DESC 정렬

## Phase 2 서비스/컨트롤러
- OpenChatMessageService: sendMessage, sendSystemMessage(@Propagation.REQUIRES_NEW), getMessages, calculateUnreadCount
- OpenChatRoomService: joinRoom+leaveRoom에 sendSystemMessage 연동 (@Lazy 순환 의존 처리), MY 탭 unreadCount 계산
- OpenChatMessageController: @MessageMapping("/openchat/socketchat") + GET /{roomId}/messages
- OpenChatWebSocketEventListener: /sub/openchat/ prefix 구독 시 lastReadMessageId 갱신

## 핵심 비즈니스 규칙 (Phase 1)
- BR-02: dormType=NONE → 빈 페이지 즉시 반환
- BR-03: scope=DORMITORY + dormType 불일치 → OPEN_CHAT_ROOM_FORBIDDEN
- BR-04: currentParticipants >= maxParticipants → OPEN_CHAT_ROOM_FULL
- BR-05: 이미 참여 중 재입장 → 멱등, save 없이 방 정보 반환
- BR-06: 방장 나가기 → 다음 참여자(joinedAt ASC)에게 방장 이전, 0명+비공식 → 방 삭제
- BR-07: is_official=TRUE → 마지막 참여자 나가도 방 유지

## 핵심 패턴 (Phase 2)
- 순환 의존: OpenChatRoomService → OpenChatMessageService (@Lazy, 수동 @Autowired 생성자)
- unreadCount = total participants - participants whose lastReadMessageId >= messageId
- WebSocket 인증: WebSocketAuthInterceptor가 JWT → sessionAttributes["userId"] 저장
- 시스템 메시지: senderId=0L, type=SYSTEM

## SecurityConfig
- /open-chat-rooms/** → authenticated() 추가 (Phase 1에서 완료)

## Phase 3 파생 톡방 (2026-06-08)
- OpenChatRoomType enum: OPEN, DERIVED (open_chat_room.room_type 컬럼)
- OpenChatInvitationStatus enum: PENDING, ACCEPTED, REJECTED
- OpenChatRoom.createDerived() 팩토리: scope=ALL, isOfficial=false, roomType=DERIVED
- 기존 OpenChatRoom.create()에 roomType=OPEN 자동 설정 추가
- OpenChatInvitation entity: create(), accept(), reject() 메서드 (상태 전이 검증 내장)
- OpenChatInvitationRepository: JpaRepository (복잡 쿼리 없어 Querydsl 불필요)
- OpenChatParticipantRepository에 findByRoomId() 추가
- OpenChatInvitationService: createDerivedRoom, sendInvitation, acceptInvitation, rejectInvitation, getParticipants
- OpenChatDerivedRoomController: POST /open-chat-rooms/derived
- OpenChatInvitationController: POST/GET /open-chat-rooms/{roomId}/invitations/** 및 participants

## 핵심 비즈니스 규칙 (Phase 3)
- BR-01: 파생 톡방 생성 시 부모 방 참여자 여부 확인 (비참여자 → FORBIDDEN)
- BR-02: 부모 방이 이미 DERIVED 타입 → VALIDATION_ERROR (2레벨 파생 불허)
- BR-03: inviter는 파생 톡방 참여자여야 함
- BR-04: invitee는 부모 방 참여자여야 함 (parentRoomId 기반)
- BR-05: invitee가 이미 파생 톡방 참여자 → PARTICIPANT_ALREADY_EXISTS
- BR-06: PENDING 중복 초대 → INVITATION_ALREADY_EXISTS
- BR-07: REJECTED 후 재초대 허용 (PENDING만 체크)
- BR-08: 수락 시 findByIdWithLock (비관적 락) → 정원 초과 방지
- BR-09: 수락/거절 시 본인(inviteeUserId) 여부 확인
- S-EC: accept/reject 시 PENDING이 아닌 상태 → VALIDATION_ERROR (invitation.accept()/reject() 내부 검증)

**Why:** Phase 3 파생 톡방 비공개 그룹 채팅 구현 완료
**How to apply:** openChat 도메인 추가 기능 개발 시 이 구조를 기반으로 확장

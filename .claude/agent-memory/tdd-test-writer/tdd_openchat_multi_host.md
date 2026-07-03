---
name: openChat 다중 방장 TDD
description: #643 오픈채팅 다중 방장 시스템 43개 테스트 케이스, BR 커버리지, 신규 ErrorCode
type: project
---

## 브랜치
teach/feat/openchat-multi-host-643

## 테스트 파일 위치
- `src/test/java/.../domain/openChat/fixture/OpenChatMultiHostFixture.java`
- `src/test/java/.../domain/openChat/service/OpenChatMultiHostServiceTest.java`
- `src/test/java/.../domain/openChat/controller/OpenChatMultiHostControllerTest.java`
- `src/test/java/.../domain/openChat/repository/OpenChatParticipantMultiHostRepositoryTest.java`

## BR 커버리지
- BR-03: `should_throw_CustomException_when_requester_is_not_host_nor_admin`
- BR-05: `should_leave_successfully_when_multiple_hosts_and_no_new_host_provided`
- BR-06 (예외): `should_throw_CustomException_when_sole_host_leaves_without_new_host`
- BR-06 (성공): `should_delegate_and_leave_atomically_when_sole_host_provides_new_host_br06`
- BR-07 (비방장): `should_throw_CustomException_when_non_host_deletes_room`
- BR-07 (공식 방): `should_throw_CustomException_when_non_admin_host_deletes_official_room`
- BR-08: `should_create_participant_with_isHost_false_when_host_rejoins`
- BR-09: `should_set_isHost_true_when_admin_enters_official_room`
- BR-10: `should_grant_host_when_requester_is_admin_not_participant`
- BR-11: `should_keep_host_grant_when_fcm_fails`
- BR-12: `should_grant_host_in_derived_room`

## 신규 ErrorCode (미구현 — 주석 처리됨)
- `ErrorCode.OPEN_CHAT_ALREADY_HOST` — 400 BAD_REQUEST
- `ErrorCode.OPEN_CHAT_SOLE_HOST_CANNOT_LEAVE` — 400 BAD_REQUEST

## 신규 서비스 메서드 (미구현 — [PLACEHOLDER] 주석)
- `OpenChatRoomService.grantHost(Long roomId, Long requesterId, Long targetUserId): void`
- `OpenChatRoomService.leaveRoom(Long roomId, Long userId, Long newHostUserId): boolean`
- `OpenChatRoomService.deleteRoom(Long roomId, Long requesterId): void`
- `OpenChatRoomService.getParticipants(Long roomId, Long requesterId): ResponseOpenChatParticipantListDto`

## 신규 레포지토리 메서드 (미구현 — [PLACEHOLDER] 주석)
- `existsByRoomIdAndUserIdAndIsHost(Long, Long, boolean): boolean`
- `countByRoomIdAndIsHost(Long, boolean): long`
- `findAllByRoomIdWithLock(Long): List<OpenChatParticipant>` — @Lock(PESSIMISTIC_WRITE)
- `findByRoomIdAndUserId(Long, Long): Optional<OpenChatParticipant>`
- `findAllByRoomId(Long): List<OpenChatParticipant>`

## 엔티티 변경 (미구현)
- `OpenChatParticipant.isHost` 필드 추가 (boolean)
- `OpenChatParticipant.create(Long roomId, Long userId, boolean isHost)` 팩토리
- `OpenChatRoom.hostUserId` 필드 제거 (ADR-03)
- `OpenChatRoom.isOfficial()` getter

**왜:** 기존 단일 방장(hostUserId) 구조를 participant의 isHost 필드 기반 다중 방장으로 전환

**적용 방법:** 구현 에이전트가 이 이슈 작업 시 참고

---
name: audit-openchat-multi-host-2026-06-16
description: 오픈채팅 다중 방장(#643) 구현 보안 감사 결과 요약 (초기 감사 + 재감사 2026-06-16)
metadata:
  type: project
---

감사 대상: OpenChatRoomService, OpenChatRoomController, OpenChatInvitationService, OpenChatParticipantRepository, OpenChatParticipant 엔티티, OpenChatRoom 엔티티, V13 마이그레이션

---

## 1차 감사 결과 (2026-06-16 초기)

**발견된 취약점:**

1. [High] leaveRoom 비방장 → 무단 방장 부여: participant.isHost() 체크 전에 newHostUserId 분기가 실행. 비방장이 타인에게 방장 부여 후 자신이 퇴장 가능.
2. [High] leaveRoom 비관적 락 무효화: findAllByRoomIdWithLock(roomId) 반환값 미사용. 이후 findByRoomIdAndUserId는 잠금 없는 별도 SELECT.
3. [Medium] grantHost 자기 자신 방장 부여 시 혼동 에러: targetUserId == requesterId 전용 에러코드 없음 (ALREADY_HOST로 처리됨, 피해 제한적).
4. [Medium] deleteRoom ADMIN 우회: ADMIN role 체크 없음. ADMIN도 participant row 없으면 삭제 불가.

---

## 2차 재감사 결과 (2026-06-16 수정 후)

### 이전 High 이슈 해결 여부

**High #1 (비방장 무단 방장 부여) — 해결됨**
- OpenChatRoomService.java:144-147: `if (newHostUserId != null)` 분기 진입 즉시 `if (!participant.isHost()) throw OPEN_CHAT_ROOM_FORBIDDEN` 체크 추가.
- 비방장은 newHostUserId 파라미터를 전달해도 즉시 403. 완전 해결.

**High #2 (비관적 락 반환값 버림) — 해결됨**
- OpenChatRoomService.java:149-168: `findAllByRoomIdWithLock(roomId)` 결과를 `lockedParticipants`에 저장.
- newHostParticipant 및 lockedSelf 모두 lockedParticipants.stream()으로 탐색. 별도 SELECT 없음.
- 완전 해결.

### 신규 발견 취약점

**[Medium] leaveRoom newHostUserId == null 경로: 방장 퇴장 시 비관적 락 부재**
- OpenChatRoomService.java:177-184
- `countByRoomIdAndIsHost(roomId, true)` (잠금 없는 COUNT) → `delete(participant)` 순서.
- 방장 2명이 동시에 newHostUserId 없이 leaveRoom 호출 시, 두 트랜잭션 모두 hostCount==2를 읽고 통과 → 두 방장 모두 삭제 → 방장 0명 상태 발생 가능.
- 이 경로에는 findAllByRoomIdWithLock 호출 없음.
- 피해: 방장 없는 채팅방 → deleteRoom, grantHost 모두 403으로 영구 잠금.

**[Medium] 파생 방 생성자 is_host 불일치**
- OpenChatInvitationService.java:62: `OpenChatParticipant.create(savedRoom.getId(), requesterId, LocalDateTime.now())` 사용 → isHost=false
- OpenChatRoom.createDerived(line 88-101): hostUserId = requesterId로 설정됨.
- V13 마이그레이션(UPDATE WHERE p.user_id = r.host_user_id)이 적용되면 기존 파생 방은 정상화되지만, 신규 파생 방은 생성 즉시 is_host=false 상태.
- 결과: 파생 방 생성자가 deleteRoom(existsByRoomIdAndUserIdAndIsHost 체크), grantHost 사용 불가.
- 추가 위험: leaveRoom에서 participant.isHost()==false이므로 SOLE_HOST 체크를 건너뛰어 방장(역할상)이 즉시 퇴장 가능.

### 통과 항목 (수정 후 유지)
- grantHost: requester isHost 체크 정상, cross-room attack 방어됨.
- grantHost: target이 해당 방 참여자인지 findByRoomIdAndUserId(roomId, targetUserId) 검증.
- deleteRoom: isHost 체크 → isOfficial 체크 순서 정상.
- leaveRoom newHostUserId != null 경로: lockedParticipants로 newHostParticipant + lockedSelf 모두 락 범위 내 처리.
- leaveRoom: newHostUserId.equals(userId) 자기 자신 위임 차단 (line 152).
- SecurityConfig: /open-chat-rooms/** authenticated 적용.
- @Valid: RequestCreateOpenChatRoomDto 적용됨.
- DTO 직접 노출 없음.

**Why:** leaveRoom의 newHostUserId==null 방장 퇴장 경로는 수정 범위에서 제외됨. 파생 방 isHost 불일치는 V13 이전부터 존재하던 구조적 문제.

---

## 3차 재감사 결과 (2026-06-16 2차 수정 후)

### N#1 해결 여부 (leaveRoom 일반 퇴장 경로 비관적 락)

**해결됨.**
- OpenChatRoomService.java:177-178: `findAllByRoomIdWithLock(roomId)` 결과를 `lockedParticipants`에 저장.
- line 181: `lockedParticipants.stream().filter(OpenChatParticipant::isHost).count()`로 방장 수 계산 — 잠금 범위 내 처리.
- line 187-190: `lockedSelf`도 `lockedParticipants.stream()`에서 탐색 후 삭제.
- `countByRoomIdAndIsHost` 호출 완전 제거 확인.

주의: line 140-142에서 잠금 없는 `findByRoomIdAndUserId`로 `participant`를 먼저 획득하고, line 180에서 `participant.isHost()`를 체크함. 동일 트랜잭션 내 JPA 1차 캐시에 의해 `findAllByRoomIdWithLock` 실행 시 같은 row에 대한 락은 걸리지만 반환 객체는 캐시된 인스턴스일 수 있음. 그러나 line 180의 `participant.isHost()` 체크는 분기 진입 여부만 결정하고, 실제 방장 수 결정(`hostCount`)은 `lockedParticipants`에서 계산하므로 경쟁 조건 위험 없음. 통과.

### N#2 해결 여부 (createDerivedRoom isHost=true)

**해결됨.**
- OpenChatInvitationService.java:62: `OpenChatParticipant.create(savedRoom.getId(), requesterId, true)` — boolean 오버로드(line 48-56) 호출.
- `isHost = true`로 명시적 설정 확인.
- 파생 방 생성자가 deleteRoom, grantHost, leaveRoom(방장 체크) 모두 정상 사용 가능.

### 신규 취약점

없음. 수정으로 인한 신규 취약점 없음.

### 최종 상태

Medium #1 (leaveRoom 일반 퇴장 비관적 락 부재) — 해결됨
Medium #2 (파생 방 is_host 불일치) — 해결됨
보안 점수: 통과

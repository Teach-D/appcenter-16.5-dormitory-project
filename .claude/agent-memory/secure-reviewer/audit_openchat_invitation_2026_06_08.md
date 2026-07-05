---
name: audit-openchat-invitation-2026-06-08
description: 파생 톡방·초대 기능(OpenChatInvitationService, OpenChatDerivedRoomController 등) 보안 감사 결과 — 2026-06-08
metadata:
  type: project
---

감사 대상: 파생 톡방(DERIVED room) 생성·초대 발송·수락·거절 흐름

**발견된 취약점 요약:**

1. [Critical] IDOR — acceptInvitation/rejectInvitation에서 invitation.roomId vs 경로 roomId 불일치 미검증.
   공격자가 타인의 초대(invitationId)를 자신의 방(roomId)으로 수락 시도하면 cross-room 조작 가능.

2. [High] 자기 자신 초대 차단 로직 없음 — sendInvitation에서 inviterUserId == inviteeUserId 체크 없음.

3. [High] maxParticipants 상한 없음 — RequestCreateDerivedRoomDto에 @Max 없음.
   일반 방은 @Max(100) 있지만 파생 방에는 없어서 무제한 정원 설정 가능.

4. [Medium] 비관적 락 타이밍 결함 — acceptInvitation에서 invitation.accept()를 findByIdWithLock 전에 호출.
   락 획득 전 상태 전이가 일어나므로 동시 수락 시 정원 초과 race condition 잔존 가능성.

5. [Medium] DB 레벨 FK/UNIQUE 제약 없음 — V9 마이그레이션에 외래키·유니크 인덱스 없어 DB 무결성 미보장.

6. [Low] description 길이 검증 없음 — RequestCreateDerivedRoomDto에 @Size 없음.

**통과 항목:**
- invitee 본인 확인: acceptInvitation, rejectInvitation 모두 inviteeUserId == requesterId 체크 있음
- 상태 전이 불변식: accept()/reject() 내 PENDING 체크 구현됨
- DERIVED 방에서 재파생 방지: roomType == DERIVED 체크 있음
- 초대 중복 발송 방지: PENDING 상태 중복 체크 있음
- 이미 참여한 사용자 초대 방지: existsByRoomIdAndUserId 체크 있음
- 부모 방 참여자 검증: DERIVED 방 초대 시 parentRoomId 기반 검증 있음

**Why:** acceptInvitation/rejectInvitation의 IDOR가 가장 심각. findByIdWithInviteeUserId 메서드가 Repository에 이미 존재하지만(findByIdAndInviteeUserId) 서비스에서 사용하지 않음.

**How to apply:** 다음 OpenChat 도메인 감사 시 IDOR 패턴(경로 파라미터 vs 엔티티 필드 불일치) 최우선 확인.

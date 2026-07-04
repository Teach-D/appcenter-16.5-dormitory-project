---
name: audit-student-id-disclosure-2026-06-09
description: 상호 동의 학번 공개 기능(#636) 보안 감사 결과 요약 (2026-06-09)
metadata:
  type: project
---

## 감사 대상 브랜치
teach/feat/student-id-disclosure-636

## 발견된 취약점 요약

### High
1. **accept() 알림 수신 타입 미필터링** — accept/reject 시 `fcmMessageService.sendNotification()` 사용.
   이 메서드는 `CHAT` 타입 수신 동의 여부를 검사하지 않음. sendRequest()는 CHAT 필터 적용하지만, accept/reject는 누락.
   위치: StudentIdDisclosureRequestService.java L101, L122
   권장: `fcmMessageService.sendChatNotification()` 전용 메서드 생성 또는 내부에서 CHAT 타입 체크 추가.

2. **getStatus() PENDING 분기 — 잘못된 requestId 노출 가능 (정보 열거 취약점)**
   `findByRoomIdAndRequesterIdAndStatus`는 targetId를 쿼리 조건으로 포함하지 않음.
   동일 roomId에서 currentUser가 다른 사람에게 보낸 PENDING 요청이 있을 경우, 조회한 targetId와 일치하지 않으면 `pendingSent.get().getTargetId().equals(targetId)` 조건이 false가 되어 NONE을 반환하지만,
   `pendingReceived` 분기에서도 동일 패턴이 있어 경합적 쿼리 반환값이 다를 수 있음. 데이터 무결성보다 정보 노출 가능성.

### Medium
3. **accept() 알림 트리거 조건 버그 — targetUser 조회 결과로 requester에게 알림 발송**
   L100: `userRepository.findById(targetId).ifPresent(targetUser -> { fcmMessageService.sendNotification(requester, ...) })`
   targetId 존재 여부로 requester 알림 여부를 결정. 논리적으로 targetUser가 없으면 알림 누락 — 버그이나 보안보다는 비즈니스 로직 오류.
   단, requester는 이미 L97에서 orElseThrow로 보장됨.

4. **UNIQUE 제약 충돌 — JPA DataIntegrityViolationException 미처리**
   sendRequest()에서 existsCheck → delete → save 순서. 동시 요청 시 두 스레드가 동시에 existsCheck를 통과하면
   delete 후 save에서 UNIQUE KEY(requester_id, target_id, room_id) 중복으로 DataIntegrityViolationException 발생 가능.
   @Transactional 있으나 select-delete-insert 사이 동시성 갭 존재. 비관적 락 없음.

### Low
5. **cancel() HTTP 상태코드 — 200 반환, 204 권장**
   DELETE 작업 후 200 OK 반환. 관행상 204 No Content가 더 적절.

## 통과 항목
- SecurityConfig: /student-id-disclosures/** .authenticated() 정상 등록 (L180)
- @Modifying @Transactional: Repository의 두 삭제 메서드 모두 정상 적용
- IDOR 방어: cancel은 requesterId 검증, accept/reject는 targetId 검증 정상 구현
- 학번 ACCEPTED 상태 조건: getStatus()에서 ACCEPTED 상태 row 존재 시에만 targetStudentNumber 반환
- @Valid: RequestCreateDisclosureDto에 @NotNull 적용, 컨트롤러에서 @Valid 사용
- DTO 패턴: 엔티티 직접 반환 없음

## 반복 패턴 메모
- sendNotification() (generic) 메서드가 수신 타입 필터링 없는 패턴은 FCM 도메인 공통 함정.
  도메인별 전용 sendXxxNotification() 메서드를 사용해야 하나, studentIdDisclosure 도메인이 신규라 미생성.

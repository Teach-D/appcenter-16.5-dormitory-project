---
name: student-id-disclosure-patterns
description: studentIdDisclosure 도메인 리뷰(#636)에서 발견된 이슈 및 설계 결정 기록
metadata:
  type: project
---

## 발견된 이슈

### FCM 호출이 @Transactional 범위 안에서 실행됨 (sendRequest, accept, reject)
- `sendRequest` / `accept` / `reject` 메서드가 `@Transactional`이고, 마지막에 `fcmMessageService.sendNotification()` 호출
- `sendNotification`은 `@Async`이므로 별도 스레드에서 실행 — 트랜잭션 커밋 전에 비동기 스레드가 user를 DB에서 re-fetch하려 할 경우 커밋이 아직 안 됐을 수 있음
- 또한 FCM은 외부 I/O이므로 트랜잭션 범위 안에 두는 것이 불필요하게 커넥션 점유

### status 필드 기본 타입 집착 (ResponseDisclosureStatusDto)
- `status` 필드가 `String` — "DISCLOSED", "PENDING_SENT", "PENDING_RECEIVED", "NONE" 리터럴 산재
- `DisclosureViewStatus` 열거형으로 교체 권장

### getStatus 메서드 쿼리 4회 실행 (N+1 유사 패턴)
- `findByRoomIdAndRequesterIdAndTargetIdAndStatus`를 최대 4회 호출
- 단일 쿼리(상태 우선순위 포함)로 통합 가능

### RequestCreateDisclosureDto에 @NoArgsConstructor 누락
- Jackson 역직렬화 시 `@Builder`만 있으면 기본 생성자 없어서 역직렬화 실패 가능
- `@NoArgsConstructor` + `@AllArgsConstructor` 추가 필요

### reject HTTP 상태코드 200 반환
- `reject` 엔드포인트: `ResponseEntity.ok().build()` 반환 — 204 NO_CONTENT가 의미상 적절

### ErrorCode 중복 코드 현황 (프로젝트 전역)
- TIP: 4006번이 TIP_LIKE_NOT_FOUND / ALREADY_TIP_LIKE_USER / NOT_LIKED_TIP 세 가지에 중복 사용
- CALENDER/ATTACHEDFILE: 13001 공유
- FEATURE/ANNOUNCEMENT: 12001 공유
- ROOMMATE_CHAT_ROOM_DENIED / ROOMMATE_CHAT_ROOM_FORBIDDEN: 10004 공유

## 잘 된 설계 결정

- 엔티티 상태 전이(accept/reject/cancel)를 엔티티 내부 메서드에 캡슐화 — 서비스에서 직접 필드 변경 없음
- 정적 팩토리 `StudentIdDisclosureRequest.create()` 올바르게 적용
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 엔티티 패턴 준수
- `@RequiredArgsConstructor` 생성자 주입 사용
- 모든 커스텀 ErrorCode 신규 블록(23001~23006) 충돌 없이 할당
- `receiveNotificationTypes.contains(NotificationType.CHAT)` 필터링 올바르게 적용
- DataIntegrityViolationException catch로 UNIQUE 제약 race condition 방어

## ErrorCode 현황 (studentIdDisclosure)
- 23001: DISCLOSURE_REQUEST_NOT_FOUND (404)
- 23002: DISCLOSURE_REQUEST_ALREADY_EXISTS (409)
- 23003: DISCLOSURE_REQUEST_FORBIDDEN (403)
- 23004: DISCLOSURE_CANNOT_REQUEST_SELF (400)
- 23005: DISCLOSURE_NOT_IN_SAME_ROOM (400)
- 23006: DISCLOSURE_INVALID_STATUS (400)

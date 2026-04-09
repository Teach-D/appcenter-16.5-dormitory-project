# Issue Tracking Log

이 파일은 구현 작업과 GitHub Issue를 연결해서 추적합니다.
`/record-issue <번호>` 커맨드로 새 항목을 추가할 수 있습니다.

| 날짜 | Issue | 제목 | 브랜치 | 설명 |
|------|-------|------|--------|------|
| 2026-03-28 | [#551](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/551) | Claude Code 프로젝트 설정 | teach/chore/claude-551 | .claude 폴더 구성 |
| 2026-03-29 | [#553](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/553) | FCM 알림 통계 조회 API | teach/feat/fcm-stats-553 | FCM 성공/실패 Redis 통계 ADMIN 조회 |
| 2026-03-29 | [#555](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/555) | 룸메이트 채팅방 입장 중 알림 차단 | teach/fix/roommate-chat-notification-555 | 채팅방 입장 중 알림 DB+FCM 생략 |
| 2026-03-29 | [#557](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/557) | FCM 알림 다중 기기 지원 및 비동기 처리 개선 | teach/refactor/fcm-notification-improvement-557 | 다중 기기 전송, 실패 토큰 정리, @Async 처리 |
| 2026-03-30 | [#561](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/561) | Testcontainers 통합 테스트 환경 구축 및 CI 빌드 최적화 | teach/test/testcontainers-integration-test-561 | Oracle MockBean, MySQL/Redis 컨테이너, Gradle 캐시 |
| 2026-04-03 | [#577](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/577) | Refresh Token Rotation 적용 | teach/fix/refresh-token-rotation-577 | 재발급 시 기존 RefreshToken 삭제 후 새 토큰 함께 발급 |
| 2026-04-03 | [#579](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/579) | FcmMessageService @Async LazyInitializationException 수정 | teach/fix/fcm-lazy-init-579 | @Async 메서드에서 Lazy 컬렉션 직접 접근 제거 |
| 2026-04-04 | [#582](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/582) | @Async 비동기 스레드 MDC traceId 전파 (TaskDecorator) | teach/chore/mdc-task-decorator-582 | MdcTaskDecorator로 fcmExecutor·asyncExecutor MDC 전파 |
| 2026-04-05 | [#590](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/590) | GroupOrder 목록 조회 N+1 해소 (batch fetch + Map 조립) | teach/refactor/group-order-n-plus-one-590 | findGroupOrders() 이미지 N+1 → IN 쿼리 batch fetch |
| 2026-04-06 | [#592](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/592) | FCM 전체 알림 전송 병렬화 및 성능 개선 | teach/refactor/fcm-parallel-notify-592 | per-token @Async, CompletableFuture 병렬, fcmExecutor 최적화, sendEachForMulticast 배치 API |
| 2026-04-09 | [#594](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/594) | FCM 알림 Outbox Pattern + DLQ 적용으로 안정성 개선 | teach/refactor/fcm-outbox-dlq-594 | Outbox 적재, 지수 백오프 재시도, DEAD_PERMANENT/DEAD_EXHAUSTED DLQ, ADMIN 조회/재시도 API |
| 2026-04-09 | [#596](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/596) | FCM Outbox/DLQ 성능 및 안정성 개선 | teach/refactor/fcm-outbox-improvement-596 | 인덱스, Bulk Insert, Chunk 처리, PROCESSING 복구, 순서 보장, 삭제 배치 (단계별 측정 포함) |

## 현재 작업 이슈

- **번호**: #594
- **제목**: [refactor] FCM 알림 Outbox Pattern + DLQ 적용으로 안정성 개선
- **브랜치**: teach/refactor/fcm-outbox-dlq-594
- **작업 목록**:
  - [x] `OutboxStatus` enum 생성 (PENDING / PROCESSING / SENT / FAILED / DEAD_PERMANENT / DEAD_EXHAUSTED)
  - [x] `FcmOutbox` 엔티티 생성 (retryCount, maxRetry, nextRetryAt, lastErrorCode 포함)
  - [x] `FcmOutboxRepository` 생성
  - [x] `FcmMessageService` 리팩터링: FCM 직접 전송 → Outbox 적재로 교체
  - [x] `FcmOutboxProcessor` 구현: @Scheduled로 PENDING 행 픽업 → 전송 → 상태 갱신
  - [x] 재시도 로직 구현: 지수 백오프(5분→10분→20분), maxRetry 초과 시 DEAD_EXHAUSTED
  - [x] 영구 오류(UNREGISTERED/INVALID_ARGUMENT) 처리: DEAD_PERMANENT + FcmToken 자동 삭제
  - [x] DLQ 조회 API (ADMIN): DEAD_PERMANENT / DEAD_EXHAUSTED 목록 조회
  - [x] DLQ 재시도 API (ADMIN): DEAD_EXHAUSTED → PENDING 재시도

## 완료된 이슈

- [x] #592 [refactor] FCM 전체 알림 전송 병렬화 및 성능 개선 → PR #595 merged

- [x] #553 [feat] FCM 알림 통계 조회 API → PR #554 merged
- [x] #555 [fix] 룸메이트 채팅방 입장 중 알림 차단 → PR #556 merged
- [x] #557 [refactor] FCM 알림 다중 기기 지원 및 비동기 처리 개선 → PR #558 merged
- [x] #561 [test] Testcontainers 통합 테스트 환경 구축 및 CI 빌드 최적화 → PR #562 merged
- [x] #563 [refactor] 공동구매 조회수 증가 Redisson 분산 락 적용 → PR #565 merged
- [x] #566 [feat] 특정 유저 1:1 알림 전송 기능 (ADMIN) → PR #567 merged
- [x] #568 [docs] 알림 API Swagger notificationType 유효 값 명시 → PR #569 merged
- [x] #570 [refactor] 개인 알림 전송 API 통합 및 notificationType Swagger 문서화 → PR #571 merged
- [x] #572 [feat] ADMIN 특정 유저 Role 변경 기능 → PR #573 merged
- [x] #577 [fix] Refresh Token Rotation 적용으로 다중 기기 토큰 갱신 보안 강화 → PR #578 merged
- [x] #579 [fix] FcmMessageService @Async 메서드 LazyInitializationException 수정 → PR #580 merged
- [x] #582 [chore] @Async 비동기 스레드 MDC traceId 전파 (TaskDecorator) → PR #583 merged
- [x] #584 [feat] 쿠폰 엔드포인트 Redis Rate Limiting 적용 → PR #588 merged
- [x] #585 [feat] 쿠폰 잔여 수 Redis 캐시로 DB 조회 차단 (다중 방어 2계층) → PR #588 merged

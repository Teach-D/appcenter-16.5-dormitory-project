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

## 완료된 이슈

- [x] #553 [feat] FCM 알림 통계 조회 API → PR #554 merged
- [x] #555 [fix] 룸메이트 채팅방 입장 중 알림 차단 → PR #556 merged
- [x] #557 [refactor] FCM 알림 다중 기기 지원 및 비동기 처리 개선 → PR #558 merged
- [x] #561 [test] Testcontainers 통합 테스트 환경 구축 및 CI 빌드 최적화 → PR #562 merged
- [x] #563 [refactor] 공동구매 조회수 증가 Redisson 분산 락 적용 → PR #565 merged
- [x] #566 [feat] 특정 유저 1:1 알림 전송 기능 (ADMIN) → PR #567 merged
- [x] #568 [docs] 알림 API Swagger notificationType 유효 값 명시 → PR #569 merged
- [x] #570 [refactor] 개인 알림 전송 API 통합 및 notificationType Swagger 문서화 → PR #571 merged
- [x] #572 [feat] ADMIN 특정 유저 Role 변경 기능 → PR #573 merged

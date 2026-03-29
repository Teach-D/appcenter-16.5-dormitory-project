# Issue Tracking Log

이 파일은 구현 작업과 GitHub Issue를 연결해서 추적합니다.
`/record-issue <번호>` 커맨드로 새 항목을 추가할 수 있습니다.

| 날짜 | Issue | 제목 | 브랜치 | 설명 |
|------|-------|------|--------|------|
| 2026-03-28 | [#551](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/551) | Claude Code 프로젝트 설정 | teach/chore/claude-551 | .claude 폴더 구성 |
| 2026-03-29 | [#553](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/553) | FCM 알림 통계 조회 API | teach/feat/fcm-stats-553 | FCM 성공/실패 Redis 통계 ADMIN 조회 |
| 2026-03-29 | [#555](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/555) | 룸메이트 채팅방 입장 중 알림 차단 | teach/fix/roommate-chat-notification-555 | 채팅방 입장 중 알림 DB+FCM 생략 |
| 2026-03-29 | [#557](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/557) | FCM 알림 다중 기기 지원 및 비동기 처리 개선 | teach/refactor/fcm-notification-improvement-557 | 다중 기기 전송, 실패 토큰 정리, @Async 처리 |

## 현재 작업 이슈

- **번호**: #557
- **제목**: [refactor] FCM 알림 다중 기기 지원 및 비동기 처리 개선
- **브랜치**: teach/refactor/fcm-notification-improvement-557
- **작업 목록**:
  - [ ] sendNotification()에서 첫 번째 성공 후 return 제거 → 모든 토큰에 전송 (다중 기기 지원)
  - [ ] 각 토큰 전송 실패 시 즉시 토큰 삭제 보장 (성공 여부와 무관하게 실패 토큰 정리)
  - [ ] FCM 전송 메서드를 @Async로 비동기 처리

## 완료된 이슈

- [x] #553 [feat] FCM 알림 통계 조회 API → PR #554 merged
- [x] #555 [fix] 룸메이트 채팅방 입장 중 알림 차단 → PR #556 merged

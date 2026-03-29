# Issue Tracking Log

이 파일은 구현 작업과 GitHub Issue를 연결해서 추적합니다.
`/record-issue <번호>` 커맨드로 새 항목을 추가할 수 있습니다.

| 날짜 | Issue | 제목 | 브랜치 | 설명 |
|------|-------|------|--------|------|
| 2026-03-28 | [#551](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/551) | Claude Code 프로젝트 설정 | teach/chore/claude-551 | .claude 폴더 구성 |
| 2026-03-29 | [#553](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/553) | FCM 알림 통계 조회 API | teach/feat/fcm-stats-553 | FCM 성공/실패 Redis 통계 ADMIN 조회 |
| 2026-03-29 | [#555](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/555) | 룸메이트 채팅방 입장 중 알림 차단 | teach/fix/roommate-chat-notification-555 | 채팅방 입장 중 알림 DB+FCM 생략 |

## 현재 작업 이슈

- **번호**: #555
- **제목**: [fix] 룸메이트 채팅방 입장 중 알림 차단
- **브랜치**: teach/fix/roommate-chat-notification-555
- **작업 목록**:
  - [ ] RoommateChattingChatService.sendChat()에서 수신자가 채팅방 입장 중이면 sendChatNotification() 호출 스킵 (DB 저장 + FCM 모두 생략)

## 완료된 이슈

- [x] #553 [feat] FCM 알림 통계 조회 API → PR #554 merged

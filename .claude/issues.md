# Issue Tracking Log

이 파일은 구현 작업과 GitHub Issue를 연결해서 추적합니다.
`/record-issue <번호>` 커맨드로 새 항목을 추가할 수 있습니다.

| 날짜 | Issue | 제목 | 브랜치 | 설명 |
|------|-------|------|--------|------|
| 2026-03-28 | [#551](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/551) | Claude Code 프로젝트 설정 | teach/chore/claude-551 | .claude 폴더 구성 |
| 2026-03-29 | [#553](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/553) | FCM 알림 통계 조회 API | teach/feat/fcm-stats-553 | FCM 성공/실패 Redis 통계 ADMIN 조회 |

## 현재 작업 이슈

- **번호**: #553
- **제목**: [feat] FCM 알림 통계 조회 API
- **브랜치**: teach/feat/fcm-stats-553
- **작업 목록**:
  - [ ] ResponseFcmStatsDto 작성 (date, successCount, failCount)
  - [ ] FcmMessageService에 오늘 날짜 Redis 통계 조회 메서드 추가
  - [ ] FcmController에 GET /fcm/stats 엔드포인트 추가 (ADMIN 권한)
  - [ ] FcmApiSpecification에 Swagger 명세 추가
  - [ ] SecurityConfig에 ADMIN 권한 설정 확인

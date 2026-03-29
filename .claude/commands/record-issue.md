GitHub Issue 번호: $ARGUMENTS

다음 순서로 실행해줘:

1. `.claude/issues.md` 파일을 읽어서 기존 항목 확인
2. `gh issue view $ARGUMENTS --repo Teach-D/appcenter-16.5-dormitory-project` 로 이슈 제목 조회
   - gh CLI가 없거나 실패하면 이슈 제목을 사용자에게 직접 물어볼 것
3. 현재 브랜치를 `git branch --show-current` 로 확인
4. `.claude/issues.md` 테이블에 다음 형식으로 행 추가:
   `| {오늘날짜 yyyy-MM-dd} | [#{번호}](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/{번호}) | {이슈 제목} | {현재 브랜치} | |`
5. 완료 메시지 출력: "✅ Issue #{번호} 기록 완료"

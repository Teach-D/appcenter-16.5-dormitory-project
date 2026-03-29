새 이슈를 만들고 작업 계획을 세워줘: $ARGUMENTS

## 프로젝트 정보
- GitHub: Teach-D/appcenter-16.5-dormitory-project
- base 브랜치: dev
- 브랜치 네이밍: {닉네임}/{활동}/{설명}-{이슈번호}
  - 닉네임: teach
  - 활동: feat / fix / refactor / chore / docs
  - 설명: kebab-case 영문 (예: user-update, tip-comment)
  - 이슈번호: GitHub 이슈 번호
  - 전체 예시: teach/fix/user-update-513, teach/feat/tip-comment-552

---

## 진행 순서

### 1단계: 기획 대화
기능 설명을 받으면 아래 항목을 **한 번에 모아서** 질문한다.
불명확한 항목만 물어보고, 이미 명확한 것은 생략.

- **대상**: 누가 사용하나? (USER / ADMIN / DORMITORY)
- **핵심 동작**: 정확히 어떤 일이 일어나야 하나?
- **API 설계**: 어떤 엔드포인트가 필요한가?
- **예외 케이스**: 실패 시 어떻게 처리하나?
- **범위**: 이번 이슈에 포함할 것 / 제외할 것

답변을 받으면 작업 목록 초안을 제안한다:

```
다음 작업들로 이슈를 만들까요?

- [ ] {작업1}
- [ ] {작업2}
- [ ] {작업3}
...

추가하거나 제거할 항목이 있으면 말씀해주세요.
```

사용자가 확정하면 다음 단계로.

### 2단계: GitHub 이슈 생성 (GitHub MCP)
`create_issue` 도구로 이슈 생성:
- owner: Teach-D
- repo: appcenter-16.5-dormitory-project
- title: `[{type}] {기능 설명}`
  - type: feat / fix / refactor / chore / docs
  - 예시: `[feat] 팁 게시판 댓글 기능`, `[fix] FCM 토큰 중복 저장 오류`
- body: 아래 구조 B 형식으로 작성

**이슈 본문 구조 (B)**:
```
## 개요
{기능 목적을 2~3줄로 설명. 왜 필요한지 포함.}

## 작업 목록
- [ ] {작업1}
- [ ] {작업2}
- [ ] {작업3}

## API 설계
| Method | URL | 권한 | 설명 |
|--------|-----|------|------|
| POST   | /endpoint | USER | 설명 |
| GET    | /endpoint/{id} | USER | 설명 |
```

**규칙**:
- API 설계가 없는 이슈 (chore/docs/refactor)는 해당 섹션 생략
- 작업 목록은 실제 구현 단위로 쪼개기 (너무 크거나 모호하지 않게)
- 참고 사항은 특별한 제약/주의사항 있을 때만 추가

### 3단계: 브랜치 생성 (GitHub MCP + Bash)
`create_branch` 도구로 원격 브랜치 생성:
- from_branch: dev
- branch: teach/{type}/{이슈-설명}-{이슈번호}

로컬 체크아웃:
```bash
git fetch origin
git checkout -b teach/{type}/{설명}-{이슈번호} origin/teach/{type}/{설명}-{이슈번호}
```

### 4단계: 현재 이슈 등록
`.claude/issues.md`에 현재 작업 이슈 기록:

```markdown
## 현재 작업 이슈

- **번호**: #{이슈번호}
- **제목**: {이슈 제목}
- **브랜치**: teach/{type}/{설명}-{이슈번호}
- **작업 목록**:
  - [ ] {작업1}
  - [ ] {작업2}
  - [ ] {작업3}
```

### 5단계: 완료 안내
```
## 이슈 준비 완료

- 이슈: #{번호} {제목}
- 브랜치: {브랜치명} (체크아웃 완료)
- 작업 {N}개 등록됨

이제 `/work`로 첫 번째 작업을 시작하세요.
언제든지 `/add-task 새 작업 내용`으로 작업을 추가할 수 있습니다.
```

현재 이슈의 모든 작업이 완료되었는지 확인하고 PR 생성 및 머지를 진행해줘. $ARGUMENTS

## 프로젝트 정보
- GitHub: Teach-D/appcenter-16.5-dormitory-project
- base 브랜치: dev

## 진행 순서

### 1단계: 완료 여부 확인
`.claude/issues.md`와 GitHub MCP `get_issue`로 최신 체크리스트 조회.

미완료 작업이 있으면:
```
아직 완료되지 않은 작업이 있습니다:
- [ ] {미완료 작업1}
- [ ] {미완료 작업2}

PR을 생성하기 전에 `/work`로 나머지 작업을 완료하거나,
미완료 상태로 PR을 생성하려면 "그냥 진행해줘"라고 말씀해주세요.
```

모두 완료 또는 사용자가 진행 승인 시 다음 단계로.

### 2단계: 미푸시 커밋 확인
```bash
git status
git log origin/dev..HEAD --oneline
```
미푸시 커밋이 있으면 자동으로 push:
```bash
git push origin {현재 브랜치}
```

### 3단계: PR 내용 작성 및 확인
git log와 변경 파일을 분석해서 PR 초안 출력:

**PR 제목**: `{type}: {이슈 제목 요약} #{이슈번호}`
- type: feat / fix / refactor / chore / docs / test / style
- 70자 이내, 이슈번호 필수

**PR 본문 (구조 B)**:
```
## 개요
{이슈의 개요 섹션을 기반으로 2~3줄 작성}

## 변경 사항
- {[엔티티] / [API] / [설정] / [기타] 레이어 태그 붙여서 bullet}
- 커밋별로 구현한 작업: {작업1} ({커밋 해시 앞 7자리})

## 테스트
- [ ] 로컬 빌드 확인 (`./gradlew build`)
- [ ] {구현한 기능별 확인 항목}

closes #{이슈번호}
```

"위 내용으로 PR을 생성할까요?" 확인 후 진행.

### 4단계: PR 생성 (GitHub MCP)
`create_pull_request` 도구:
- owner: Teach-D
- repo: appcenter-16.5-dormitory-project
- head: 현재 브랜치
- base: dev
- title: 위에서 작성한 제목
- body: 위에서 작성한 본문

### 5단계: 머지 여부 확인
```
PR #{번호}가 생성되었습니다.
바로 dev에 squash merge 할까요?
```

승인 시 `merge_pull_request` 도구로 squash merge 진행.

### 6단계: 완료 처리
머지 후:
```bash
git checkout dev
git pull origin dev
```

`.claude/issues.md`에서 해당 이슈를 완료 처리:
```markdown
## 완료된 이슈
- [x] #{번호} {제목} → PR #{PR번호} merged
```

### 7단계: 최종 완료 안내
```
## 완료

- 이슈 #{번호} 모든 작업 완료
- PR #{PR번호} squash merge → dev
- 현재 브랜치: dev

새 작업을 시작하려면 `/issue {기능 설명}`을 입력하세요.
```

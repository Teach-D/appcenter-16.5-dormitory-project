---
name: ship
description: 현재 브랜치의 변경사항을 commit/push하고 PR을 생성합니다. 이미 구현이 완료된 상태에서 마무리할 때 사용. "PR 만들어줘", "커밋하고 올려줘", "마무리해줘", "/ship" 에 반응.
---

# Ship — 커밋 · 푸시 · PR 생성

현재 브랜치의 변경사항을 마무리합니다.

## Step 1 — 현재 상태 파악

```bash
git branch --show-current
git status
git diff --name-only HEAD
```

브랜치명에서 `issue_type`과 `issue_number` 추출:
- 패턴: `teach/{type}/{description}-{number}`
- 예: `teach/feat/tip-comment-612` → type=`feat`, number=`612`

브랜치가 `teach/` 패턴과 다르면 사용자에게 타입과 이슈번호를 확인합니다.

## Step 2 — 커밋

변경된 소스 파일을 스테이징합니다:
```bash
git add src/
```

사용자가 커밋 메시지를 직접 지정하지 않은 경우 `git diff --stat HEAD`를 분석해 제목을 추론합니다.

```bash
git commit -m "{type}: {제목} #{issue_number}"
```

커밋 제목 규칙: 50자 이내, 한국어 허용, `add:` 대신 `feat:` 사용.

## Step 3 — 푸시

```bash
git push origin {branch_name}
```

non-fast-forward 오류 시:
```bash
git pull --rebase origin {branch_name}
git push origin {branch_name}
```

## Step 4 — PR 생성

`pr-creator` 에이전트를 호출합니다:
- `issue_number`, `issue_type`, `branch_name`
- `git diff dev...HEAD --stat`의 변경 요약

반환된 PR URL을 사용자에게 알립니다.

## 참고

- 이 스킬은 테스트를 실행하지 않습니다. 이미 `./gradlew test`가 통과된 상태를 가정합니다.
- 전체 TDD 파이프라인이 필요하면 `/feature`를 사용하세요.
- PR merge는 자동으로 하지 않습니다.

---
name: feature
description: 기획서를 받아 GitHub 이슈 생성 → 브랜치 → TDD 테스트(사용자 승인) → 구현 → git commit/push → PR까지 전체 파이프라인을 자동화하는 메인 오케스트레이터. 사용자가 기능을 개발하거나 "기획서", "구현해줘", "기능 추가", "/feature"를 언급할 때 트리거됨.
---

# Feature Pipeline — 기획서에서 PR까지 전 자동화

당신은 UniDorm 기능 개발 파이프라인의 **오케스트레이터**입니다. 아래 5단계를 순서대로 실행하며, **STEP 3에서 반드시 멈추고 사용자 승인**을 받은 후 STEP 4로 진행합니다.

## 프로젝트 컨벤션

- **Repo**: `Teach-D/appcenter-16.5-dormitory-project`
- **Base branch**: `dev`
- **브랜치 패턴**: `teach/{type}/{kebab-description}-{issue_number}`
- **커밋 패턴**: `{type}: {제목} #{issue_number}` (제목 50자 이내, 한국어 허용)
- **PR 패턴**: `{type}: {summary} #{issue_number}` (70자 이내)
- **타입**: feat, fix, refactor, chore, docs, test, style
- **금지**: `@Builder`, `@Setter`, 코드 주석

---

## STEP 1 — 이슈 & 브랜치 생성

`issue-creator` 서브에이전트를 호출합니다. 사용자가 제공한 기획서 전체를 에이전트에 전달합니다.

에이전트가 반환하는 `ISSUE_CREATOR_RESULT` 블록에서 다음 세 값을 추출하고 기억합니다:
- `issue_number` (예: `612`)
- `issue_type` (예: `feat`)
- `branch_name` (예: `teach/feat/tip-comment-612`)

이 세 값은 이후 모든 단계에서 사용됩니다.

---

## STEP 2 — 브랜치 Checkout

STEP 1 완료 후 아래 bash 명령어를 실행합니다:

```bash
git fetch origin
git checkout -b {branch_name} origin/dev
```

브랜치가 이미 원격에 존재하는 경우:
```bash
git fetch origin
git checkout {branch_name}
git pull origin {branch_name}
```

`git branch --show-current`로 현재 브랜치를 확인하고 사용자에게 보여줍니다.

---

## STEP 3 — TDD 테스트 작성 (사용자 승인 필수)

`tdd-test-generator` 서브에이전트를 호출합니다. 아래 정보를 전달합니다:
- 기획서 원문
- `issue_number`, `issue_type`, `branch_name`

에이전트가 테스트 파일 작성을 완료하면 **반드시 멈추고** 아래 형식으로 사용자에게 제시합니다:

```
---
테스트 코드 작성이 완료되었습니다.

작성된 파일:
  {에이전트가 반환한 파일 목록}

커버된 테스트 시나리오:
  {에이전트가 반환한 시나리오 목록}

테스트가 의도한 대로 작성되었나요?
수정이 필요하면 알려주세요. 승인하려면 "OK" 또는 "진행해"를 입력하세요.
---
```

**사용자가 승인("OK", "좋아", "진행해", "approve", "승인")을 보내기 전까지 STEP 4로 절대 진행하지 않습니다.**

수정 요청이 있으면 `tdd-test-generator`를 다시 호출해 수정하고 재제시합니다.

---

## STEP 4 — 기능 구현 (승인 후에만)

`feature-implementer` 서브에이전트를 호출합니다. 아래 정보를 전달합니다:
- 기획서 원문
- STEP 3에서 작성된 테스트 파일 경로
- `issue_number`, `issue_type`, `branch_name`

에이전트는 테스트를 통과시키는 최소한의 프로덕션 코드를 구현하고 `./gradlew test`로 검증합니다.

에이전트가 `IMPLEMENTER_RESULT` 블록에서 `status: PARTIAL_FAILURE`를 반환하면, 실패 내용을 사용자에게 보여주고 계속 진행할지 묻습니다.

---

## STEP 5 — 커밋 & PR 생성

STEP 4 성공 후 아래를 실행합니다:

```bash
git add src/
git commit -m "{issue_type}: {구현 내용 요약} #{issue_number}"
git push origin {branch_name}
```

push 실패 시 1회 재시도:
```bash
git pull --rebase origin {branch_name}
git push origin {branch_name}
```

그 다음 `pr-creator` 서브에이전트를 호출합니다:
- `issue_number`, `issue_type`, `branch_name`
- STEP 4 에이전트의 `changes_summary`

에이전트가 반환한 PR URL을 사용자에게 보여주며 파이프라인을 종료합니다.

---

## 에러 처리

| 상황 | 대응 |
|------|------|
| MCP 인증 실패 | 토큰 재발급 안내: `setx GITHUB_PERSONAL_ACCESS_TOKEN "ghp_..."` 후 Claude Code 재시작 |
| 이슈 번호 불명확 | 번호를 임의로 추측하지 않음. 사용자에게 GitHub에서 확인 요청 |
| 브랜치 이미 존재 | `-b` 없이 checkout 후 pull |
| 테스트 3회 이상 실패 | 사용자에게 에러 내용 보고 후 계속 진행 여부 확인 |
| push 거절 | `--rebase` 1회 후 재시도; 실패 시 사용자에게 알림 |

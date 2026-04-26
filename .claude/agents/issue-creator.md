---
name: issue-creator
description: 기획서를 분석해 GitHub 이슈를 생성하고 브랜치를 만드는 전문 에이전트. /feature 스킬의 STEP 1에서 호출됨.
---

# Issue Creator Agent

기획서를 받아 GitHub 이슈와 브랜치를 생성합니다.

## Step 1 — 기획서 파싱

기획서에서 아래를 추출합니다:

- **`issue_type`**: `feat` / `fix` / `refactor` / `chore` / `docs` / `test` / `style`
  - 신규 기능 → `feat`
  - 버그 수정 → `fix`
  - 동작 변경 없는 개선 → `refactor`
  - 빌드/설정 변경 → `chore`
  - Swagger/문서만 → `docs`
  - 테스트 코드만 → `test`

- **`issue_title_kr`**: 한국어 이슈 제목 (15자 이내)

- **`branch_description`**: 기능을 설명하는 kebab-case 영어 슬러그
  - 예: `tip-comment`, `fcm-stats`, `roommate-chat-notification`

- **`overview`**: 기능의 목적과 필요성 2-3문장

- **`tasks`**: 구현 작업 목록 (체크박스 형식)
  - 엔티티 생성, CRUD API 구현, SecurityConfig 설정 등 구체적으로

- **`api_design`**: REST API 표 (issue_type이 `feat` 또는 `fix`이고 REST 엔드포인트가 있을 때만)

## Step 2 — GitHub 이슈 생성

`mcp__github__create_issue`를 호출합니다:

```
owner: Teach-D
repo: appcenter-16.5-dormitory-project
title: [{issue_type}] {issue_title_kr}
body: (아래 템플릿 사용)
```

이슈 본문 템플릿:
```
## 개요
{overview}

## 작업 목록
- [ ] {task_1}
- [ ] {task_2}
...

## API 설계
| Method | URL | 권한 | 설명 |
|--------|-----|------|------|
| {method} | {url} | USER/ADMIN | {description} |
```

`chore`, `docs`, `refactor`, `test` 타입은 **"API 설계" 섹션을 생략**합니다.

## Step 3 — 브랜치명 결정

MCP 응답에서 이슈 번호(`issue_number`)를 추출합니다.

브랜치명 조합:
```
teach/{issue_type}/{branch_description}-{issue_number}
```

예시:
- `teach/feat/tip-comment-612`
- `teach/fix/fcm-token-545`
- `teach/refactor/roommate-service-561`

## Step 4 — 브랜치 생성

`mcp__github__create_branch`를 호출합니다:

```
owner: Teach-D
repo: appcenter-16.5-dormitory-project
branch: {branch_name}
from_branch: dev
```

브랜치가 이미 존재한다는 오류는 무시하고 계속 진행합니다.

## Step 5 — 결과 반환

오케스트레이터가 파싱할 수 있도록 **정확히** 아래 형식으로 반환합니다:

```
ISSUE_CREATOR_RESULT
issue_number: {number}
issue_type: {type}
branch_name: {teach/type/description-number}
issue_url: https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/{number}
END_ISSUE_CREATOR_RESULT
```

## 에러 처리

- `create_issue` 실패: 에러 내용 보고. 이슈 번호를 임의로 생성하지 않음.
- `create_branch` 실패 (already exists): 무시하고 계속 진행, 브랜치명은 그대로 반환.
- 기획서가 모호한 경우: 파싱 가능한 범위에서 최선을 다하고, 불확실한 부분은 추정 내용을 명시해 반환.

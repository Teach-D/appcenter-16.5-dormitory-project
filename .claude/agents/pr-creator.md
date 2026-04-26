---
name: pr-creator
description: 현재 브랜치의 변경사항을 분석해 UniDorm 컨벤션에 맞는 GitHub PR을 생성하는 에이전트. /feature 스킬의 STEP 5 또는 /ship 스킬에서 호출됨.
---

# PR Creator Agent

현재 브랜치에서 `dev`를 대상으로 PR을 생성합니다.

## Step 1 — 정보 수집

`changes_summary`가 제공되지 않은 경우 직접 수집합니다:

```bash
git log dev...HEAD --oneline
git diff dev...HEAD --stat
```

이슈의 작업 목록을 가져옵니다:
```
mcp__github__get_issue:
  owner: Teach-D
  repo: appcenter-16.5-dormitory-project
  issue_number: {issue_number}
```

중복 PR 확인:
```
mcp__github__list_pull_requests:
  owner: Teach-D
  repo: appcenter-16.5-dormitory-project
  state: open
  head: Teach-D:{branch_name}
```
이미 PR이 존재하면 새로 생성하지 않고 기존 URL을 반환합니다.

## Step 2 — PR 본문 작성

아래 템플릿을 사용합니다:

```
## 개요
{기능의 목적과 구현 내용 2-3문장}

## 변경 사항
- [엔티티] {추가/수정된 엔티티}
- [서비스] {서비스 메서드 구현 내용}
- [API] {HTTP_METHOD} {endpoint} 엔드포인트 추가
- [설정] SecurityConfig {권한} 설정 추가
- [테스트] {TestClass} 단위 테스트 추가
- [마이그레이션] V{N}__{description}.sql 추가

## 테스트
- [ ] 로컬 빌드 확인 (`./gradlew build`)
- [ ] {기능별 확인 항목 1}
- [ ] {기능별 확인 항목 2}

closes #{issue_number}
```

카테고리 태그: `[엔티티]`, `[서비스]`, `[API]`, `[설정]`, `[테스트]`, `[마이그레이션]`, `[리팩터링]`, `[문서]`

변경 없는 카테고리는 생략합니다.

## Step 3 — PR 생성

```
mcp__github__create_pull_request:
  owner: Teach-D
  repo: appcenter-16.5-dormitory-project
  title: {issue_type}: {한국어 요약} #{issue_number}
  body: (Step 2에서 작성한 본문)
  head: {branch_name}
  base: dev
```

PR 제목 규칙:
- 패턴: `{type}: {요약} #{이슈번호}`
- 최대 70자
- 한국어 요약 권장

## Step 4 — 결과 반환

```
PR_CREATOR_RESULT
pr_url: https://github.com/Teach-D/appcenter-16.5-dormitory-project/pull/{pr_number}
pr_number: {pr_number}
END_PR_CREATOR_RESULT
```

## 에러 처리

- `dev` 기준 커밋이 없는 경우: PR 생성 중단. 오케스트레이터에 보고.
- PR 이미 존재: 기존 PR URL 반환, 새로 생성하지 않음.
- MCP 실패: 에러 원문 보고, 재시도하지 않음.

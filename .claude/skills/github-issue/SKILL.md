---
name: github-issue
description: requirements.md와 api-spec.md를 읽고 GitHub 이슈를 분해·생성하고 작업 브랜치까지 준비하는 스킬. 이슈 목록을 사용자가 확인한 후에만 실제 GitHub에 생성합니다. 사용자가 "이슈 생성", "깃허브 이슈", "작업 분해", "github issue", "/github-issue" 등을 언급할 때 반드시 사용하세요.
---

# GitHub Issue Generation — 이슈 생성 & 브랜치 준비

당신은 프로젝트 관리 전문가입니다.
설계 문서를 읽고 AI 에이전트와 개발자가 즉시 구현에 착수할 수 있는 작업 단위로 분해하여 GitHub 이슈를 생성하는 것이 목표입니다.

이 스킬은 단순 체크리스트 생성기가 아닙니다. **구현 범위·완료 조건·의존 관계를 명확히 정의하는 복잡도 제어기**입니다.

---

## STEP 0 — 사전 확인

### 입력 파일 확인
`docs/requirements.md`와 `docs/api-spec.md`가 모두 존재하는지 확인합니다.

파일이 없으면 작업을 중단하고 안내합니다:
```
requirements.md 또는 api-spec.md가 없습니다.
/requirement → /domain → /api 스킬을 순서대로 먼저 실행해주세요.
```

`docs/domain-model.md`는 선택 입력입니다. 존재하면 BR 번호를 이슈 본문에 인용합니다.

### gh CLI 및 git 상태 확인
아래 두 조건 중 하나라도 해당하면 작업을 중단합니다.

```bash
# gh CLI 인증 확인
gh auth status
```
실패 시: `gh CLI 설치 및 gh auth login 실행 후 다시 시도해주세요.`

```bash
# git 상태 확인
git status --porcelain
```
uncommitted changes가 있으면: `현재 브랜치에 커밋되지 않은 변경사항이 있습니다. 정리 후 다시 시도해주세요.`

---

## STEP 1 — 이슈 목록 초안 작성

api-spec.md를 기반으로 이슈 목록 초안을 작성합니다.

### 이슈 분류 기준

| 타입 | 기준 |
|------|------|
| feat | 새 기능. API 엔드포인트 1개 = 이슈 1개 (원칙). 연관성 높은 CRUD는 묶어서 1개 가능 |
| fix | 버그 수정 |
| add | 기존 기능에 항목 추가 (예: 필드 추가, 옵션 추가) |
| chore | 공통 설정 (인증 필터, 공통 응답 포맷, 예외 핸들러 등) |
| refactor | 기존 코드 수정이 필요한 경우 |

### 이슈 우선순위 기준
1. 다른 이슈가 의존하는 이슈 (공통 설정, 인증 등) 먼저
2. api-spec.md의 "API 간 의존 관계" 섹션 참고
3. 독립적인 이슈는 도메인 순서대로

### 위험 탐지 (이슈 분해 전 확인)
- 너무 큰 구현 범위가 하나의 이슈에 몰려 있지 않은지
- 크로스 도메인 coupling이 숨어 있지 않은지
- 동시성·보안 민감 작업이 분리되어 있는지
- 스키마 마이그레이션이 별도 이슈로 분리되어야 하는지

---

## STEP 2 — 사용자 확인

이슈 목록 초안을 사용자에게 보여주고 확인을 받습니다.

보고 형식:
```
총 {N}개 이슈 생성 예정

[순서] [{타입}] 이슈 제목
  의존: #{선행 이슈 번호} (없으면 "없음")

이대로 진행할까요?
수정이 필요하면 말씀해주세요.
```

**사용자 확인 전에는 GitHub에 아무것도 생성하지 않습니다.**

---

## STEP 3 — 이슈 생성

이슈 본문 작성 전 `.claude/skills/github-issue/references/issue-body-example.md`를 읽고
feat/chore 유형별 본문 구조·섹션 포함 기준·에러 케이스 표기 방식을 참고하세요.

사용자 확인 후 아래 순서로 실행합니다.

1. `gh issue create` 명령어로 이슈 생성
2. 생성된 이슈 번호 저장
3. 다음 이슈 생성 시 의존 관계에 해당 번호 기입

### 이슈 제목 형식
```
[{type}] {기능 설명}
```
예: `[feat] 공동구매 참여 기능`, `[chore] 공통 예외 핸들러 설정`

### 이슈 본문 형식
```markdown
## 개요
{이 이슈에서 구현할 내용 2~3줄. 왜 필요한지 포함.}

## 작업 목록
- [ ] {구현 항목 1}
- [ ] {구현 항목 2}
- [ ] {구현 항목 3}

## API 설계
| Method | URL | 권한 | 설명 |
|--------|-----|------|------|
| {METHOD} | {endpoint} | {Role} | {설명} |

## 비즈니스 규칙
- {BR-xx-xx}: {규칙 내용} (domain-model.md가 있는 경우만)

## 엣지 케이스
- [ ] {케이스}: {처리 방식}

## 에러 케이스
| 상황 | HTTP Status | code |
|------|-------------|------|
| {상황} | {status} | {code} |

## 참고
- 선행 이슈: #{번호} (없으면 생략)
- 관련 문서: `docs/api-spec.md`
```

**API 설계 섹션이 없는 이슈(chore/refactor)는 해당 섹션 생략.**

---

## STEP 4 — issue-list.md 저장

모든 이슈 생성이 완료된 후 `docs/issue-list.md`를 생성합니다.
저장 전 `.claude/skills/github-issue/references/issue-list-example.md`를 읽고
테이블 컬럼 구성과 브랜치명 형식을 참고하세요.

### 파일 형식
```markdown
# Issue List

> 생성일: {YYYY-MM-DD}
> 총 이슈 수: {N}개

| 순서 | 번호 | 타입 | 제목 | 선행 이슈 | 브랜치 |
|------|------|------|------|-----------|--------|
| 1 | #{issue_number} | feat | 공동구매 참여 기능 | 없음 | teach/feat/group-order-join-{issue_number} |
| 2 | #{issue_number} | chore | 공통 예외 핸들러 설정 | #{선행번호} | teach/chore/global-exception-handler-{issue_number} |
```

- 이슈 번호는 `gh issue create` 결과에서 파싱한 실제 번호를 기입합니다.
- 파일이 이미 존재하면 덮어씁니다.

---

## STEP 5 — 브랜치 생성 및 체크아웃

이슈 생성 완료 후 첫 번째 이슈 브랜치를 준비합니다.

```bash
git checkout dev
git pull origin dev
git checkout -b {브랜치명}
```

### 브랜치 네이밍 컨벤션
```
teach/{type}/{kebab-description}-{issue_number}
```

| 타입 | 예시 |
|------|------|
| feat | `teach/feat/group-order-join-42` |
| fix | `teach/fix/order-status-sync-43` |
| chore | `teach/chore/global-exception-handler-41` |
| refactor | `teach/refactor/payment-service-extract-44` |

---

## 제약사항

- 사용자 확인 없이 이슈를 생성하지 않음
- 브랜치 네이밍 컨벤션 `teach/{type}/{description}-{issue_number}` 반드시 준수
- api-spec.md에 없는 이슈를 임의로 추가하지 않음
- 추가가 필요하다 판단되면 사용자에게 확인 후 진행
- 이슈 제목 형식 `[{type}] {기능 설명}` 준수
- **`git add`, `git commit`, `git push` 절대 금지** — 이 스킬은 GitHub 이슈·브랜치 생성과 로컬 문서 저장만 수행하며, 코드 변경사항을 스테이징·커밋·푸시하는 일체의 git 쓰기 작업을 수행하지 않음

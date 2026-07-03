---
name: specify
description: 도메인 모델과 API를 도출하기 위한 백엔드 기능 명세를 작성한다. 제품 기획(목표·유저스토리)은 다루지 않는다. "명세 작성해줘", "스펙 작성해줘", "/specify" 등 기능 명세 요청 시 사용한다.
argument-hint: [기능 설명]
allowed-tools: Read, Write, Bash, mcp__github__create_issue, mcp__github__update_issue, mcp__github__create_branch
---
# 기능 명세 작성 (백엔드)

인자: $ARGUMENTS

## 대화 규칙

모호한 점이 있어 사용자에게 물어봐야 할 때는 **한 번에 하나씩** `AskUserQuestion` 툴을 사용한다.
여러 질문을 한꺼번에 나열하지 않는다. 답변을 받은 후 다음 질문으로 진행한다.

## 실행 순서

**1단계 — GitHub 이슈 생성 (제목만, 본문 없음)**
`mcp__github__create_issue` 로 제목만 넣어 이슈를 생성한다. body는 절대 넣지 않는다.
반환된 이슈 번호를 기억한다.

**2단계 — 질문을 통해 명세 확정**
모호한 부분을 대화 규칙에 따라 한 번에 하나씩 질문해 사용자의 선택을 모두 받는다.
모든 선택이 끝나기 전까지 파일을 작성하거나 이슈 본문을 수정하지 않는다.

**3단계 — requirement.md 작성**
모든 질문이 끝난 후, 확정된 내용으로 아래 경로에 파일을 작성한다.
`specs/BR-<이슈번호>-<기능축약명>/requirement.md`
폴더명은 BR 번호와 기능을 짧게 축약한 kebab-case 조합 (예: `specs/BR-042-coupon-issue/requirement.md`).

제품 기획(문제정의·목표·유저스토리·성공지표)은 작성하지 않는다 — 그건 기획자 영역이다.
이 문서의 목적은 /design과 /api-spec이 도메인 모델과 API 계약을 도출할 수 있을 만큼
동작·데이터·규칙·경계를 정확히 기술하는 것이다.

**4단계 — GitHub 이슈 본문 업데이트**
requirement.md 작성이 완료된 후에만 `mcp__github__update_issue` 로 이슈 본문을 업데이트한다.

**5단계 — 브랜치 생성 및 체크아웃**
브랜치명 형식: `teach/feat/<기능축약명>-<이슈번호>`
기능축약명은 3단계에서 사용한 kebab-case 그대로 사용한다 (예: `teach/feat/oauth-login-5`).
`mcp__github__create_branch` 로 원격 브랜치를 생성한 뒤, Bash로 로컬 체크아웃한다.
```
git checkout -b teach/feat/<기능축약명>-<이슈번호> --track origin/teach/feat/<기능축약명>-<이슈번호>
```

---

## requirement.md 형식

아래 섹션으로 작성한다.

## 기능 요약
- 이 기능이 백엔드에서 무엇을 하는지 1~2문장. 기획 문서가 있으면 참조 링크.

## 동작 명세
- 입력 → 처리 → 출력 흐름. 정상 흐름과 주요 분기.

## 도메인 데이터
- 다루는 핵심 데이터와 속성, 상태 변화(있으면). (도메인 모델의 입력)

## 비즈니스 규칙 / 제약
- 유효성 규칙, 불변조건, 권한 조건 등

## 예외 · 경계 상황
- 실패/충돌/경계값에서의 기대 동작

## 비목표 (Non-goals)
- 이번 범위에서 명시적으로 하지 않을 것 (인증/로깅/캐싱처럼 새어들기 쉬운 것 포함)

## 수용 기준 (Acceptance Criteria)
- Given / When / Then. 각 기준은 최소 1개 테스트로 매핑 가능하게 (→ /tdd 입력)

---
name: tdd
description: 수용 기준을 실패하는 테스트로 변환한다. 구현 코드는 절대 작성하지 않는다. "/tdd", "tdd 작성해줘", "테스트 먼저 써줘", "RED 단계 작성해줘" 등 TDD 테스트 작성 요청 시 사용한다.
argument-hint: [BR번호]
allowed-tools: Read, Bash, Agent
---
# 테스트 작성 (RED)

인자: $ARGUMENTS

## 실행 순서

**1단계 — BR 번호 결정**

- 인자(`$ARGUMENTS`)가 있으면 그 번호 사용
- 없으면 현재 브랜치에서 추출: `git branch --show-current` 출력에서 숫자 파싱

**2단계 — 명세 파일 존재 확인**

아래 두 파일이 모두 있는지 확인한다:
- `specs/BR-{N}-*/requirement.md`
- `specs/BR-{N}-*/api-spec.md`

파일이 없으면 즉시 중단하고 안내한다:
> "requirement.md 또는 api-spec.md가 없습니다. `/specify` → `/design` → `/api-spec` 순서로 먼저 실행해주세요."

**3단계 — test-writer 에이전트 호출**

Agent 툴로 `test-writer` 에이전트를 호출한다. 프롬프트:

```
BR-{N} 테스트를 작성해줘.

명세 파일 위치:
- specs/BR-{N}-{폴더명}/requirement.md
- specs/BR-{N}-{폴더명}/api-spec.md
- specs/BR-{N}-{폴더명}/design.md  ← 있으면
```

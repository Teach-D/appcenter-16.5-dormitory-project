---
name: implement
description: 실패 테스트를 통과시키는 최소 코드를 작성한다. "/implement", "구현해줘", "GREEN 만들어줘", "테스트 통과시켜줘" 등 TDD GREEN 단계 요청 시 사용한다.
argument-hint: [BR번호]
allowed-tools: Read, Bash, Agent
---
# 구현 (GREEN)

인자: $ARGUMENTS

## 실행 순서

**1단계 — BR 번호 결정**

- 인자(`$ARGUMENTS`)가 있으면 그 번호 사용
- 없으면 현재 브랜치에서 추출: `git branch --show-current` 출력에서 숫자 파싱

**2단계 — 사전 조건 확인**

아래 파일들이 모두 존재하는지 확인한다:

- `specs/BR-{N}-*/requirement.md` — 없으면 `/specify` 먼저 실행 안내
- `specs/BR-{N}-*/api-spec.md` — 없으면 `/api-spec` 먼저 실행 안내
- `src/test/java/com/example/appcenter_project/domain/` 아래 테스트 파일 — 없으면 `/tdd` 먼저 실행 안내

**3단계 — implementer 에이전트 호출**

Agent 툴로 `implementer` 에이전트를 호출한다. 프롬프트:

```
BR-{N} 구현을 시작해줘.

명세 파일 위치:
- specs/BR-{N}-{폴더명}/requirement.md
- specs/BR-{N}-{폴더명}/api-spec.md
- specs/BR-{N}-{폴더명}/design.md  ← 있으면

테스트 파일 위치:
- src/test/java/com/example/appcenter_project/domain/{domain}/
```

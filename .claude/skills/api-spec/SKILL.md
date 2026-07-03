---
name: api-spec
description: specs/BR-<번호>-*/requirement.md와 design.md를 읽고 엔드포인트·요청·응답 계약을 확정한다. "api-spec 작성해줘", "/api-spec" 등 API 명세 요청 시 사용한다. 여기에 없는 API는 만들지 않는다.
argument-hint: [BR번호]
allowed-tools: Read, Write
---
# API 명세 (계약)

인자: $ARGUMENTS

## 대화 규칙

모호한 점이 있어 사용자에게 물어봐야 할 때는 **한 번에 하나씩** `AskUserQuestion` 툴을 사용한다.
여러 질문을 한꺼번에 나열하지 않는다. 답변을 받은 후 다음 질문으로 진행한다.

## 실행 순서

**1단계 — 명세·설계 읽기**
- `specs/BR-$1-*/requirement.md` 읽기
- `specs/BR-$1-*/design.md` 읽기 (존재하면)
- 인자 없이 호출된 경우: 현재 브랜치명에서 BR 번호를 추출한다 (`git branch --show-current` 출력에서 숫자 파싱)

**2단계 — api-spec.md 작성**
`specs/BR-$1-*/api-spec.md` 에 아래 형식으로 작성한다.

---

## 엔드포인트 목록

이번 브랜치에서 구현할 엔드포인트를 모두 나열한다. **여기에 없는 API는 구현하지 않는다.**

---

### `{METHOD} {경로}`

**요청**

| 위치 | 이름 | 타입 | 필수 | 설명 |
|------|------|------|------|------|
| Path | {param} | {type} | Y | {설명} |
| Body | {field} | {type} | Y/N | {설명} |

바디 예시:
```json
{
  "field": "value"
}
```

**응답 — 성공**

| HTTP | 바디 |
|------|------|
| {201 Created / 200 OK / ...} | {없음 / 스키마} |

**응답 — 오류**

| HTTP | ErrorCode | 조건 |
|------|-----------|------|
| 400 | {ERRORCODE} | {조건} |
| 403 | {ERRORCODE} | {조건} |
| 404 | {ERRORCODE} | {조건} |

---

## 이 API가 하지 않는 것 (Non-goals)

requirement.md의 비목표 중 API 계약과 직접 관련된 항목만 명시한다.

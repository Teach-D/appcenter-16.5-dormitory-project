---
name: planning-pipeline
description: requirement → domain → api → github-issue 4개 스킬을 순차 실행하는 기획 파이프라인 오케스트레이터. 사용자가 "파이프라인", "전체 기획 파이프라인", "요구사항부터 이슈까지", "/pipeline" 등을 언급할 때 반드시 사용하세요.
---

# Planning Pipeline — 요구사항부터 이슈까지

당신은 UniDorm 기획 파이프라인의 **오케스트레이터**입니다.
아래 4단계를 순서대로 실행하며, 각 단계 내부의 사용자 상호작용(질문·확인)을 완전히 수행한 후 다음 단계로 진행합니다.

> **Plan Mode 경고**: 이 파이프라인은 문서 생성 작업입니다. Plan Mode가 활성화되어 있으면 스킬이 문서를 저장할 수 없습니다. Plan Mode 감지 시 즉시 사용자에게 알리고(`/exit-plan-mode` 또는 ESC로 해제하세요`) 해제 후 재실행을 안내합니다.

---

## STEP 0 — 기능 설명 추출

`/pipeline {기능 설명}` 형식으로 받은 텍스트에서 `{기능 설명}` 부분을 추출해 `INITIAL_REQUIREMENT` 변수에 저장합니다.

기능 설명이 없으면 (예: `/pipeline` 만 입력된 경우):
```
기획 파이프라인을 시작합니다.
어떤 기능을 만들고 싶으신가요? 간단히 설명해주세요.
```
라고 물어보고 답변을 `INITIAL_REQUIREMENT`로 사용합니다.

---

## STEP 1 — 요구사항 분석

`/requirement` 스킬을 `Skill` 도구로 호출합니다.

**초기 요구사항 전달**: STEP 0에서 저장한 `INITIAL_REQUIREMENT`를 `/requirement` 스킬의 첫 번째 입력으로 전달합니다. 사용자가 이미 기능을 설명했으므로 스킬 내부의 초기 요구사항 파악 단계(STEP 1)에서 이 내용을 출발점으로 사용합니다.

이 단계 완료 조건:
- 사용자가 질문에 충분히 답변함
- `docs/requirements.md` 파일이 생성됨

완료 확인 후 진행 배너 출력:
```
✅ STEP 1/4 완료 — docs/requirements.md 생성됨
▶ STEP 2/4 시작 — 도메인 모델 설계 (/domain)
```

---

## STEP 2 — 도메인 모델 설계

STEP 1 완료 후 `/domain` 스킬을 `Skill` 도구로 호출합니다.

이 단계 완료 조건:
- 사용자가 설계 결정 질문에 답변함
- `docs/domain-model.md` 파일이 생성됨

완료 확인 후 진행 배너 출력:
```
✅ STEP 2/4 완료 — docs/domain-model.md 생성됨
▶ STEP 3/4 시작 — API 명세서 작성 (/api)
```

---

## STEP 3 — API 명세서 작성

STEP 2 완료 후 `/api` 스킬을 `Skill` 도구로 호출합니다.

이 단계 완료 조건:
- API 설계 결정이 완료됨
- `docs/api-spec.md` 파일이 생성됨

완료 확인 후 진행 배너 출력:
```
✅ STEP 3/4 완료 — docs/api-spec.md 생성됨
▶ STEP 4/4 시작 — GitHub 이슈 생성 (/github-issue)
```

---

## STEP 4 — GitHub 이슈 생성

STEP 3 완료 후 `/github-issue` 스킬을 `Skill` 도구로 호출합니다.

이 단계 완료 조건:
- 사용자가 이슈 목록을 확인하고 승인함
- 모든 이슈가 GitHub에 생성됨
- `docs/issue-list.md` 파일이 생성됨
- 첫 번째 이슈 브랜치가 생성 및 체크아웃됨

완료 후 최종 보고:
```
🎉 파이프라인 완료!

생성된 파일:
- docs/requirements.md
- docs/domain-model.md
- docs/api-spec.md
- docs/issue-list.md

이제 /feature 스킬로 구현을 시작할 수 있습니다.
```

---

## 중단 처리

사용자가 "취소", "중단", "cancel", "stop" 을 입력하면 현재 STEP 이후 파이프라인을 중단하고 안내합니다:
```
⏸ 파이프라인이 STEP {N}/4에서 중단되었습니다.
재개하려면 다음 스킬을 직접 실행하세요: /{skill-name}
```

---

## 제약사항

- 각 STEP 내부의 사용자 상호작용(질문, 확인)을 생략하거나 대신 답변하지 않음
- 각 STEP의 출력 파일이 실제로 생성됐는지 확인 후 다음 STEP으로 진행
- STEP 0의 기능 설명은 `/requirement`에 전달만 하고, 요구사항 분석은 `/requirement` 스킬에 위임

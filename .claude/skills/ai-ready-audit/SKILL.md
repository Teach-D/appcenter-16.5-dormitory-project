---
name: ai-ready-audit
description: AI-Ready Codebase Rubric(7개 카테고리, 100점 만점)에 따라 현재 프로젝트의 AI 준비도를 평가하고 카테고리별 점수·근거·개선 권고를 제공하는 스킬. "AI-ready 평가해줘", "AI 준비도 점수 알려줘", "코드베이스 AI 친화도 평가", "AI-ready audit 해줘", "rubric으로 평가해줘", "AI 친화도 채점해줘", "몇 점짜리 AI 환경이야" 등 AI 준비도·AI 친화도 평가 관련 요청이 오면 반드시 이 스킬을 사용한다.
---

# AI-Ready Codebase Auditor

현재 프로젝트의 AI 준비도를 7개 카테고리(총 100점)로 채점하고, 카테고리별 점수 근거와 우선순위 개선안을 제공한다.

---

## Step 1 — 컨텍스트 파일 수집

다음 파일/경로를 읽어 채점 근거를 수집한다. 없으면 없음으로 기록한다.

- `CLAUDE.md` (루트)
- `docs/` 하위 모든 `.md` 파일
- `.claude/rules/` 하위 모든 `.md` 파일
- `.claude/skills/` 하위 모든 `SKILL.md`
- `.claude/hooks/` 하위 파일 목록 (내용까지)
- `.claude/settings.json`

---

## Step 2 — 7개 카테고리 채점

### A. AI Navigation & Coverage (15점)

| 점수 | 기준 |
|------|------|
| 0 | CLAUDE.md 없음, AI가 grep/search로 구조를 추측해야 함 |
| 5 | 일부 module에 README/context 존재 |
| 10 | 대부분 핵심 module에 역할·entry point·related files 정리 |
| 15 | 모든 핵심 module/workflow에 AI navigation guide 존재, 1-2 hops 안에 찾을 수 있음 |

측정식: `AI context로 안내 가능한 핵심 module 수 / 전체 핵심 module 수`

### B. Context Document Quality (20점)

각 항목 4점씩:

| 항목 | 기준 |
|------|------|
| B1. Conciseness | 각 context 파일이 25-35줄 또는 ~1,000 tokens 내외 |
| B2. Quick Commands | copy-paste 가능한 명령어와 사용 시점 포함 |
| B3. Key Files | 실제 수정에 필요한 3-5개 핵심 파일 명시 |
| B4. Non-Obvious Patterns | 실패를 유발하는 hidden rule과 예외 명시 |
| B5. See Also / Cross References | 관련 module·context file·dependency map 연결 |

### C. Tribal Knowledge Externalization (20점)

| 점수 | 기준 |
|------|------|
| 0 | senior engineer, Slack, 과거 PR에만 지식 존재 |
| 5 | 일부 gotcha가 README/comment에 흩어짐 |
| 10 | 반복 작업 암묵지 일부 문서화 |
| 15 | compatibility rule, naming convention, generated code rule, deprecated-but-required rule 정리 |
| 20 | 식별된 tribal knowledge 대부분이 context file/checklist/playbook에 반영, AI 질의로 회수 가능 |

**Meta Five-Question Framework** — 각 핵심 module에 아래 5개 질문에 답 가능하면 4점씩(최대 20점):
1. What does this module configure/own?
2. What are common modification patterns?
3. What non-obvious patterns cause failures?
4. What are the cross-module dependencies?
5. What tribal knowledge is hidden in comments/history/human memory?

### D. Cross-Module Dependency & Data Flow Mapping (15점)

| 점수 | 기준 |
|------|------|
| 0 | 변경 영향 범위를 사람이 수동으로 추적 |
| 5 | 일부 architecture diagram 또는 dependency note 존재 |
| 10 | 주요 module 간 dependency와 ownership 문서화 |
| 15 | "What depends on X?"에 대해 graph/index/map으로 답 가능, 변경 전파 경로 추적 가능 |

### E. Verification & Quality Gates (15점)

| 항목 | 점수 | 기준 |
|------|:----:|------|
| E1. Reference Accuracy | 5 | file path, API, command hallucination 0건 |
| E2. Independent Critic Review | 4 | 최소 2-3 round 독립 review 또는 checklist 존재 |
| E3. Task Validation | 4 | build/test/lint 등 변경 유형별 검증 명령 제공 |
| E4. Prompt/Workflow Tests | 2 | 대표 AI task query를 실제로 테스트한 증거 |

### F. Freshness & Self-Maintenance (10점)

| 점수 | 기준 |
|------|------|
| 0 | context 수동 관리, stale 여부 불명 |
| 3 | 문서 owner 있고 가끔 업데이트 |
| 6 | CI/script로 broken path/reference 일부 검출 |
| 10 | 주기적 자동 실행 — file path validation, coverage gap detection, critic review, stale reference repair |

### G. Agent Performance Outcomes (5점)

| 점수 | 기준 |
|------|------|
| 0 | AI 성능 개선 측정 없음 |
| 2 | 정성적으로 "도움 된다" 수준 |
| 3 | 대표 task success rate 또는 human intervention rate 측정 |
| 5 | tool calls, token usage, task completion time, correctness, prompt pass rate를 before/after로 측정 |

---

## Step 3 — 결과 출력

### 점수표 형식

```
## AI-Ready Codebase 점수표

| 카테고리 | 만점 | 획득 | 요약 |
|---------|:----:|:----:|------|
| A. AI Navigation & Coverage | 15 | ? | ... |
| B. Context Document Quality | 20 | ? | ... |
| C. Tribal Knowledge Externalization | 20 | ? | ... |
| D. Cross-Module Dependency & Data Flow | 15 | ? | ... |
| E. Verification & Quality Gates | 15 | ? | ... |
| F. Freshness & Self-Maintenance | 10 | ? | ... |
| G. Agent Performance Outcomes | 5 | ? | ... |
| **Total** | **100** | **?** | |

**등급: [등급명]** — [등급 의미]
```

**등급 기준:**

| 점수 | 등급 | 의미 |
|------|------|------|
| 90-100 | AI-Native / Agentic-Ready | Agent가 대부분의 반복 작업을 자율 수행, context layer도 self-maintaining |
| 75-89 | AI-Ready | 대부분 개발 작업에서 AI가 안정적으로 navigation·edit·verify 가능 |
| 60-74 | AI-Assisted | AI가 유용하지만 complex/domain-specific task에는 human context 필요 |
| 40-59 | AI-Fragile | 간단한 task는 가능하나 hidden rule·dependency 때문에 오류 위험 높음 |
| <40 | AI-Hostile | tribal knowledge 의존도 높고 AI가 추측 기반으로 작업함 |

### 카테고리별 상세 형식

각 카테고리를 아래 형식으로 출력한다. 감점이 크고 개선 효과가 높은 카테고리를 먼저 다룬다.

```
### [카테고리명]: [획득점수]/[만점]점

**현재 상태:**
[어떤 파일이 있는지·없는지 구체적인 근거]

**감점 이유:**
[점수를 깎은 구체적인 이유]

**개선 권고 (우선순위 순):**
1. [즉시 실행 가능한 액션 — 파일 1개 추가/수정 수준]
2. [중기 개선 — 구조적 변화 필요]
```

---

## Step 4 — 마무리 안내

1. 다음 등급으로 올라가기 위해 ROI가 가장 높은 카테고리 2개를 추천한다.
2. 즉시 개선 가능한 항목(파일 1개 추가/수정)과 장기 개선 항목(구조 변경 필요)을 구분해서 안내한다.
3. 점수 개선 후 재평가 원하면 다시 호출하라고 안내한다.

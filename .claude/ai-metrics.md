# AI 기여도 측정 로그

PR 완료 시 아래 항목을 기록한다. 누적 데이터로 AI 작업 효율 추적.

---

## 기록 템플릿

```
### PR #[번호] — [제목]
- 날짜: YYYY-MM-DD
- AI 생성 라인 (순수 AI 작성): ~N줄
- 인간 수정 라인 (AI 결과물 수정): ~N줄
- 보완 비율: N% (인간 수정 / 전체 변경)
- human clarification 횟수: N회 (AI가 방향을 되물은 횟수)
- 주요 오류 유형: [없음 / antipatterns / 실수재발 / 기타]
- 메모: [특이사항]
```

---

## 측정 기록

### PR #617 — fix: last_active_date 타임존 오류 수정
- 날짜: 2026-04-27
- AI 생성 라인: ~8줄 (LocalDate → Instant 변환)
- 인간 수정 라인: ~0줄
- 보완 비율: 0%
- human clarification 횟수: 0회
- 주요 오류 유형: antipatterns (LocalDate/외부API 오탈자)
- 메모: AI가 antipatterns.md의 Instant 규칙을 참조해 자동 수정. 이후 규칙 파일에 실제 발생 사례로 추가됨.

### PR #618 — fix: FCM 전체 알림 N+1 쿼리 및 트랜잭션 오류 수정
- 날짜: 2026-04-27
- AI 생성 라인: ~45줄 (bulkEnqueueOutbox 패턴 구현 + @Modifying 수정)
- 인간 수정 라인: ~5줄
- 보완 비율: 10%
- human clarification 횟수: 1회 (bulk insert 방식 확인)
- 주요 오류 유형: antipatterns-jpa (N+1, @Modifying @Transactional 누락)
- 메모: AnnouncementNotificationService N+1 → bulkEnqueueOutbox 패턴으로 전환. antipatterns-jpa.md에 실제 사례로 추가됨.

---

## 집계

| 기간 | 총 PR | 평균 보완비율 | 평균 clarification | 주요 오류 패턴 |
|------|:-----:|:-----------:|:-----------------:|--------------|
| 2026 Q2 | 2 | 5% | 0.5회 | antipatterns (JPA N+1, Instant) |

---

## 측정 방법 가이드

### 라인 수 계산

```bash
# PR diff 라인 수
git diff main...HEAD --stat

# AI가 작성한 파일만 집계 (Claude Code 세션 완료 후)
git diff main...HEAD -- '*.java' | grep '^+' | grep -v '^+++' | wc -l
```

### 보완 비율 계산

```
보완 비율 = (인간이 수정한 라인 수) / (전체 변경 라인 수) × 100
```

낮을수록 AI 출력 품질이 높음. 목표: **20% 이하**

### Before/After 비교 (기능 단위)

| 지표 | Before (수동 개발) | After (AI 지원) |
|------|:-----------------:|:--------------:|
| 구현 시간 | 기준값 | 측정 후 기록 |
| 버그 발생 | 기준값 | 측정 후 기록 |
| clarification 횟수 | — | PR당 평균 |

실제 측정값이 쌓이면 Before 기준값을 채울 것.

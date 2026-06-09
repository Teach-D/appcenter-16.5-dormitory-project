---
name: api
description: requirements.md와 domain-model.md를 읽고 테스트/구현/프론트엔드 에이전트가 바로 사용할 수 있는 API 명세서(api-spec.md)를 docs/ 폴더에 생성하는 스킬. URL은 /도메인명 복수형으로 시작하며, 에러 컨트랙트·동시성·사이드이펙트까지 명시합니다. 사용자가 "API 설계", "API 명세", "api spec", "엔드포인트 설계", "/api" 등을 언급할 때 반드시 사용하세요.
---

# API Design — API 명세서 작성

당신은 REST API 설계 전문가입니다.
requirements.md와 domain-model.md를 읽고, 테스트·구현·프론트엔드 에이전트가 이 문서만 보고 작업할 수 있는 API 명세서를 만드는 것이 목표입니다.

이 스킬은 단순 CRUD 목록 생성기가 아닙니다. **비즈니스 행동 명세(business behavior contract)** 를 만드는 것이 목표입니다.

---

## STEP 0 — 입력 확인

`docs/requirements.md`와 `docs/domain-model.md`가 모두 존재하는지 확인합니다.

파일이 없으면 작업을 중단하고 사용자에게 안내합니다:
```
requirements.md 또는 domain-model.md가 없습니다.
/requirement → /domain 스킬을 순서대로 먼저 실행해주세요.
```

파일이 여러 개면 어떤 파일을 기반으로 작성할지 사용자에게 확인합니다.

---

## STEP 1 — 입력 문서 분석

두 문서를 읽고 아래 항목을 추출합니다.

- **리소스** → 도메인 모델의 애그리거트 루트 기준으로 URL 도출
- **사용자 역할** → 역할별 접근 가능 엔드포인트
- **비즈니스 액션** → 상태 변화가 있는 행동 (단순 CRUD 초월)
- **비즈니스 규칙(BR)** → 각 엔드포인트에 귀속될 규칙 번호
- **도메인 이벤트** → API 호출로 발행되는 이벤트와 사이드 이펙트
- **동시성 위험** → 중복 요청, 경쟁 조건이 있는 엔드포인트

---

## STEP 2 — 불명확한 부분 질문

API 설계 결정에 필요한 부분을 질문합니다.

규칙:
- 한 번에 최대 3개
- 구현 방식이 아닌 비즈니스 결정 사항만 질문
- 각 질문에 선택지와 트레이드오프 반드시 제시

질문 형식:
```
1. [설계 결정] 질문 내용

   선택지:
     A) ~ → 장점: ~ / 단점: ~
     B) ~ → 장점: ~ / 단점: ~
```

충분한 정보가 모이면 STEP 3으로 진행합니다.

---

## STEP 3 — api-spec.md 작성 & 저장

아래 형식으로 문서를 작성하고 `docs/api-spec.md`로 저장합니다.

**URL 규칙**: 모든 엔드포인트는 `/도메인명(복수형)`으로 시작합니다.
예: `/orders`, `/users`, `/notifications`, `/group-orders`

---

문서 형식:

```markdown
# API 명세서

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`

---

## 1. 공통 정보

### Base URL
`/도메인명(복수형)`

### 인증 방식
- 헤더: `Authorization: Bearer {accessToken}`
- 만료: {시간}
- 미인증 시: `401 UNAUTHORIZED`

### 공통 응답 형식

**성공:**
```json
{
  "success": true,
  "data": {},
  "message": null
}
```

**실패:**
```json
{
  "success": false,
  "data": null,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

### 공통 에러 코드

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| UNAUTHORIZED | 401 | 인증 토큰 없음 또는 만료 |
| FORBIDDEN | 403 | 권한 없음 |
| NOT_FOUND | 404 | 리소스 없음 |
| VALIDATION_ERROR | 400 | 요청값 검증 실패 |
| CONFLICT | 409 | 비즈니스 충돌 (중복, 상태 불일치 등) |
| INTERNAL_ERROR | 500 | 서버 내부 오류 |

### 페이지네이션 (목록 조회 공통)

**Query Parameters:**
| 이름 | 타입 | 기본값 | 설명 |
|------|------|--------|------|
| page | number | 0 | 페이지 번호 (0-based) |
| size | number | 20 | 페이지 크기 (max: 100) |
| sort | string | createdAt,desc | 정렬 기준 |

**Response 공통 필드:**
```json
{
  "content": [],
  "totalElements": 0,
  "totalPages": 0,
  "currentPage": 0,
  "hasNext": false
}
```

---

## 2. API 목록

### {도메인명} API

---

#### {API 이름}

**`{HTTP Method} /도메인명(복수형)[/{pathParam}]`**

| 항목 | 내용 |
|------|------|
| 설명 | {한 줄 설명} |
| 인증 | 필요 / 불필요 |
| 권한 | {Role} |
| 멱등성 | 멱등 / 비멱등 |

**Path Parameters:**
| 이름 | 타입 | 설명 |
|------|------|------|
| {name} | Long | {설명} |

**Query Parameters:**
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| {name} | String | N | - | {설명} |

**Request Body:**
```json
{
  "fieldName": "타입 | 설명 | 필수여부"
}
```

**Validation Rules:**
- `{fieldName}`: {규칙} (예: 1~100자, 공백 불가, null 불가)

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "fieldName": "타입 | 설명"
  }
}
```

**비즈니스 로직 요약:**
1. {처리 흐름 단계별 기술}
2. {BR 번호 인용} 적용 (예: BR-1-2: 재고 0이면 주문 불가)
3. {상태 변화 명시}

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| {상황} | 400 | {CODE} | {메시지} |
| {상황} | 404 | {CODE} | {메시지} |
| {상황} | 409 | {CODE} | {메시지} |

**동시성 & 멱등성:**
- {중복 요청 처리 방식 또는 "해당 없음"}

**사이드 이펙트 / 도메인 이벤트:**
- {발행 이벤트명}: {발행 조건 및 처리 내용}

**엣지 케이스:**
- [ ] {케이스}: {처리 방식}

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|------------|-----------|-----------|
| {API명} | {이벤트명} | {서비스/애그리거트} | {동작} |

---

## 4. API 간 의존 관계

- `{API-A}` 호출 전 `{API-B}` 선행 필요 → 이유: {설명}
- `{API-C}`와 `{API-D}` 동시 호출 불가 → 이유: {설명}

---

## 5. 보안 체크리스트

- [ ] 모든 쓰기 API에 인증 적용
- [ ] 타인 리소스 접근 차단 (ID 소유권 검증)
- [ ] 민감 데이터 응답 마스킹
- [ ] 요청 크기 제한
- [ ] Rate Limit 적용 대상 API 식별

---

## 6. 최종 검토

- [ ] ambiguous endpoint 없음
- [ ] 숨겨진 동시성 위험 식별 완료
- [ ] 누락된 인가(authorization) 검사 없음
- [ ] 최종 일관성(eventual consistency) 처리 방식 명시 완료
- [ ] 하위 호환성 위험 없음

---

## 7. TBD
- 미결 사항 목록
```

---

## 제약사항

- Spring, JPA 등 구현 기술 언급 금지
- 모든 URL은 `/도메인명(복수형)`으로 시작
- domain-model.md의 비즈니스 규칙 번호(BR-xx-xx)를 비즈니스 로직 요약에 반드시 인용
- requirements.md에 없는 API를 임의로 추가하지 않음
- 추가가 필요하다 판단되면 TBD에 기록하고 사용자에게 확인
- 단순 CRUD 사고 금지: 행동(action) 중심으로 설계

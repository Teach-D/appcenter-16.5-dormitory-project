# 요구사항 명세서 — 공동구매 음식점 명칭 정규화 (Kakao Local API)

## 1. 개요

- **서비스 목적**
  - 공동구매(GroupOrder) 등록 시 음식점 명칭을 카카오 로컬 API로 정규화하여 같은 음식점에 대한 표기 중복(예: "BBQ 송도점", "비비큐 송도", "bbq 송도점")을 제거한다.
  - 정규화된 `placeId`를 기반으로 통계·검색·UX 일관성을 개선한다.
  - 운영 중 누적된 중복 데이터를 일회성 마이그레이션으로 정규화한다.

- **핵심 사용자**
  - `USER`: 공동구매 작성자/참여자 — 음식점 입력의 주체
  - `ADMIN`: 마이그레이션 실행, 통계 조회
  - `DORMITORY`: 영향 없음 (참고용)

- **범위**
  - **In Scope**
    - 카카오 로컬 API 키워드 검색 연동(`/v2/local/search/keyword.json`)
    - `Place` 엔티티 신규 도입 (1:N으로 여러 GroupOrder가 동일 Place 참조)
    - `GroupOrder`에 `place_id`(FK, nullable) + `raw_place_name`(원본 보존) 컬럼 추가
    - `GroupOrderType = FOOD` 인 경우에만 정규화 적용
    - 음식점 검색 endpoint(자동완성용) + 자유 텍스트 입력 둘 다 지원 (하이브리드)
    - Redis 캐시(24h, NOT_FOUND sentinel) + 일일 사용량/적중률 카운터
    - 어드민 마이그레이션 endpoint + 통계 조회 endpoint (ADMIN 전용)
    - 정규화 실패(NOT_FOUND, API 실패) 시 `place_id=null` fallback 등록 허용
  - **Out of Scope**
    - 음식 외 `GroupOrderType`(택배·공구 등)에 대한 정규화
    - 사용자가 placeId를 임의 수정/재정규화 (작성 시점에만 정규화)
    - 카카오 외 다른 지도 API 연동 (네이버 등)
    - 음식점별 메뉴/평점 등 부가 정보 수집
    - 결제·배송 연동

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 | 변경 여부 |
|--------|----------|-----------|
| `Place` | id, placeId(UQ, 카카오 ID), placeName, roadAddress, latitude, longitude, createdAt, updatedAt | **신규** |
| `GroupOrder` | (기존 속성) + **place(FK, nullable)**, **rawPlaceName(원본 입력 보존)** | **컬럼 추가** |
| `GroupOrderChatRoom` | (변경 없음) | 변경 없음 |
| `GroupOrderType` enum | `FOOD`, 그 외 | 변경 없음 (FOOD만 정규화 대상) |

### 엔티티 간 관계

- `Place` 1 ↔ N `GroupOrder`
  - 같은 `place_id`를 가진 카카오 장소는 단 하나의 `Place` row로 통합
  - 여러 공동구매가 같은 음식점에 대해 별도로 생성될 수 있으며 모두 같은 `Place`를 참조
- `GroupOrder.place`는 **nullable**
  - `groupOrderType != FOOD` → 항상 null
  - `groupOrderType = FOOD` + 카카오 정규화 성공 → Place 연결
  - `groupOrderType = FOOD` + 카카오 NOT_FOUND/API 실패 → null (fallback)

### 상태/플래그 (캐시 영역)

```
Redis Key 구조
  kakao:place:{normalizedKeyword}         값: placeId 또는 "__NOT_FOUND__" sentinel  (TTL 24h)
  kakao:usage:{yyyy-MM-dd}                값: 일일 API 호출 카운트 (INCR)
  kakao:cache:hit                         값: 누적 캐시 적중 수
  kakao:cache:miss                        값: 누적 캐시 미스 수
```

---

## 3. 비즈니스 규칙

1. **BR-01** 음식점 정규화는 `GroupOrderType = FOOD` 인 경우에만 수행한다.
   - 위반(다른 타입에 placeId 전달) 시: 무시하고 `place_id=null`로 저장 (오류 아님)

2. **BR-02** 공동구매 작성 요청 시 `placeId`와 `rawPlaceName`은 모두 선택 입력이며, 다음 우선순위로 처리한다.
   - (a) `placeId` 제공 → 서버는 카카오 ID 유효성만 확인(없으면 카카오 조회) 후 Place 연결
   - (b) `placeId` 없음 + `rawPlaceName` 있음 → 서버가 카카오 키워드 검색으로 자동 매칭
   - (c) 둘 다 없음 → 음식 타입이라도 정규화 생략, place_id null 저장
   - 어떤 경우에도 `rawPlaceName`은 원본 그대로 DB에 저장 (사용자 표기 보존)

3. **BR-03** 카카오 API 매칭 결과 처리
   - **성공**: 검색 결과 첫 번째 document의 `id`를 `placeId`로 사용, 신규면 `Place` row 생성, 기존이면 재사용
   - **NOT_FOUND**: `place_id=null`로 등록 허용, Redis에 `__NOT_FOUND__` sentinel 적재 (TTL 24h)
   - **API 5xx / 타임아웃**: 3회 재시도(지수 백오프), 최종 실패 시 `place_id=null` fallback + WARN 로그
   - 어떤 경우에도 공동구매 작성 자체는 실패시키지 않는다 (관용 정책, 답변 4-A)

4. **BR-04** Redis 캐시 정책
   - 키 정규화: `trim() + toLowerCase() + 중복 공백 단일화`
   - TTL 24h
   - 음수 캐시 sentinel `__NOT_FOUND__` → 같은 미발견 키워드 반복 호출 차단
   - 캐시 히트 시 카카오 API 호출하지 않음

5. **BR-05** 카카오 일일 호출 한도 가드
   - `kakao:usage:{yyyy-MM-dd}` ≥ 250,000 도달 시 신규 호출 차단 + Slack 알림 1회 발송
   - 차단된 호출은 NOT_FOUND와 동일하게 처리 (fallback)
   - 한도는 환경설정값으로 분리, 무료 한도 300,000 대비 안전 마진 50,000

6. **BR-06** 트랜잭션 경계
   - 카카오 API 호출은 `@Transactional` 밖에서 수행 (외부 호출 트랜잭션 포함 금지)
   - `Place` 저장은 `@Transactional(propagation = REQUIRES_NEW)` 별도 트랜잭션
   - `GroupOrder` 저장 트랜잭션이 롤백되어도 `Place` row는 유지(다른 공동구매가 재사용 가능)

7. **BR-07** 마이그레이션 endpoint(`POST /admin/places/migrate`)
   - `ADMIN` 권한만 호출 가능, 위반 시 403 FORBIDDEN
   - 청크 500건 페이징, `place_id IS NULL AND groupOrderType = 'FOOD'` 조건만 처리 (idempotent)
   - 일 처리 상한 10,000건, 카카오 호출 사이 100ms sleep
   - 운영 시간대(09–18시) 자동 정지 (한도 보호)

8. **BR-08** 통계 조회 endpoint(`GET /admin/places/stats`)
   - `ADMIN` 권한만 호출 가능, 위반 시 403 FORBIDDEN
   - 응답: 중복 등록 N→M, 신규 등록 중복률(최근 7일), 일별 카카오 호출 수, 캐시 적중률, 무료 한도 사용률, fallback 발생률

9. **BR-09** 음식점 검색 endpoint(`GET /group-orders/places/search?keyword=...`)
   - 인증된 모든 사용자(`USER`, `ADMIN`, `DORMITORY`) 호출 가능
   - 카카오 키워드 검색 결과 상위 N개(예: 10개) 반환 — 프론트가 자동완성/선택 UI에 사용
   - 동일하게 Redis 캐시 통과, 카운터 누적
   - 입력 길이 2자 미만 시 빈 리스트 반환 (호출 절약)

10. **BR-10** 통계는 `GROUP BY place_id`로 집계 (같은 place_id 행 병합 금지)
    - 각 공동구매는 시점·주최자가 다른 독립 이벤트이므로 row 병합은 의미가 없다.
    - 통계 노출 시 `place_id IS NULL`은 "미정규화" 그룹으로 별도 집계.

---

## 4. 사용자 & 권한

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` | 공동구매 작성/조회(기존), 음식점 검색 endpoint 호출 |
| `ADMIN` | 위 모든 권한 + 마이그레이션 실행 + 통계 조회 |
| `DORMITORY` | 음식점 검색 endpoint 호출 가능 (조회 권한 기존 정책 따름) |
| 비인증 | 없음 |

---

## 5. 주요 시나리오

### Happy Path — A. 자유 텍스트 입력
1. USER가 공동구매 작성 시 `rawPlaceName="BBQ 송도점"`, `placeId=null`, `groupOrderType=FOOD` 전송
2. 서버가 키워드 정규화 → `"bbq 송도점"` → Redis 조회 (miss)
3. 카카오 키워드 검색 호출, 첫 번째 결과 `id=12345` 획득
4. `Place(placeId=12345)` 신규 저장 후 `GroupOrder.place` 연결, `rawPlaceName`은 원본 그대로 저장
5. Redis에 `kakao:place:bbq 송도점 → 12345` 저장, 사용량 카운터 +1, miss 카운터 +1
6. 다른 USER가 `rawPlaceName="비비큐 송도"`로 작성 → 정규화 키 다름 → API 호출 → 같은 `id=12345` 반환 → 기존 Place 재사용

### Happy Path — B. 선택형(자동완성)
1. USER가 입력 중 프론트가 `GET /group-orders/places/search?keyword=BBQ` 호출
2. 서버가 상위 10개 후보 반환 (카카오 → 프론트)
3. USER가 후보 중 하나 선택, 프론트가 `placeId=12345`, `rawPlaceName="BBQ 송도점"` 전송
4. 서버가 placeId로 Place 조회/저장 (API 재호출 불필요 시 캐시 사용)
5. GroupOrder 저장

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| `groupOrderType != FOOD` | 정규화 생략, `place_id=null` 저장, `rawPlaceName`도 무시 |
| 카카오 검색 결과 0건 | `place_id=null` fallback, Redis NOT_FOUND sentinel 저장, 작성 성공 |
| 카카오 5xx/타임아웃 (재시도 3회 모두 실패) | `place_id=null` fallback + WARN 로그, 작성 성공 |
| 일일 한도 250,000 도달 후 호출 | 호출 차단, NOT_FOUND와 동일 처리, Slack 알림 (1일 1회) |
| 마이그레이션 도중 한도 도달 | 마이그레이션 일시 정지 + 다음 날 재개 (멱등) |
| 동일 `placeId` 동시 신규 저장 race | DB UQ 제약 + REQUIRES_NEW 트랜잭션 — 충돌 시 재조회 1회 |
| `placeId` 전송했지만 카카오에서 검증 실패 | `place_id=null` fallback (악의적 위변조 방지) |
| `keyword` 2자 미만 검색 요청 | 빈 리스트 반환 (API 미호출) |
| 마이그레이션 중 `rawPlaceName`이 빈 문자열 | 해당 row 스킵, 카운트에서 제외 |

---

## 6. 비기능 요구사항

- **성능**
  - 공동구매 작성 응답 시간 영향 최소화: 캐시 히트 시 +5ms 이내, 미스 시 +300ms 이내 (카카오 API + DB)
  - 음식점 검색 endpoint: 캐시 미스 기준 500ms 이내
- **외부 연동**
  - 카카오 디벨로퍼스 REST API 키 발급 필요 (환경변수 `KAKAO_REST_API_KEY`)
  - 무료 한도 300,000 호출/일, 안전 마진 250,000 가드
- **인프라**
  - Redis (기존 인프라 재사용) — 캐시 + 카운터
  - Slack 웹훅 (기존 `SlackErrorNotifier` 재사용) — 한도 도달 알림
- **데이터 보존**
  - `rawPlaceName`은 정규화 성공 여부와 무관하게 영구 보존 (사용자 입력 추적용)
  - `Place` row는 삭제하지 않음 (FK 무결성 + 통계 보존)
- **확장성**
  - `Place` 엔티티는 추후 다른 도메인(예: 모임 장소)에서도 재사용 가능하도록 `groupOrder` 패키지 외부로 이동 가능한 구조 유지 (현 단계는 `groupOrder/entity` 배치)
- **관측성**
  - Phase 7 측정 지표를 통계 API(`GET /admin/places/stats`)로 노출
  - 호출 수·캐시·fallback 지표는 Redis 카운터 + 일별 로그로 이중 기록

---

## 7. 미결 사항 (TBD)

- [ ] **카카오 검색 정확도 검증 방안**: 마이그레이션 중 매칭 결과 무작위 샘플링 → 수동 검증 워크플로우 정의
- [ ] **`placeId` 검증 시 별도 API 호출 여부**: 현재는 신뢰하고 저장 → 위변조 가능성 vs 호출 수 증가 trade-off
- [ ] **통계 API 응답 형식**: 단순 JSON vs 시계열(일별 배열) vs CSV 다운로드 — 어드민 대시보드 UI 결정 후 확정
- [ ] **마이그레이션 야간 시간대 자동 정지**: 09–18시 정지 기준이 KST/UTC 어느 쪽인지, 휴일 처리 여부
- [ ] **`Place` 엔티티 위치**: `groupOrder/entity` vs 공용 `place/entity` 패키지 — 향후 재사용 가능성 검토 후 분리 여부 결정
- [ ] **프론트엔드 자동완성 UI 적용 시점**: 검색 endpoint 제공 후 프론트 작업 일정 협의 필요

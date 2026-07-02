# API 명세서 — 공동구매 음식점 명칭 정규화

> 기반 요구사항: `docs/requirements.md`
> 기반 도메인 모델: `docs/domain-model.md`
> 기능: 공동구매 음식점 명칭 정규화 (Kakao Local API)

---

## 1. 공통 정보

### Base URL
- 검색: `/places`
- 어드민(마이그레이션/통계): `/admin/places`
- 공동구매(기존 확장): `/group-orders`

### 인증 방식
- 헤더: `Authorization: Bearer {accessToken}`
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

### 신규 ErrorCode (본 작업 추가)

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `KAKAO_API_ERROR` | 502 | 카카오 로컬 API 호출 최종 실패 (재시도 후) — 검색 endpoint에서만 노출, 작성 endpoint는 fallback |
| `KAKAO_QUOTA_EXCEEDED` | 503 | 일일 호출 한도 도달로 호출 차단 — 검색 endpoint에서만 노출 |
| `KEYWORD_TOO_SHORT` | 400 | 검색 키워드 2자 미만 (BR-09) — 비즈니스 정책상 빈 리스트 반환으로 처리되어 실제로는 노출 안 됨 |
| `INVALID_PLACE_FOR_TYPE` | 400 | placeId/rawPlaceName이 `groupOrderType != FOOD`와 함께 전송됨 (INV-GO-N1) |
| `PLACE_ID_INVALID` | 400 | 클라이언트가 보낸 placeId가 카카오에 존재하지 않음(위변조 의심) — TBD 정책에 따라 fallback 또는 거부 |

### 공통 에러 코드 (기존)

| 코드 | HTTP Status | 설명 |
|------|-------------|------|
| `UNAUTHORIZED` | 401 | 인증 토큰 없음 또는 만료 |
| `FORBIDDEN` | 403 | 권한 없음 |
| `VALIDATION_ERROR` | 400 | 요청값 검증 실패 |
| `INTERNAL_ERROR` | 500 | 서버 내부 오류 |

---

## 2. API 목록

### 2.1 Places (검색)

---

#### 음식점 검색 (자동완성용)

**`GET /places/search`**

| 항목 | 내용 |
|------|------|
| 설명 | 카카오 로컬 키워드 검색으로 음식점 후보 리스트 반환. 프론트엔드 자동완성 UI에서 사용. |
| 인증 | 필요 |
| 권한 | `USER`, `ADMIN`, `DORMITORY` (인증된 모든 사용자) |
| 멱등성 | 멱등 (캐시 24h) |
| Rate Limit | 카카오 전체 한도(BR-05)에 합산되므로 응용단에서 별도 제한 없음. 클라이언트 debounce 권장 |

**Query Parameters:**
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `keyword` | String | Y | - | 검색어. 2자 미만이면 빈 리스트 반환 (BR-09) |
| `size` | int | N | 10 | 반환 개수. 1~15 범위. 카카오 page=1 기준 |

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "keyword": "BBQ",
    "results": [
      {
        "placeId": "12345678",
        "placeName": "BBQ 송도점",
        "roadAddress": "인천 연수구 컨벤시아대로 165",
        "latitude": 37.3911,
        "longitude": 126.6391
      }
    ]
  }
}
```

**비즈니스 로직 요약:**
1. `keyword.trim().length() < 2`면 즉시 빈 results 반환 (API 호출 X). (BR-09)
2. 키워드 정규화(`trim + lowercase + 공백 단일화`) → Redis 캐시 조회. (BR-04)
3. **캐시 hit (placeId)**: 단일 결과를 Place 저장소에서 조회 후 1건 반환.
4. **캐시 hit (NOT_FOUND sentinel)**: 빈 results 반환 (API 호출 X). (BR-04)
5. **캐시 miss**: QuotaGuard 확인 → 한도 미만이면 카카오 호출, 한도 도달이면 `KAKAO_QUOTA_EXCEEDED` 503. (BR-05)
6. 카카오 응답을 `ResponsePlaceSearchItemDto` 리스트로 매핑하여 반환.
7. 응답 후 첫 번째 결과의 `placeId`만 캐시에 저장 (작성 흐름의 자동 매칭과 키 공유).
8. 카운터 갱신: `kakao:usage:{date}`, `kakao:cache:hit|miss`. (BR-04, BR-05)

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 미인증 | 401 | `UNAUTHORIZED` | 인증이 필요합니다. |
| 카카오 API 최종 실패 (재시도 3회 후) | 502 | `KAKAO_API_ERROR` | 외부 장소 검색 서비스에 일시적인 문제가 있습니다. |
| 일일 한도 도달 | 503 | `KAKAO_QUOTA_EXCEEDED` | 일일 외부 API 호출 한도에 도달했습니다. |
| keyword 누락 | 400 | `VALIDATION_ERROR` | keyword는 필수입니다. |
| size 범위 초과 | 400 | `VALIDATION_ERROR` | size는 1~15 사이여야 합니다. |

**동시성 & 멱등성:**
- 같은 키워드 동시 요청은 모두 같은 캐시 키를 조회 → 캐시 miss 시 중복 카카오 호출 가능 (요청 합치기 미적용, MVP 범위 외).

**사이드 이펙트 / 도메인 이벤트:**
- 도메인 이벤트 없음 (ADR-P-03).
- Redis 카운터 갱신만 발생.
- Place row를 **저장하지 않는다** (검색은 read-only). 작성 시점에만 Place 영속화.

**엣지 케이스:**
- [ ] 키워드에 특수문자/이모지: 카카오에 그대로 전달 (서버에서 별도 sanitize 없음).
- [ ] 카카오 응답 0건: 빈 results + NOT_FOUND sentinel 캐시 적재.
- [ ] 결과 1건만 있음: 정상 반환, 클라이언트는 자동 선택 가능.

---

### 2.2 GroupOrders (확장)

---

#### 공동구매 작성 (placeId/rawPlaceName 추가)

**`POST /group-orders`**

| 항목 | 내용 |
|------|------|
| 설명 | 기존 공동구매 작성 endpoint. `placeId`, `rawPlaceName` 두 optional 필드 추가. |
| 인증 | 필요 |
| 권한 | `USER` (기존 정책 유지) |
| 멱등성 | 비멱등 |
| 하위 호환성 | 신규 필드 둘 다 optional → 기존 클라는 영향 없음 (정규화 생략) (결정 3-A) |

**Request Body:**
```json
{
  "title": "string | 공동구매 제목 | 필수",
  "groupOrderType": "FOOD | 공동구매 유형 (enum) | 필수",
  "price": "int | 가격 | 필수",
  "link": "string | 외부 링크 | 선택",
  "openChatLink": "string | 오픈채팅 링크 | 선택",
  "deadline": "ISO-8601 LocalDateTime | 마감 일시 | 필수",
  "description": "string | 설명 | 선택",
  "placeId": "string | 카카오 장소 ID (자동완성 선택 시) | 선택 [신규]",
  "rawPlaceName": "string | 사용자 입력 음식점 원본 (자유 텍스트) | 선택 [신규]"
}
```

**Validation Rules:**
- `placeId`: 최대 30자. 형식은 카카오 측 ID 그대로(숫자 문자열).
- `rawPlaceName`: 최대 100자. 공백만 입력 시 null 처리.
- `placeId`와 `rawPlaceName`을 동시에 보내는 경우 `placeId` 우선 (BR-02).
- `groupOrderType != FOOD` 인데 `placeId` 또는 `rawPlaceName`이 비어있지 않으면 `INVALID_PLACE_FOR_TYPE` 400 반환. (INV-GO-N1)

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "id": 12345,
    "title": "BBQ 송도점 같이 시켜먹을 사람",
    "groupOrderType": "FOOD",
    "deadline": "2026-07-05T18:00:00",
    "place": {
      "id": 7,
      "placeId": "12345678",
      "placeName": "BBQ 송도점",
      "roadAddress": "인천 연수구 컨벤시아대로 165"
    },
    "rawPlaceName": "BBQ 송도점",
    "normalizationOutcome": "MATCHED"
  }
}
```

- `place`는 정규화 실패 시 `null`.
- `normalizationOutcome` enum: `MATCHED`, `NOT_FOUND`, `FALLBACK_ERROR`, `SKIPPED`, `QUOTA_EXCEEDED`.
  - 클라이언트는 `MATCHED`가 아니면 "음식점을 자동 인식하지 못했습니다" 같은 안내 노출 가능.

**비즈니스 로직 요약:**
1. Request 검증 + `groupOrderType` 별 정규화 분기. (BR-01)
2. **`groupOrderType != FOOD`** 또는 **둘 다 비어있음** → 정규화 생략, `place=null`, `rawPlaceName=null/원본` 저장. (BR-01, BR-02-(c))
3. **`groupOrderType == FOOD`** → PlaceResolver 호출 (트랜잭션 진입 **전**). (ADR-P-05)
   - `placeId` 우선 처리 (BR-02-(a)):
     - Place 저장소에 있음 → 그대로 사용 (`MATCHED`)
     - Place 저장소에 없음 → 카카오로 메타 조회 → 신규 Place 저장 또는 `PLACE_ID_INVALID` (TBD)
   - `placeId` 없음 + `rawPlaceName` 있음 (BR-02-(b)):
     - 키워드 정규화 → 캐시 → 미스면 카카오 검색 첫 결과 채택 → Place 저장/재사용 (`MATCHED`)
     - 카카오 결과 0건 → `NOT_FOUND` outcome, `place=null` fallback (BR-03)
     - 카카오 5xx/timeout 재시도 3회 실패 → `FALLBACK_ERROR` outcome, `place=null` fallback, WARN 로그 (BR-03)
     - 한도 도달 → `QUOTA_EXCEEDED` outcome, `place=null` fallback (BR-05)
4. PlaceResolver 결과를 가지고 GroupOrder 저장 트랜잭션 시작. `rawPlaceName`은 원본 그대로 보존. (INV-GO-N2)
5. 응답에 `place`와 `normalizationOutcome` 포함.

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 미인증 | 401 | `UNAUTHORIZED` | 인증이 필요합니다. |
| 필수 필드 누락 | 400 | `VALIDATION_ERROR` | {필드}는 필수입니다. |
| FOOD 외 타입에 place 필드 전송 | 400 | `INVALID_PLACE_FOR_TYPE` | FOOD 타입에만 음식점 정보를 입력할 수 있습니다. |
| placeId 위변조 (선택 정책) | 400 | `PLACE_ID_INVALID` | 유효하지 않은 음식점 ID입니다. (TBD-2 채택 시) |

- 카카오 API 자체 실패는 사용자에게 노출하지 않고 fallback (BR-03, 답변 4-A).

**동시성 & 멱등성:**
- 같은 `placeId`로 동시 작성 요청 → Place 저장소의 unique 제약(INV-P-01)으로 보호.
- Place 저장 충돌 시 PlaceResolver가 재조회 1회 후 기존 row 반환.
- GroupOrder는 비멱등이라 별도 처리 없음.

**사이드 이펙트 / 도메인 이벤트:**
- 도메인 이벤트 없음.
- 카운터 갱신: `kakao:usage:{date}`, `kakao:cache:hit|miss`.
- Place 신규 row 생성 (별도 트랜잭션, REQUIRES_NEW).

**엣지 케이스:**
- [ ] `placeId` 전송했지만 카카오에서 메타 조회 실패 → TBD에 따라 `PLACE_ID_INVALID` 거부 vs fallback. MVP는 fallback(`place=null`).
- [ ] 동일 사용자가 같은 음식점에 여러 공동구매 작성 → 기존 GroupOrder 정책 따름 (Place 측은 무관, 같은 Place 재사용).
- [ ] GroupOrder 저장 트랜잭션 실패 → Place row는 남음 (고아). 다음 작성 시 재사용 (ADR-P-05).
- [ ] `rawPlaceName="   "` (공백만) → null로 정규화하여 저장.

---

### 2.3 Admin / Places

---

#### 마이그레이션 실행

**`POST /admin/places/migrate`**

| 항목 | 내용 |
|------|------|
| 설명 | 기존에 `place=null AND groupOrderType=FOOD`인 공동구매 row를 청크 단위로 정규화. |
| 인증 | 필요 |
| 권한 | `ADMIN` 전용 (BR-07) |
| 멱등성 | 멱등 — 이미 정규화된 row는 자동 스킵 |

**Query Parameters:**
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `maxRows` | int | N | 10000 | 이번 실행에서 처리할 최대 row 수 (BR-07 상한). 1~10000 |
| `chunkSize` | int | N | 500 | 청크 페이지 크기. 100~1000 |
| `throttleMs` | int | N | 100 | 카카오 호출 간 대기(ms). 0~1000 |

**Request Body:** 없음

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "startedAt": "2026-06-30T22:00:00",
    "finishedAt": "2026-06-30T22:14:32",
    "scannedRows": 9842,
    "matchedRows": 7351,
    "notFoundRows": 1820,
    "errorRows": 671,
    "quotaExceededAtRow": null,
    "lastProcessedId": 18452
  }
}
```

- `quotaExceededAtRow`가 non-null이면 일일 한도 도달로 조기 종료된 것. 다음 날 같은 endpoint를 다시 호출하면 `lastProcessedId` 이후부터 자동 재개 (cursor 페이징, ADR-P-06).

**비즈니스 로직 요약:**
1. ADMIN 권한 확인. 위반 시 403 `FORBIDDEN`. (BR-07)
2. cursor=`0`부터 시작해서 `findUnnormalizedFoodOrders(chunkSize, lastId)` 청크 조회. (ADR-P-06)
3. 각 row의 `rawPlaceName`(없으면 `title`) 으로 PlaceResolver 호출.
4. 결과에 따라 카운터(`matchedRows`/`notFoundRows`/`errorRows`) 증가, 매칭된 row만 `place` 연결 업데이트. (INV-GO-N3 예외 조항)
5. 한 호출 후 `throttleMs` sleep. (BR-07)
6. `scannedRows >= maxRows` 또는 일일 한도 도달 시 종료.
7. 진행 로그(`progress=%`, `lastProcessedId`) 매 청크마다 INFO 로그.

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 미인증 | 401 | `UNAUTHORIZED` | 인증이 필요합니다. |
| 권한 없음 | 403 | `FORBIDDEN` | ADMIN 권한이 필요합니다. |
| chunkSize 범위 초과 | 400 | `VALIDATION_ERROR` | chunkSize는 100~1000 사이여야 합니다. |

**동시성 & 멱등성:**
- 동시 실행 방지: 동일 endpoint에 중복 호출 들어오면 두 번째 호출은 즉시 409 `CONFLICT` 반환 + Redis 락 `places:migration:running` 키 사용 (간이 락).
- 청크 단위 멱등: 같은 cursor에서 다시 시작해도 이미 처리된 row는 `place IS NULL` 조건에 걸리지 않아 스킵.

**사이드 이펙트 / 도메인 이벤트:**
- Place 신규 row 다수 생성.
- GroupOrder.place 컬럼 다수 갱신.
- 카운터 누적.
- 도메인 이벤트 없음.

**엣지 케이스:**
- [ ] 마이그레이션 중간 서버 재시작: 재호출 시 cursor 페이징으로 자연 재개.
- [ ] `rawPlaceName`도 `title`도 비어있는 row: 스킵 + `errorRows`에는 포함하지 않고 별도 `skippedRows`로 집계 (응답 확장).
- [ ] 한 row 처리 중 예외: 트랜잭션은 row 단위라 다른 row는 정상 진행. WARN 로그.
- [ ] 운영 시간대(09–18 KST) 자동 정지: 본 endpoint는 수동 호출이므로 운영자가 야간에 호출 (BR-07). 자동 정지 로직은 TBD.

---

#### 통계 조회

**`GET /admin/places/stats`**

| 항목 | 내용 |
|------|------|
| 설명 | 정규화 효과 측정 지표 조회 (BR-08, requirements Phase 7). 결정 2-B에 따라 요약 + 일별 시계열. |
| 인증 | 필요 |
| 권한 | `ADMIN` 전용 |
| 멱등성 | 멱등 |

**Query Parameters:**
| 이름 | 타입 | 필수 | 기본값 | 설명 |
|------|------|------|--------|------|
| `days` | int | N | 7 | 시계열 일수 (오늘 포함). 1~30 |

**Response (성공):**
```json
{
  "success": true,
  "data": {
    "summary": {
      "distinctRawPlaceNameCount": 312,
      "distinctPlaceIdCount": 184,
      "deduplicationRatio": 0.41,
      "newOrdersLastNDays": 47,
      "newOrdersNullPlaceLastNDays": 6,
      "fallbackRatio": 0.128,
      "cacheHitTotal": 14820,
      "cacheMissTotal": 3211,
      "cacheHitRatio": 0.822,
      "quotaUsageToday": 423,
      "quotaLimitDaily": 250000,
      "quotaUsageRatio": 0.0017
    },
    "daily": [
      {
        "date": "2026-06-24",
        "kakaoCalls": 612,
        "cacheHits": 2104,
        "cacheMisses": 612,
        "fallbacks": 28
      },
      {
        "date": "2026-06-25",
        "kakaoCalls": 549,
        "cacheHits": 2310,
        "cacheMisses": 549,
        "fallbacks": 22
      }
    ]
  }
}
```

**필드 정의:**
| 필드 | 정의 |
|------|------|
| `distinctRawPlaceNameCount` | `COUNT(DISTINCT TRIM(LOWER(rawPlaceName)))` — 정규화 BEFORE 지표 |
| `distinctPlaceIdCount` | `COUNT(DISTINCT place_id)` — 정규화 AFTER 지표 (place_id IS NULL 제외) |
| `deduplicationRatio` | `1 - (distinctPlaceIdCount / distinctRawPlaceNameCount)` — N→M 효과 |
| `newOrdersLastNDays` | 최근 `days`일 신규 생성된 FOOD 타입 GroupOrder 수 |
| `newOrdersNullPlaceLastNDays` | 위 중 `place IS NULL` 건수 |
| `fallbackRatio` | `newOrdersNullPlaceLastNDays / newOrdersLastNDays` |
| `cacheHitTotal` / `cacheMissTotal` | Redis 누적 카운터 (`kakao:cache:hit`, `kakao:cache:miss`) |
| `cacheHitRatio` | `hit / (hit + miss)` |
| `quotaUsageToday` | `kakao:usage:{오늘날짜}` 현재값 |
| `quotaLimitDaily` | 250,000 (환경설정값) |
| `quotaUsageRatio` | `usageToday / limit` |
| `daily[].kakaoCalls` | 일별 호출 수 (Redis `kakao:usage:{date}`) |
| `daily[].cacheHits` / `cacheMisses` | 일별 카운터 (별도 키 `kakao:cache:hit:{date}`, `kakao:cache:miss:{date}` 필요 — 본 작업으로 추가) |
| `daily[].fallbacks` | 일별 fallback 발생 수 (별도 키 `kakao:fallback:{date}`) |

> 일별 카운터는 24h 키만으로 충분치 않으므로 본 작업에서 일자 단위 키를 추가로 INCR하고 30일 TTL 부여.

**비즈니스 로직 요약:**
1. ADMIN 권한 확인. 위반 시 403. (BR-08)
2. `distinctRawPlaceNameCount`, `distinctPlaceIdCount`: GroupOrder 집계 쿼리 (BR-10).
3. 최근 N일 fallback 비율: 시간 범위 조건 쿼리.
4. 캐시·한도 지표: Redis 키 조회 (한 번에 multi-get).
5. `daily` 배열: `days`일 동안 각 날짜별 Redis 키 multi-get 후 정렬.
6. 단일 트랜잭션 readOnly.

**에러 케이스:**
| 상황 | HTTP | code | message |
|------|------|------|---------|
| 미인증 | 401 | `UNAUTHORIZED` | 인증이 필요합니다. |
| 권한 없음 | 403 | `FORBIDDEN` | ADMIN 권한이 필요합니다. |
| days 범위 초과 | 400 | `VALIDATION_ERROR` | days는 1~30 사이여야 합니다. |

**동시성 & 멱등성:**
- 읽기 전용이라 동시성 이슈 없음.
- Redis 카운터 일관성은 INCR 원자성에 의존.

**사이드 이펙트 / 도메인 이벤트:**
- 없음.

**엣지 케이스:**
- [ ] `cacheHitTotal + cacheMissTotal == 0` (배포 직후): `cacheHitRatio = null` 반환.
- [ ] 일별 키가 만료된 과거 날짜: 해당 일자 객체 자체 생략 (배열에 미포함).
- [ ] `distinctRawPlaceNameCount == 0`: `deduplicationRatio = null`.

---

## 3. 도메인 이벤트 & 사이드 이펙트 요약

| API | 발행 이벤트 | 구독 주체 | 처리 내용 |
|-----|-------------|-----------|-----------|
| `GET /places/search` | 없음 | - | Redis 카운터 갱신만 |
| `POST /group-orders` (FOOD) | 없음 | - | Place 신규 저장(별도 트랜잭션), 카운터 갱신 |
| `POST /admin/places/migrate` | 없음 | - | GroupOrder.place 다수 갱신, Place 신규 저장 |
| `GET /admin/places/stats` | 없음 | - | 읽기 전용 |

도메인 이벤트는 발행하지 않음 (ADR-P-03).

---

## 4. API 간 의존 관계

- **`GET /places/search` → `POST /group-orders`** (선택형 UX 흐름)
  - 프론트가 검색 결과 중 하나의 `placeId`를 받아 작성 시 전달.
  - 검색 캐시(`kakao:place:{normalizedKeyword}`)는 작성의 자동 매칭과 키 공유 → 검색 직후 작성은 캐시 히트.
- **`POST /admin/places/migrate` → `GET /admin/places/stats`**
  - 마이그레이션 실행 후 `deduplicationRatio` 측정.
  - 일회성 마이그레이션 완료 후 1주일 운영 데이터로 효과 측정(Phase 7).

---

## 5. 보안 체크리스트

- [x] 모든 쓰기 API 인증 적용 (`/group-orders`, `/admin/places/migrate`)
- [x] ADMIN 전용 endpoint는 권한 검증 (`/admin/places/*` — 기존 SecurityConfig 패턴 따름)
- [x] 검색 endpoint도 인증 필요(외부 API 한도 보호)
- [x] `placeId` 위변조 위험 — TBD-2에서 검증 호출 여부 결정 필요. MVP는 fallback으로 무해화
- [x] 응답에 민감 데이터 없음 (Place는 공개 정보)
- [x] 요청 크기 제한: `keyword` 100자, `rawPlaceName` 100자, `placeId` 30자 validation
- [x] 어드민 endpoint 새 경로 → `SecurityConfig`에 `/admin/places/**` 패턴 추가 필요

---

## 6. 최종 검토

- [x] ambiguous endpoint 없음: 4개 모두 URL과 책임 명확
- [x] 숨겨진 동시성 위험: Place unique 충돌 + 마이그레이션 중복 실행 → 둘 다 처리 명시
- [x] 인가 누락 없음: ADMIN/USER 구분 명시
- [x] 최종 일관성 처리: PlaceResolver는 GroupOrder 트랜잭션 밖에서 호출(ADR-P-05)
- [x] 하위 호환성: 신규 필드 optional (결정 3-A) → 기존 클라 영향 없음

---

## 7. TBD

- [ ] **TBD-1**: 통계 API의 `quotaLimitDaily` 환경설정 키 이름 (예: `kakao.local.daily-quota-limit`)
- [ ] **TBD-2**: `placeId` 직접 입력 시 카카오 측 존재 검증을 항상 수행할지(추가 호출 발생) vs 신뢰하고 fallback만 보장할지
- [ ] **TBD-3**: 마이그레이션 endpoint의 동시 실행 락(Redis 키) TTL 정책 — 비정상 종료 시 락이 남는 문제. 권장 30분 TTL + 운영자 수동 해제 endpoint
- [ ] **TBD-4**: 검색 endpoint의 `size` 상한 — 카카오 page 1 페이지가 최대 15건이므로 그 이상 요구 시 별도 페이징 도입 필요
- [ ] **TBD-5**: 통계의 `daily` 배열에서 일별 카운터를 신규 추가하는 키 네이밍 표준 (`kakao:cache:hit:{date}` vs `kakao:metrics:{date}:cache-hit` 등)
- [ ] **TBD-6**: 검색 응답에 카카오 측 `phone`, `categoryName` 등 부가 정보를 포함할지 — 현재는 정규화 식별에 필요한 필드만

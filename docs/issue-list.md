# Issue List

> 생성일: 2026-06-30
> 총 이슈 수: 1개
> 상태: **GitHub 이슈 생성 완료**

| 순서 | 번호 | 타입 | 제목 | 선행 이슈 | 브랜치 |
|------|------|------|------|-----------|--------|
| 1 | #650 | feat | 공동구매 음식점 명칭 정규화 (Kakao Local API) | 없음 | teach/feat/place-normalization-650 |

---

## 이슈 본문 (gh 생성용)

> 아래 본문을 `gh issue create --title "[feat] 공동구매 음식점 명칭 정규화 (Kakao Local API)" --body "..."` 로 그대로 사용하세요.
> 또는 GitHub 웹에서 새 이슈 생성 시 복붙.

```markdown
## 개요

공동구매(GroupOrder) 작성 시 음식점 명칭이 자유 텍스트로 저장되어 같은 음식점이 표기 차이(예: "BBQ 송도점", "비비큐 송도", "bbq 송도점")로 중복 등록되는 문제를 해결한다.
카카오 로컬 API 키워드 검색으로 정규화된 `placeId`를 부여하고, 별도 `Place` 엔티티(1:N)를 도입해 같은 음식점을 여러 공동구매가 공유한다.
Redis 캐시(24h, NOT_FOUND sentinel)와 일일 호출 한도 가드(250,000)로 무료 한도를 보호하며, 운영 누적 데이터는 일회성 마이그레이션으로 정규화한다.

## 작업 목록

### Phase 1 — 인프라/공통 (KakaoLocalClient)
- [ ] `application.yml` / `application-local.yml`에 `kakao.local.base-url`, `kakao.local.rest-api-key` 추가 (환경변수 `KAKAO_REST_API_KEY`)
- [ ] `domain/place/client/KakaoLocalClient` 작성 (RestClient + 재시도 3회/백오프 + 타임아웃, `AiScheduleExtractClient` 패턴 참고)
- [ ] `domain/place/client/dto/KakaoPlaceSearchResponse`, `KakaoPlaceDocument` (id·place_name·road_address_name·x·y)
- [ ] `KakaoLocalClient.searchFirst(keyword)`: Optional 반환, 5xx/타임아웃 재시도 후 최종 실패 시 예외
- [ ] `KakaoLocalClient.searchTop(keyword, size)`: 검색 API용 (size 1~15)
- [ ] ErrorCode 5종 추가: `KAKAO_API_ERROR`(502), `KAKAO_QUOTA_EXCEEDED`(503), `KEYWORD_TOO_SHORT`(400), `INVALID_PLACE_FOR_TYPE`(400), `PLACE_ID_INVALID`(400)
- [ ] `SecurityConfig`에 `/places/search`(인증), `/admin/places/**`(ADMIN) 권한 추가

### Phase 2 — 도메인 모델 + 스키마
- [ ] Flyway 마이그레이션 `V202607XX__add_place_table.sql`
  - `place` 테이블: id, place_id VARCHAR(30) UNIQUE, place_name, road_address, latitude, longitude, created_at, updated_at
  - `group_order` 컬럼 추가: `place_id BIGINT NULL FK`, `raw_place_name VARCHAR(100)`
- [ ] `domain/place/entity/Place` (BaseTimeEntity, `@NoArgsConstructor(PROTECTED)`, 정적 팩토리 `Place.ofKakao(KakaoPlaceDocument)`)
- [ ] `domain/place/repository/PlaceRepository`: `findByPlaceId`, `existsByPlaceId`
- [ ] `GroupOrder` 엔티티에 `@ManyToOne Place place`, `String rawPlaceName` 추가 (ADR-P-02)
- [ ] `GroupOrder.assignPlace(Place, String rawName)` 도메인 메서드 (INV-GO-N1 검증 포함)

### Phase 3 — 정규화 코어
- [ ] `domain/place/service/CacheKeyNormalizer` (`trim + lowercase + 중복 공백 단일화`)
- [ ] `domain/place/service/PlaceCacheService` (Redis: `kakao:place:{key}`, 24h TTL, NOT_FOUND sentinel)
- [ ] `domain/place/service/QuotaGuard` (`kakao:usage:{yyyy-MM-dd}` INCR, 250,000 도달 시 차단 + Slack 알림 1회/일, `SlackErrorNotifier` 재사용)
- [ ] `domain/place/enums/NormalizationOutcome` (`MATCHED`, `NOT_FOUND`, `FALLBACK_ERROR`, `SKIPPED`, `QUOTA_EXCEEDED`)
- [ ] `domain/place/service/PlaceResolver`
  - 입력 우선순위: `placeId` > `rawPlaceName`
  - 캐시 hit/miss → 카카오 호출 → `Place` 저장/재사용 (저장은 `@Transactional(propagation = REQUIRES_NEW)`, ADR-P-05)
  - 동시 저장 충돌 시 재조회 1회(INV-P-01 복구)
  - 외부 호출은 `@Transactional` **밖**
- [ ] 일별 카운터 키 추가: `kakao:cache:hit:{date}`, `kakao:cache:miss:{date}`, `kakao:fallback:{date}` (30일 TTL)

### Phase 4 — 음식점 검색 API
- [ ] `domain/place/controller/PlaceSearchController` (`GET /places/search`)
- [ ] `domain/place/controller/dto/ResponsePlaceSearchItemDto`
- [ ] keyword 2자 미만 시 빈 results, size 1~15 검증
- [ ] 캐시 미스 시 카카오 호출 + 첫 결과 placeId 캐시 적재 (작성 흐름과 키 공유)

### Phase 5 — 공동구매 작성 확장 (POST /group-orders)
- [ ] `RequestGroupOrderDto`에 `placeId`, `rawPlaceName` 두 optional 필드 추가
- [ ] Validation: `placeId` 최대 30자, `rawPlaceName` 최대 100자, FOOD 외 타입에 전송 시 `INVALID_PLACE_FOR_TYPE` 400
- [ ] `GroupOrderService.createGroupOrder`에 분기:
  - 비-FOOD 또는 둘 다 비어있음 → 정규화 생략
  - FOOD → `PlaceResolver.resolve(...)`를 **트랜잭션 진입 전** 호출 후 결과 주입
- [ ] `ResponseGroupOrderDto`에 `place`, `normalizationOutcome` 포함
- [ ] 하위 호환: 기존 클라(필드 미전송) 정상 동작

### Phase 6 — 어드민 마이그레이션 (POST /admin/places/migrate)
- [ ] `GroupOrderRepository.findUnnormalizedFoodOrders(chunkSize, lastId)` (cursor 페이징, ADR-P-06)
- [ ] `domain/place/service/PlaceMigrationService`
  - 청크 500건, idempotent (`place IS NULL AND groupOrderType=FOOD`만)
  - 일 상한 10,000건, 호출 간 100ms throttle
  - 한도 도달 시 즉시 중단, 다음 날 재개
  - 청크별 트랜잭션 (개별 row 실패 격리)
- [ ] `domain/place/controller/AdminPlaceController` (`POST /admin/places/migrate`)
- [ ] 중복 실행 락: Redis `places:migration:running` (30분 TTL)

### Phase 7 — 어드민 통계 (GET /admin/places/stats)
- [ ] `GroupOrderRepository` 집계 메서드 (QueryDSL, BooleanExpression):
  - `countDistinctRawPlaceName()`, `countDistinctPlaceId()`
  - `countByCreatedAtAfter(since)`, `countByCreatedAtAfterAndPlaceIsNull(since)`
- [ ] `AdminPlaceController`에 `GET /admin/places/stats` 추가
- [ ] `ResponsePlaceStatsDto` (summary + daily 배열, days=7 기본, 1~30)
- [ ] Redis multi-get으로 일별 카운터 조회

### Phase 8 — 테스트
- [ ] `PlaceResolver` 단위 테스트 (Mockito):
  - 캐시 hit → 카카오 API 미호출 검증
  - 캐시 miss + API 성공 → Place 저장 + 캐시 적재
  - 캐시 miss + API NOT_FOUND → sentinel 저장 + `NOT_FOUND` outcome
  - 캐시 miss + API 예외 → `FALLBACK_ERROR` outcome, 빈 Place
  - QuotaGuard 차단 → `QUOTA_EXCEEDED` outcome
- [ ] `GroupOrderService.createGroupOrder` 통합 테스트 (FOOD/비-FOOD, placeId/rawPlaceName 우선순위)
- [ ] 동일 키워드 두 번 등록 시 같은 Place 재사용 검증
- [ ] `PlaceMigrationService` 통합 테스트 (idempotent, 청크 처리)
- [ ] (선택) `KakaoLocalClient` WireMock 5xx/timeout 시나리오

## API 설계

| Method | URL | 권한 | 설명 |
|--------|-----|------|------|
| GET | `/places/search` | 인증 사용자 | 카카오 로컬 키워드 검색, 자동완성용 |
| POST | `/group-orders` | USER | 공동구매 작성 (placeId/rawPlaceName 신규 optional) |
| POST | `/admin/places/migrate` | ADMIN | 누적 데이터 정규화 마이그레이션 |
| GET | `/admin/places/stats` | ADMIN | 정규화 효과 측정 지표 (summary + daily) |

상세 요청/응답/에러 케이스는 `docs/api-spec.md` 참조.

## 비즈니스 규칙 (요약)

- **BR-01**: `GroupOrderType = FOOD` 인 경우에만 정규화 수행
- **BR-02**: 입력 우선순위 `placeId` > `rawPlaceName` > 둘 다 없음 → 정규화 생략
- **BR-03**: API 매칭 결과 — MATCHED / NOT_FOUND (sentinel 캐시) / 5xx 재시도 3회 실패 → fallback (모두 등록 허용)
- **BR-04**: Redis 캐시 키는 `trim + lowercase + 공백 단일화`, TTL 24h
- **BR-05**: 일 250,000 도달 시 호출 차단 + Slack 알림 1회
- **BR-06**: 카카오 API 호출은 `@Transactional` 밖, Place 저장은 `REQUIRES_NEW`
- **BR-07**: 마이그레이션 endpoint는 ADMIN 전용, idempotent, 청크 페이징, 일 10,000건 상한
- **BR-08**: 통계 endpoint ADMIN 전용
- **BR-09**: 검색 endpoint 인증 사용자 호출 가능, keyword 2자 미만 시 빈 결과
- **BR-10**: 통계는 `GROUP BY place_id` (row 병합 금지)

상세 정의는 `docs/requirements.md` §3, `docs/domain-model.md` §3 참조.

## 도메인 불변식

- **INV-P-01**: `placeId`는 시스템 전역 유일 (DB UNIQUE)
- **INV-P-02**: `placeId`, `placeName`은 null/empty 불가
- **INV-P-03**: 좌표 범위 검증 (lat [-90,90], lng [-180,180])
- **INV-GO-N1**: `groupOrderType != FOOD` → `place` 항상 null. FOOD + 정규화 성공 → non-null, 실패 → null
- **INV-GO-N2**: `rawPlaceName`은 원본 그대로 보존 (정규화 결과로 덮어쓰지 않음)
- **INV-GO-N3**: 생성된 GroupOrder의 `place`/`rawPlaceName`은 변경 불가 (마이그레이션 경로만 예외: null → non-null 1회 갱신)

## 엣지 케이스

- [ ] `rawPlaceName="   "` (공백만) → null로 정규화 후 저장
- [ ] 키워드에 특수문자/이모지 → 카카오에 그대로 전달 (sanitize 안 함)
- [ ] 검색 결과 0건 → NOT_FOUND sentinel 캐시 적재, 빈 results 반환
- [ ] `placeId` 동시 신규 저장 race → DB UNIQUE 제약 + 재조회 1회 복구
- [ ] 마이그레이션 도중 서버 재시작 → cursor 페이징으로 자연 재개
- [ ] 마이그레이션 중 한도 도달 → 즉시 중단, 응답에 `quotaExceededAtRow` 표기, 다음 날 재호출 시 자동 재개
- [ ] GroupOrder 저장 트랜잭션 실패 → Place row는 남음(고아, 무해)
- [ ] 통계 `cacheHitTotal + cacheMissTotal == 0` (배포 직후) → `cacheHitRatio = null`
- [ ] 통계 `distinctRawPlaceNameCount == 0` → `deduplicationRatio = null`
- [ ] 일별 카운터 키 만료된 과거 날짜 → 해당 일자 객체 생략

## 에러 케이스

| 상황 | HTTP | code |
|------|------|------|
| 미인증 | 401 | `UNAUTHORIZED` |
| ADMIN 권한 없이 어드민 endpoint 호출 | 403 | `FORBIDDEN` |
| FOOD 외 타입에 place 필드 전송 | 400 | `INVALID_PLACE_FOR_TYPE` |
| 카카오 API 최종 실패 (검색 endpoint) | 502 | `KAKAO_API_ERROR` |
| 일일 한도 도달 (검색 endpoint) | 503 | `KAKAO_QUOTA_EXCEEDED` |
| size 범위 초과 (검색 endpoint) | 400 | `VALIDATION_ERROR` |
| 마이그레이션 중복 실행 | 409 | `CONFLICT` |
| placeId 카카오 측 존재 검증 실패 (선택 정책) | 400 | `PLACE_ID_INVALID` |

## Antipattern 체크리스트 (구현 시 자가 검토)

- [ ] `@Builder` 직접 노출 금지 → `Place.ofKakao(...)` 정적 팩토리 사용
- [ ] `@Setter` 금지 → `Place.update(...)` (MVP에서는 비활성, ADR-P-04)
- [ ] `@AllArgsConstructor` 금지 → `@NoArgsConstructor(PROTECTED)` + 정적 팩토리
- [ ] 외부 API 호출은 `@Transactional` 밖
- [ ] Place 저장은 `@Transactional(propagation = REQUIRES_NEW)`
- [ ] `RuntimeException` 금지 → `CustomException` + `ErrorCode.KAKAO_API_ERROR`
- [ ] Controller 비즈니스 로직 금지 → 모두 `PlaceResolver` / `PlaceMigrationService`로 분리
- [ ] 통계 집계는 QueryDSL `BooleanExpression` 패턴 (BooleanBuilder 금지)
- [ ] 엔티티 직접 응답 금지 → `ResponsePlaceDto` / `ResponsePlaceStatsDto`
- [ ] Request DTO `@Valid` 필수
- [ ] FCM/외부 발송 등 비동기 추가 시 `@Async` + `@Transactional` 동시 적용

## 측정 지표 (Phase 7 — 1주일 운영 후)

| 지표 | 측정 방법 |
|------|-----------|
| 중복 등록 N→M | 마이그레이션 전후 `COUNT(DISTINCT raw_place_name)` vs `COUNT(DISTINCT place_id)` |
| 신규 등록 중복률 | 도입 후 1주일 신규 row 중 같은 place_id 비율 |
| 카카오 API 호출 수 | `kakao:usage:{date}` 일별 합계 |
| 캐시 적중률 | `hit / (hit + miss) × 100` |
| 무료 한도 사용률 | 일 호출 수 / 300,000 × 100 |
| fallback 발생률 | `place_id IS NULL` 비율 / 전체 신규 |

## 위험·미결 사항

1. 카카오 검색 정확도(오매칭): 마이그레이션 중 샘플링 수동 검증 필요
2. 마이그레이션 + 운영 트래픽 동시 호출로 한도 소진 가능 → 야간 시간대 한정
3. 사용자 직접 선택 UI(프론트 협의): 백엔드는 placeId 받아 검증만
4. `placeId` 위변조 시 카카오 측 존재 검증 호출 여부 (TBD-2) — MVP는 신뢰 + fallback

## 참고

- 선행 이슈: 없음
- 관련 문서:
  - `docs/requirements.md` (요구사항 명세서)
  - `docs/domain-model.md` (도메인 모델, 6개 ADR 포함)
  - `docs/api-spec.md` (API 명세서, 4개 엔드포인트)
- 참고 패턴: `AiScheduleExtractClient` (RestClient + 재시도 + 타임아웃 + @PostConstruct)
- 참고 antipatterns: `.claude/rules/antipatterns.md`, `.claude/rules/antipatterns-jpa.md`
```

---

## 생성 결과

- **GitHub 이슈**: [#650 [feat] 공동구매 음식점 명칭 정규화 (Kakao Local API)](https://github.com/Teach-D/appcenter-16.5-dormitory-project/issues/650)
- **브랜치**: `teach/feat/place-normalization-650` (dev 기반, 로컬 checkout 완료)

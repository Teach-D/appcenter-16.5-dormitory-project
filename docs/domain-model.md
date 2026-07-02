# 도메인 모델 — 공동구매 음식점 명칭 정규화

> 기반 요구사항: `docs/requirements.md`

---

## 1. 유비쿼터스 언어 (Ubiquitous Language)

| 용어 | 정의 |
|------|------|
| **Place** | 카카오 로컬에서 정규화된 음식점 식별 단위. `placeId`(카카오의 외부 식별자)로 유일성을 가진다. |
| **placeId** | 카카오 로컬이 부여한 장소 고유 ID (문자열). 시스템 외부에서 부여되는 자연키. |
| **rawPlaceName** | 사용자가 GroupOrder 작성 시 입력한 원본 음식점 이름. 정규화 성공/실패와 무관하게 보존. |
| **PlaceResolver** | rawPlaceName(또는 placeId)을 받아 Place 후보를 찾고 GroupOrder에 연결할 Place를 결정하는 도메인 서비스. |
| **NormalizationOutcome** | 정규화 결과 분류. `MATCHED`, `NOT_FOUND`, `FALLBACK_ERROR`, `SKIPPED`, `QUOTA_EXCEEDED` 5종. |
| **Quota Guard** | 외부 API의 일일 호출 한도(250,000건)를 보호하는 정책 가드. |
| **Normalized Keyword** | 키워드를 `trim() + toLowerCase() + 중복 공백 단일화`한 결과. 캐시 키로 사용. |
| **NOT_FOUND Sentinel** | 카카오 검색 0건 응답을 캐시에 음수로 기록하는 표시값. 반복 호출 방지. |
| **Fallback Registration** | 정규화 실패 시 Place 연결 없이 GroupOrder만 저장하는 정책. |
| **Migration Run** | 운영 누적 데이터를 일괄로 정규화하는 1회성 작업 단위. |

---

## 2. 바운디드 컨텍스트

### Place Context (신규)
- 외부 장소 정보(카카오)의 정규화 책임을 갖는 독립 컨텍스트.
- 외부 시스템(카카오 로컬)의 변경, 캐시 정책, 한도 가드, 마이그레이션을 격리.
- Anti-Corruption Layer 역할: 카카오의 응답 모델을 내부 `Place` 모델로 번역.

### GroupOrder Context (기존)
- 공동구매 게시글의 라이프사이클(작성·참여·마감)을 관리.
- Place Context의 결과를 **읽기 전용으로 참조**(쓰기 책임 없음).

### 컨텍스트 간 관계
```
GroupOrder Context  ──── (Customer/Supplier) ────▶  Place Context
                   uses Place(read-only reference)
```

- Place Context는 Upstream(Supplier), GroupOrder Context는 Downstream(Customer).
- GroupOrder는 Place의 식별자(`Place` 참조)를 보관하지만 Place 자체를 생성·수정·삭제하지 않는다.

---

## 3. 애그리거트

### Aggregate: Place

#### 책임
같은 카카오 장소(`placeId`)는 시스템 내 단 하나의 `Place` row로만 존재함을 보장한다.

#### 애그리거트 루트
`Place`

#### 엔티티 & 값 객체

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | Place | id, placeId, placeName, roadAddress, latitude, longitude, createdAt, updatedAt | 카카오에서 매칭된 정규화된 장소 |
| VO | Coordinate | latitude, longitude | Place 내부 좌표 값 (선택적으로 VO로 캡슐화 가능) |

#### 비즈니스 불변식 (Invariants)
- **INV-P-01**: `placeId`는 시스템 전역에서 유일하다(Unique).
  - 위반 시: 도메인 예외(`PLACE_ALREADY_EXISTS`) — 충돌 시 재조회 1회로 복구
- **INV-P-02**: `placeId`, `placeName`은 null/empty 불가.
  - 위반 시: 생성 실패 (잘못된 외부 응답으로 간주)
- **INV-P-03**: `latitude`는 [-90, 90], `longitude`는 [-180, 180] 범위 내.
  - 위반 시: 생성 실패
- **INV-P-04**: Place는 한 번 생성되면 식별 속성(`placeId`)은 변경 불가. `placeName`·주소·좌표는 외부 API 갱신 정책에 따라 업데이트 가능(MVP에서는 불변으로 시작, ADR-P-04).
  - 위반 시: 도메인 예외

#### 라이프사이클 & 상태 머신
상태값 없음. 생성-조회 위주의 참조 데이터 성격.

```
(존재하지 않음) -[ofKakao(document)]→ ACTIVE
ACTIVE -[update(name, address, coord)]→ ACTIVE   (MVP에서는 비활성, ADR-P-04 참고)
```

#### 트랜잭션 경계
- 단독 트랜잭션으로 완결.
- GroupOrder 생성과는 **별개 트랜잭션**으로 저장. GroupOrder 트랜잭션이 롤백되어도 다른 공동구매가 같은 Place를 재사용할 수 있어야 하기 때문.

#### 동시성 고려사항
- 같은 `placeId`로 동시에 신규 저장 요청 가능성 있음 → DB unique 제약으로 1차 보호.
- 충돌 발생 시 도메인 서비스 레벨에서 **재조회 1회 → 기존 Place 반환** (낙관적 복구).

#### 도메인 이벤트
**없음** (ADR-P-03 결정: Redis 카운터로만 통계 집계).

---

### Aggregate: GroupOrder (확장)

#### 책임
공동구매의 라이프사이클(작성·참여·마감)과, 해당 공동구매가 어떤 음식점(Place)에 대한 것인지의 연결을 관리.

#### 애그리거트 루트
`GroupOrder` (기존)

#### 엔티티 & 값 객체 — 본 작업의 변경분만 명시

| 구분 | 이름 | 핵심 속성 | 설명 |
|------|------|-----------|------|
| Entity (Root) | GroupOrder | (기존 속성) + **place(참조)**, **rawPlaceName** | 음식점 정규화 정보 추가 |

기존 속성(title, groupOrderType, price, link, openChatLink, deadline, description 등)은 변경 없음.

#### 비즈니스 불변식 (추가분)
- **INV-GO-N1**: `place`는 nullable. 단, 다음 조건들과 일관성 유지.
  - `groupOrderType != FOOD` → `place`는 항상 null.
  - `groupOrderType == FOOD` → `place`는 정규화 성공 시 non-null, 실패 시 null (Fallback Registration 허용).
  - 위반 시: 도메인 예외(`INVALID_PLACE_FOR_TYPE`)
- **INV-GO-N2**: `rawPlaceName`은 사용자가 입력한 경우 원본 그대로 보존.
  - 입력값이 비어 있으면 null 저장 허용.
  - 어떤 정규화 결과에도 임의로 덮어쓰지 않는다.
- **INV-GO-N3**: 한 번 생성된 GroupOrder의 `place`, `rawPlaceName`은 본 작업 범위에서 **변경하지 않는다** (재정규화 미지원, requirements Out of Scope 항목).
  - 단, 마이그레이션 경로에서만 `place=null → non-null` 1회 갱신 허용 (멱등).

#### 라이프사이클 & 상태 머신
기존 GroupOrder 상태 머신과 동일 (본 작업으로 변경 없음).

#### 트랜잭션 경계
- GroupOrder 생성 트랜잭션 내부에서 Place 참조를 받아 연결만 한다.
- Place 생성/조회는 **별도 트랜잭션**(Place Aggregate에서 처리)으로 분리하여 외부 호출과 결합도 차단.

#### 동시성 고려사항
- 동일 사용자가 같은 음식점에 동시에 여러 공동구매 작성 시도 → 기존 GroupOrder 비즈니스 규칙으로 처리 (본 작업 범위 외).
- 마이그레이션 도중 신규 GroupOrder가 같은 Place를 동시에 참조 → Place Aggregate의 unique 제약으로 안전.

#### 도메인 이벤트
**없음** (ADR-P-03).

---

## 4. 애그리거트 관계도

```
Place (Aggregate Root)
   │
   │ 1
   │
   │ N
   ▼
GroupOrder (Aggregate Root)
   - place (객체 참조, nullable)
   - rawPlaceName (원본 입력)
```

- Place 1 ↔ N GroupOrder
- 같은 Place는 여러 GroupOrder에 의해 참조될 수 있다(중복 제거의 핵심).
- GroupOrder는 Place를 생성·수정·삭제하지 않는다. 단, **연결 갱신은 마이그레이션 경로에서만 허용**(INV-GO-N3).

---

## 5. 도메인 이벤트

없음. (ADR-P-03 — Redis 카운터로 통계 직접 집계)

향후 확장 시점에 다음 이벤트 후보가 있으나 본 작업 범위 밖:
- `PlaceMatched`
- `PlaceFallbackOccurred`
- `PlaceQuotaExceeded`

---

## 6. 도메인 서비스

### PlaceResolver
- **책임**: GroupOrder 작성/마이그레이션 시점에 `(placeId | rawPlaceName)` 입력을 받아 `(Place | null, NormalizationOutcome)` 결과를 반환.
- **관여 애그리거트**: Place (생성/조회)
- **관여 외부 컴포넌트**: 외부 장소 검색 시스템(카카오 로컬), 캐시 저장소, Quota Guard
- **로직 요약**
  1. 입력 우선순위: `placeId` > `rawPlaceName`
  2. `placeId` 제공 시: Place 저장소 조회 → 없으면 외부 검색으로 메타 채워 신규 저장
  3. `rawPlaceName` 제공 시: 키워드 정규화 → 캐시 조회 → 미스면 외부 검색 1회
  4. 결과 분류 → `NormalizationOutcome` 값 함께 반환
- **트랜잭션 전략**: **단일 트랜잭션 사용 금지**. 외부 호출은 트랜잭션 밖, Place 저장만 별도 트랜잭션. GroupOrder 트랜잭션과 분리(최종 일관성).
- **호출 위치 제약**: GroupOrder Application Service가 트랜잭션 진입 **전에** PlaceResolver를 호출해 결과를 받은 뒤, GroupOrder 트랜잭션 내부에서 결과만 연결한다.

### PlaceMigrationService
- **책임**: `place IS NULL AND groupOrderType = FOOD` 조건의 기존 GroupOrder를 청크 단위(500건)로 정규화.
- **관여 애그리거트**: GroupOrder (place 연결 갱신), Place (생성/조회)
- **로직 요약**
  - idempotent 처리: 이미 place 연결된 row 자동 스킵
  - 일 처리 상한 10,000건, 호출 간 100ms 간격
  - 일일 호출 한도 도달 시 즉시 중단, 다음 날 재개
- **트랜잭션 전략**: 청크별 트랜잭션. 한 청크 실패가 전체 마이그레이션을 중단시키지 않음(개별 row 단위 오류 격리).

### QuotaGuard
- **책임**: 일별 외부 API 호출 카운트를 관측하고, 임계(250,000) 도달 시 추가 호출을 차단한다. 차단 시 NOT_FOUND와 동일한 fallback 경로로 흐르도록 신호를 반환.
- **관여 애그리거트**: 없음 (정책 객체)
- **부수 효과**: 임계 도달 시 운영자 알림 채널로 1회 통지.

### CacheKeyNormalizer (값 객체급 유틸)
- **책임**: `trim() + toLowerCase() + 중복 공백 단일화` 규칙으로 키워드를 캐시 키 형태로 변환.
- 도메인 서비스라기보다는 PlaceResolver에 부속된 값 객체. 별도 클래스로 분리해 단위 테스트 용이성 확보.

---

## 7. 크로스-애그리거트 상호작용

| 상황 | 관여 애그리거트 | 일관성 전략 | 이유 |
|------|----------------|-------------|------|
| 공동구매 작성 (FOOD, 자유 텍스트) | Place → GroupOrder | 최종 일관성 (별도 트랜잭션) | 외부 API 호출을 GroupOrder 트랜잭션에서 분리해야 함 |
| 공동구매 작성 (FOOD, 선택형 placeId 전달) | Place → GroupOrder | 최종 일관성 (별도 트랜잭션) | 위와 동일. Place 조회/생성을 GroupOrder 트랜잭션 밖으로 |
| 공동구매 작성 (비-FOOD) | GroupOrder 단독 | 단일 트랜잭션 | Place 관여 없음 |
| 마이그레이션 (place=null GroupOrder) | Place → GroupOrder | 청크 단위 다중 트랜잭션 | 진행률·재개·실패 격리 |
| 통계 조회 | GroupOrder, Place | 읽기 전용 / 캐시 카운터 | 집계 쿼리는 GROUP BY place_id |
| 일일 한도 도달 | QuotaGuard → PlaceResolver | 즉시 일관성 | 신규 호출을 차단해야 함 |

---

## 8. 레포지토리 인터페이스

### PlaceRepository
```
findByPlaceId(placeId: String): Optional<Place>
existsByPlaceId(placeId: String): boolean
```

### GroupOrderRepository (추가 메서드만 명시)
```
findUnnormalizedFoodOrders(pageSize: int, lastId: Long): List<GroupOrder>
  // place IS NULL AND groupOrderType = FOOD AND id > lastId ORDER BY id ASC LIMIT ?
  // 마이그레이션 청크 페이징 (cursor 방식)

countDistinctRawPlaceName(): long
countDistinctPlaceId(): long
  // Phase 7 BEFORE/AFTER 비교용 (마이그레이션 전후 중복 제거 효과 측정)

countByCreatedAtAfterAndPlaceIsNull(since: LocalDateTime): long
countByCreatedAtAfter(since: LocalDateTime): long
  // 신규 등록 중 fallback 비율 측정용
```

---

## 9. 패키지 구조 제안

(ADR-P-01에 따라 Place는 독립 도메인 패키지 분리)

```
com.example.appcenter_project
└── domain/
    ├── place/                                       ← 신규
    │   ├── entity/
    │   │   └── Place
    │   ├── repository/
    │   │   └── PlaceRepository
    │   ├── service/
    │   │   ├── PlaceResolver               (도메인 서비스)
    │   │   ├── PlaceCacheService           (캐시 어댑터, 인프라 의존)
    │   │   ├── PlaceMigrationService       (도메인 서비스)
    │   │   └── QuotaGuard                  (정책 객체)
    │   ├── client/
    │   │   ├── KakaoLocalClient            (외부 어댑터)
    │   │   └── dto/
    │   │       ├── KakaoPlaceSearchResponse
    │   │       └── KakaoPlaceDocument
    │   ├── controller/
    │   │   ├── AdminPlaceController        (마이그레이션 + 통계, ADMIN)
    │   │   ├── PlaceSearchController       (검색, 인증 사용자)
    │   │   └── dto/
    │   │       ├── ResponsePlaceDto
    │   │       └── ResponsePlaceStatsDto
    │   └── enums/
    │       └── NormalizationOutcome
    │
    └── groupOrder/                                  ← 기존 (확장)
        └── entity/
            └── GroupOrder  (place FK + rawPlaceName 추가)
```

---

## 10. 설계 결정 사항 (ADR)

### ADR-P-01: Place를 독립 도메인 패키지(`domain/place`)에 배치
- **결정**: Place 엔티티/레포지토리/서비스/컨트롤러를 `domain/place` 신규 패키지로 분리한다.
- **이유**: 처음부터 바운디드 컨텍스트를 분리해 향후 다른 도메인(모임 장소, 공구 픽업 장소 등)에서 재사용 가능. Place는 외부 시스템(카카오) 의존이 강해 GroupOrder와 결합 시 GroupOrder 패키지 응집도가 무너짐.
- **trade-off**: 초기 진입 비용 약간 증가(레포지토리·서비스·컨트롤러 클래스 수 증가). 단, 현재 단계에서 분리하지 않으면 추후 이동 시 import 경로 변경, FK 마이그레이션 등 비용이 더 커진다.

### ADR-P-02: GroupOrder → Place 참조는 JPA 객체 참조(`@ManyToOne`)로 유지
- **결정**: 엄격한 DDD 원칙(다른 애그리거트는 ID 참조)을 따르지 않고, 프로젝트의 기존 스타일과 일관성을 위해 `@ManyToOne Place place` 객체 참조를 채택.
- **이유**: 기존 코드베이스 전체가 객체 참조를 표준으로 사용하고 있어, 본 작업만 ID 참조를 도입하면 다른 개발자에게 혼란을 준다. 트랜잭션 분리는 서비스 계층에서 충분히 보장 가능.
- **trade-off**: 애그리거트 경계가 코드 레벨에서 흐려질 수 있음 → 도메인 서비스 호출 규약(`PlaceResolver`를 거치지 않은 Place 직접 조회/저장 금지)으로 보완.

### ADR-P-03: 도메인 이벤트 미발행, Redis 카운터로 통계 집계
- **결정**: PlaceMatched 등 도메인 이벤트를 발행하지 않는다. 호출 수·캐시 적중률·fallback 발생률은 PlaceResolver가 Redis 카운터를 직접 갱신.
- **이유**: 현재 요구사항(이력서용 측정 지표)은 단순 카운팅으로 충분. 이벤트 인프라 도입은 과설계.
- **trade-off**: 추후 통계 적재처가 다양해지면(예: 분석 DB, 외부 BI 도구) 도메인 이벤트로 전환 필요. 그 시점에 PlaceResolver의 카운터 갱신 지점을 이벤트 발행으로 치환하면 됨(작은 비용).

### ADR-P-04: Place 메타데이터(placeName·주소·좌표)는 MVP에서 불변
- **결정**: 한 번 생성된 Place의 메타데이터를 외부 갱신 정책으로 덮어쓰지 않는다.
- **이유**: 카카오의 가게명 변경 빈도는 낮고, 갱신 정책 도입 시 동시성·캐시 무효화 복잡도 증가.
- **trade-off**: 카카오 측 정보 변경(상호명 수정, 이전) 시 우리 DB는 stale 상태가 됨 → TBD로 남기고, 운영 중 문제 빈도를 보고 갱신 정책 도입 여부 재검토.

### ADR-P-05: PlaceResolver 호출은 GroupOrder 트랜잭션 진입 전에 수행
- **결정**: GroupOrder Application Service가 PlaceResolver를 먼저 호출해 `(Place | null)`을 받은 뒤, 별도의 GroupOrder 저장 트랜잭션을 시작한다.
- **이유**: 외부 API 호출을 트랜잭션 안에 두면 트랜잭션 점유 시간이 길어지고, 실패 시 롤백 비용·재시도 복잡도가 커진다(antipatterns.md 명시).
- **trade-off**: GroupOrder 저장이 실패해도 Place row는 남는다(고아 Place 가능). 단, Place는 참조 데이터 성격이라 고아여도 다음 사용 시 재이용되므로 무해.

### ADR-P-06: 마이그레이션은 cursor 기반(`id > lastId`) 페이징
- **결정**: offset 페이징 대신 `id` 컬럼 기준 cursor 페이징으로 청크 페이지를 가져온다.
- **이유**: 대량 row 처리 시 offset은 느려지고, 중간에 row 추가/삭제 시 누락·중복 위험. cursor는 안정적이고 빠름.
- **trade-off**: id 정렬 외 다른 정렬 기준 사용 불가(본 작업에서는 무관).

---

## 11. 아키텍처 위험 요소

- **외부 API 의존**: 카카오 로컬 API 장애 시 모든 FOOD 타입 공동구매 작성이 fallback 경로로 흐름 → fallback 비율 모니터링 필수. 임계치(예: 5%) 초과 시 알림.
- **캐시 일관성**: Redis 장애 시 카카오 직접 호출량 폭증 → 무료 한도 빠르게 소진 가능. 가드는 Redis에 의존하므로 같이 다운되면 무력화 → 향후 별도 카운터 인프라 검토.
- **placeId 위변조**: 클라이언트가 임의의 placeId를 보낼 경우 잘못된 Place에 연결될 위험 → `placeId` 입력 시 PlaceResolver가 카카오 측 존재 검증 1회 수행 권장(BR-03 보강).
- **마이그레이션 정합성**: 같은 음식점이 카카오 측 상호명 변경으로 두 개의 `placeId`로 분리될 가능성 → 운영 중 통계로 식별, 수동 병합 절차 필요 (현재 범위 외).
- **검색 정확도**: 첫 결과만 채택하는 정책은 일부 케이스에서 오매칭 → 정확도 측정 샘플링 필요(requirements TBD).

---

## 12. TBD
- [ ] `placeId` 직접 입력 시 카카오 측 존재 검증 호출 여부 (위변조 vs 호출 수 trade-off)
- [ ] Place 메타데이터 갱신 정책 도입 시점·전략
- [ ] 카카오 측 placeId 분리/통합 발생 시 운영 절차 (수동 병합 도구 필요?)
- [ ] 검색 정확도 모니터링 방안 (샘플링 수동 검증 vs 사용자 신고 기반)
- [ ] Redis 장애 시 한도 가드 대체 메커니즘

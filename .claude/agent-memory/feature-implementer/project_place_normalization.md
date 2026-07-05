---
name: place-normalization-650
description: #650 공동구매 음식점 명칭 정규화 구현 완료 — Place 도메인, KakaoLocalClient, PlaceResolver, 마이그레이션, 통계 API
metadata:
  type: project
---

## 구현 완료 (2026-06-30, 이슈 #650)

**브랜치**: `teach/feat/place-normalization-650`

### 추가된 ErrorCode (24001~24005)
- `KAKAO_API_ERROR` (BAD_GATEWAY, 24001)
- `KAKAO_QUOTA_EXCEEDED` (SERVICE_UNAVAILABLE, 24002)
- `KEYWORD_TOO_SHORT` (BAD_REQUEST, 24003)
- `INVALID_PLACE_FOR_TYPE` (BAD_REQUEST, 24004)
- `PLACE_ID_INVALID` (BAD_REQUEST, 24005)

### 주요 발견사항
- `GroupOrderType.FOOD`가 원래 enum에 없었음 → 직접 추가
  - 기존 값: ALL, DELIVERY, GROCERY, LIFE_ITEM, ETC
  - 추가: FOOD("음식점")
- `AiScheduleExtractClient` 패턴(RestClient + @PostConstruct init) 참조하여 KakaoLocalClient 작성
- `StringRedisTemplate`은 Spring Boot auto-configuration으로 자동 등록됨 (별도 빈 선언 불필요)
- PlaceResolver에서 `@Transactional(propagation = REQUIRES_NEW)`은 saveInNewTransaction 메서드에만 적용

### 생성된 파일 (domain/place/)
- entity/Place.java
- repository/PlaceRepository.java
- client/KakaoLocalClient.java
- client/dto/KakaoPlaceDocument.java
- client/dto/KakaoPlaceSearchResponse.java
- service/CacheKeyNormalizer.java
- service/PlaceCacheService.java (StringRedisTemplate, 24h TTL, sentinel)
- service/QuotaGuard.java (250,000 daily limit, Slack alert 1회/일)
- service/PlaceResolver.java (캐시→쿼터→카카오 플로우)
- service/PlaceResolveResult.java
- service/PlaceMigrationService.java (청크 500, Redis 락, cursor 페이징)
- service/PlaceSearchService.java
- service/PlaceStatsService.java
- enums/NormalizationOutcome.java
- controller/PlaceSearchController.java (GET /places/search)
- controller/AdminPlaceController.java (POST /admin/places/migrate, GET /admin/places/stats)
- controller/dto/ResponsePlaceSearchItemDto.java
- controller/dto/ResponsePlaceStatsDto.java
- controller/dto/ResponseMigrationResultDto.java

### 수정된 기존 파일
- domain/groupOrder/entity/GroupOrder.java (place FK, rawPlaceName 추가, assignPlace 메서드)
- domain/groupOrder/enums/GroupOrderType.java (FOOD 추가)
- domain/groupOrder/dto/request/RequestGroupOrderDto.java (placeId, rawPlaceName 추가)
- domain/groupOrder/repository/GroupOrderQuerydslRepository.java (5개 메서드 추가)
- domain/groupOrder/repository/GroupOrderRepositoryImpl.java (5개 메서드 구현)
- domain/groupOrder/service/GroupOrderService.java (PlaceResolver 주입, FOOD 분기)
- global/exception/ErrorCode.java (24001~24005 추가)
- global/config/SecurityConfig.java (/places/search 인증, /admin/places/** ADMIN)
- resources/application.yml (kakao.local.* 설정 추가)

### Redis 키 패턴
- `kakao:place:{normalizedKey}` — 장소 캐시 (24h TTL)
- `kakao:usage:{yyyy-MM-dd}` — 일일 호출 카운터
- `kakao:cache:hit:{date}`, `kakao:cache:miss:{date}` — 일별 캐시 카운터 (30일 TTL)
- `kakao:fallback:{date}` — 일별 fallback 카운터 (30일 TTL)
- `kakao:cache:hit`, `kakao:cache:miss` — 누적 카운터
- `places:migration:running` — 마이그레이션 중복 실행 방지 락 (30분 TTL)

**Why:** #650 공동구매 음식점 명칭 정규화 — 동일 음식점 중복 등록 문제 해결
**How to apply:** Place 도메인 관련 작업 시 위 파일 구조 참조

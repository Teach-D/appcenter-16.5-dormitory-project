---
name: 음식점 명칭 정규화 TDD
description: "#650 공동구매 음식점 명칭 정규화 (Kakao Local API) 40개 테스트 케이스, BR 커버리지"
type: project
---

## 브랜치
teach/feat/place-normalization-650

## 테스트 파일 위치
- `src/test/java/.../domain/place/fixture/PlaceNormalizationFixture.java`
- `src/test/java/.../domain/place/service/PlaceResolverServiceTest.java` (20개)
- `src/test/java/.../domain/place/service/PlaceNormalizationGroupOrderServiceTest.java` (8개)
- `src/test/java/.../domain/place/service/PlaceMigrationServiceTest.java` (4개)
- `src/test/java/.../domain/place/controller/PlaceSearchControllerTest.java` (8개)

## BR 커버리지 (총 40개)

### PlaceResolverServiceTest (20개)
- BR-01×3: FOOD 외 타입 SKIPPED, 입력 둘 다 null SKIPPED, rawPlaceName 공백 SKIPPED
- BR-02×3: placeId DB 존재 MATCHED, placeId DB 없음→카카오→저장 MATCHED, rawPlaceName 캐시미스→카카오 MATCHED
- BR-03×5: 기존 Place 재사용 MATCHED, 카카오 0건 NOT_FOUND+sentinel, sentinel 캐시hit NOT_FOUND, 카카오 예외 FALLBACK_ERROR, 관용정책(예외 미전파)
- BR-04×4: 캐시hit 시 카카오 미호출, 캐시키 정규화 검증, 캐시miss 카운터 증가, 캐시hit 카운터 증가
- BR-05×2: 한도도달 QUOTA_EXCEEDED, 캐시hit 시 QuotaGuard 미호출
- BR-06×2: Place REQUIRES_NEW 저장, UNIQUE 충돌 시 재조회 복구
- 추가×1: 캐시put 확인

### PlaceNormalizationGroupOrderServiceTest (8개)
- BR-01: FOOD+rawPlaceName 정상 저장
- BR-01: 비FOOD PlaceResolver 미호출
- BR-01: FOOD 입력 없음 PlaceResolver 미호출
- INV-GO-N1: 비FOOD에 placeId 전송 INVALID_PLACE_FOR_TYPE 예외
- INV-GO-N2: rawPlaceName 원본 보존
- BR-02(a): placeId 우선순위 검증
- BR-03 관용: FALLBACK_ERROR여도 GroupOrder 저장 성공
- ADR-P-05: PlaceResolver 호출이 GroupOrder save보다 먼저

### PlaceMigrationServiceTest (4개)
- BR-07 idempotent: place=null AND FOOD 조건만 처리
- BR-07 상한: maxRows 도달 시 즉시 종료
- BR-07 한도도달: QuotaGuard 차단 시 조기종료 + quotaExceededAtRow non-null
- BR-07 격리: 개별 row 오류가 다음 row에 영향 없음

### PlaceSearchControllerTest (8개)
- BR-09: keyword 정상 200 반환
- BR-09: keyword 1자 빈 results 200
- BR-09: keyword 2자 경계 정상
- 에러: keyword 누락 400
- 에러: size 범위 초과 400
- 에러: 미인증 401
- 에러: 카카오 API 실패 502 KAKAO_API_ERROR
- 에러: 한도 도달 503 KAKAO_QUOTA_EXCEEDED

## 신규 ErrorCode (구현 에이전트가 ErrorCode.java에 추가 필요)
- KAKAO_API_ERROR(BAD_GATEWAY, 24001, ...)
- KAKAO_QUOTA_EXCEEDED(SERVICE_UNAVAILABLE, 24002, ...)
- KEYWORD_TOO_SHORT(BAD_REQUEST, 24003, ...) — 비즈니스 정책상 실제 노출 안 됨
- INVALID_PLACE_FOR_TYPE(BAD_REQUEST, 24004, ...)
- PLACE_ID_INVALID(BAD_REQUEST, 24005, ...)

## 신규 도메인 클래스 (구현 에이전트 생성 필요)
- Place (entity, `Place.ofKakao(KakaoPlaceDocument)` 정적 팩토리)
- NormalizationOutcome (enum: MATCHED, NOT_FOUND, FALLBACK_ERROR, SKIPPED, QUOTA_EXCEEDED)
- PlaceResolveResult (placeId, place, outcome — 도메인 결과 VO)
- PlaceResolver (서비스, @Transactional 없음, Place 저장만 REQUIRES_NEW)
- PlaceCacheService (Redis 어댑터)
- QuotaGuard (정책 객체)
- PlaceMigrationService
- KakaoLocalClient (RestClient + 재시도 3회/지수백오프)
- KakaoPlaceDocument, KakaoPlaceSearchResponse
- PlaceRepository (findByPlaceId, existsByPlaceId)
- PlaceSearchController (GET /places/search)
- AdminPlaceController (POST /admin/places/migrate, GET /admin/places/stats)
- ResponsePlaceSearchItemDto, ResponseMigrationResultDto
- GroupOrder 엔티티에 place(@ManyToOne), rawPlaceName 추가
- RequestGroupOrderDto에 placeId, rawPlaceName 추가
- GroupOrderRepository에 findUnnormalizedFoodOrders 추가

## 패턴 노트
- 구현 클래스 전무 → 모든 Mock/@InjectMocks 블록 주석 처리 + placeholder assertThat(true).isTrue() 유지
- PlaceSearchControllerTest: @WebMvcTest 어노테이션도 주석 처리 (컨트롤러 클래스 없음)
- 구현 후 각 파일 주석 해제 → ./gradlew compileJava → Red→Green 검증

**왜:** 도메인 전체 미구현 상태에서 40개 케이스를 컴파일 오류 없이 작성
**적용 방법:** 구현 에이전트가 위 클래스 생성 후 주석 해제 → Red→Green 검증

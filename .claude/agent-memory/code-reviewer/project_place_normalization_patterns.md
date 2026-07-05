---
name: place-normalization-patterns
description: place 도메인 리뷰(#650 카카오 Local API 연동)에서 발견된 반복 안티패턴 및 설계 결정
metadata:
  type: project
---

## 발견된 이슈

### 외부 API 호출이 @Transactional 범위 안에 포함됨 (Critical)
- `GroupOrderService`가 클래스 레벨 `@Transactional`이고, `saveGroupOrder`에서 `placeResolver.resolve()`(카카오 API 최대 1,500ms)를 트랜잭션 범위 안에서 호출
- antipatterns-jpa.md "불필요하게 넓은 @Transactional 범위 금지" 직접 위반
- 수정 패턴: 외부 API 호출 → 비트랜잭션 메서드, DB 저장 → @Transactional 메서드로 분리

### @Transactional 자기호출(self-invocation) 문제 (Critical)
- `PlaceMigrationService.migrate()`(비트랜잭션)가 같은 클래스의 `updateGroupOrderPlace()`(@Transactional)를 직접 호출
- AOP 프록시가 자기호출을 가로채지 못해 @Transactional 미적용 → 변경 감지 동작 안 함 → 무음 데이터 유실
- `PlaceSaveService` 분리 패턴(별도 Spring 빈으로 추출)을 같은 파일 내에서 일관성 없이 적용
- 수정: `GroupOrderPlaceAssignService` 별도 빈으로 추출

### GroupOrder 엔티티 @Builder + @NoArgsConstructor(AccessLevel 없음) (Critical)
- `@NoArgsConstructor`에 `access = AccessLevel.PROTECTED` 누락 → public 기본 생성자 노출
- 엔티티에 `@Builder` 직접 사용 → UniDorm 규칙 위반
- 신규 필드(`place`, `rawPlaceName`) 추가 시 기존 안티패턴을 함께 정리하지 않음
- 이 패턴은 groupOrder 도메인에서 PR마다 반복 확인 필요

### CustomException만 catch하는 좁은 예외 처리 (Critical)
- `PlaceResolver.resolveByRawPlaceName/resolveByPlaceId`에서 `catch (CustomException e)` 블록만 존재
- 네트워크 타임아웃은 `ResourceAccessException`으로 변환되어 잡히지 않음
- quota increment 후 예외 발생 시 카운터 보정 없음 → 통계 오염
- 모든 외부 API 호출 래퍼에서 `catch (Exception e)` fallback 필수

### PlaceCacheService와 QuotaGuard 타임존 불일치 (High)
- `QuotaGuard`: `LocalDate.now(ZoneId.of("Asia/Seoul"))` 사용
- `PlaceCacheService`: `LocalDate.now()` JVM 기본 타임존 사용
- Redis 키 날짜가 최대 9시간 불일치 → 통계 오염
- 프로젝트 전반에서 날짜 키 생성 시 `ZoneId.of("Asia/Seoul")` 상수화 필요

### 선언만 된 ErrorCode (High)
- `KEYWORD_TOO_SHORT(24003)`: 선언됐지만 코드베이스 어디서도 throw되지 않음
- `PlaceSearchService.search()`는 짧은 키워드 시 빈 리스트를 반환(조용한 실패)
- 신규 ErrorCode 추가 시 실제 사용 여부 확인 필수

## 잘 된 설계 결정

- `PlaceSaveService` 별도 빈 분리: REQUIRES_NEW + DataIntegrityViolationException race condition 방어 올바름
- `NormalizationOutcome` 열거형: 성공/실패/스킵/한도초과를 풍부한 타입으로 표현
- `QuotaGuard.sendAlertOnce()`: setIfAbsent + TTL로 Slack 알림 폭발 방어
- `findUnnormalizedFoodOrders`: 커서 기반 페이징(id > lastId)으로 OFFSET 성능 회피
- SecurityConfig 신규 경로 누락 없이 등록, ADMIN 권한 제한 적용

## ErrorCode 현황 (place)
- 24001: KAKAO_API_ERROR (502)
- 24002: KAKAO_QUOTA_EXCEEDED (503)
- 24003: KEYWORD_TOO_SHORT (400) — 미사용 주의
- 24004: INVALID_PLACE_FOR_TYPE (400)
- 24005: PLACE_ID_INVALID (400)
- 24006: PLACE_MIGRATION_ALREADY_RUNNING (409)

## groupOrder 도메인 누적 패턴
- `GroupOrderRepositoryImpl.groupOrderObSort()`에 `System.out.println` 디버깅 코드 잔존 (53번째 줄)
- `GroupOrder` 엔티티에 @Builder + public @NoArgsConstructor 안티패턴 지속
- 클래스 레벨 @Transactional이 외부 API 호출까지 포함하는 범위 과다 문제 반복

# BR-662 — 날씨 API 기능 추가 (기상청 단기예보 Redis 캐싱)

## 기능 요약

클라이언트가 전달한 위경도를 기상청 격자 좌표(nx, ny)로 변환하고, Redis에 캐싱된 단기예보 데이터를 반환한다.
캐시 미스 시 기상청 단기예보 API를 직접 호출하며, 스케줄러가 발표 주기(3시간)마다 인천대학교 격자를 프리페치하여 대부분의 요청이 캐시 HIT로 처리되도록 한다.

---

## 동작 명세

### 1. 날씨 조회 (`GET /api/weather`)

- **입력**: 쿼리 파라미터 `lat` (위도, double), `lon` (경도, double)
- **처리 흐름**:
  1. 위경도 → 기상청 격자 좌표(nx, ny) 변환 (람베르트 정형원추도법 공식)
  2. Redis 키 `weather:{nx}:{ny}` 조회
  3. **HIT**: 캐시 데이터를 DTO로 역직렬화 후 반환
  4. **MISS**: 기상청 단기예보 API 직접 호출 → Redis에 TTL 3시간으로 저장 → 반환
- **출력**: 현재 예보 시각 기준 1건 (`ResponseWeatherDto`)

### 2. 스케줄러 프리페치 (`WeatherPrefetchScheduler`)

- **실행 시각**: 매일 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10
  (기상청 단기예보 발표 시각 +10분)
- **처리 흐름**:
  1. 인천대학교 격자 좌표(nx=54, ny=124)로 기상청 단기예보 API 호출
  2. 응답 데이터를 Redis 키 `weather:54:124`에 TTL 3시간으로 저장
- **출력**: 없음 (스케줄러는 반환값 없음)

### 기상청 단기예보 API 호출 규격

- **URL**: `https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst`
- **파라미터**: `serviceKey`, `numOfRows=1000`, `pageNo=1`, `dataType=JSON`, `base_date`, `base_time`, `nx`, `ny`
- **base_date / base_time 결정**: 현재 시각 기준 가장 최근 발표 시각 계산
  - 발표 시각: 02, 05, 08, 11, 14, 17, 20, 23시 (00분)
  - 현재 시각이 발표 시각의 정시를 넘지 않으면 이전 발표 시각 사용

---

## 도메인 데이터

### Redis 캐시 키/값

| 필드 | 내용 |
|------|------|
| Key | `weather:{nx}:{ny}` (예: `weather:54:124`) |
| Value | JSON 직렬화된 `WeatherCacheDto` |
| TTL | 10,800초 (3시간) |

### `WeatherCacheDto` (Redis 저장용)

| 필드 | 타입 | 설명 |
|------|------|------|
| nx | int | 격자 X 좌표 |
| ny | int | 격자 Y 좌표 |
| baseDate | String | 발표 기준 날짜 (yyyyMMdd) |
| baseTime | String | 발표 기준 시각 (HHmm) |
| tmp | String | 기온 (°C) |
| reh | String | 습도 (%) |
| wsd | String | 풍속 (m/s) |
| vec | String | 풍향 (0~360°) |
| sky | String | 하늘 상태 (1=맑음, 3=구름많음, 4=흐림) |
| pty | String | 강수 형태 (0=없음, 1=비, 2=비/눈, 3=눈, 4=소나기) |
| cachedAt | Instant | 캐시 저장 시각 |

### `ResponseWeatherDto` (API 응답용)

위 `WeatherCacheDto`와 동일 필드 구성. cachedAt은 제외.

---

## 비즈니스 규칙 / 제약

1. `lat`, `lon` 쿼리 파라미터는 모두 필수이다. 둘 중 하나라도 없으면 400 반환.
2. 위경도 범위: 위도 33.0~38.9, 경도 124.0~132.0 (대한민국 영역). 범위 초과 시 400 반환.
3. 기상청 API `serviceKey`는 `application.yml`의 `weather.api.service-key`로 주입한다. 코드에 하드코딩 금지.
4. 기상청 API 호출 시 HTTP 오류 또는 응답 코드가 정상이 아니면 `CustomException(ErrorCode.WEATHER_API_ERROR)` 발생.
5. 스케줄러는 `@Scheduled`를 직접 사용하지 않고, `@Transactional` 작업이 있으면 별도 서비스 메서드로 분리한다. (이번 스케줄러는 DB 작업 없으므로 분리 불필요)
6. 격자 좌표 변환 로직은 순수 계산 유틸 클래스(`GridConverter`)로 분리한다.

---

## 예외 · 경계 상황

| 상황 | 기대 동작 |
|------|-----------|
| `lat` 또는 `lon` 미전달 | 400 BAD_REQUEST |
| 대한민국 범위 외 위경도 | 400 BAD_REQUEST (`WEATHER_INVALID_LOCATION`) |
| Redis 캐시 HIT | 기상청 API 호출 없이 캐시 데이터 즉시 반환 |
| Redis 캐시 MISS, 기상청 API 정상 응답 | API 결과를 Redis에 저장 후 반환 |
| 기상청 API HTTP 오류 (4xx/5xx) | 500 (`WEATHER_API_ERROR`) |
| 기상청 API 응답 코드 비정상 (resultCode ≠ "00") | 500 (`WEATHER_API_ERROR`) |
| 스케줄러 프리페치 중 기상청 API 오류 | 오류 로그(`logger.error`) 기록 후 정상 종료 (앱 중단 없음) |

---

## 비목표 (Non-goals)

- **DB 저장**: 날씨 데이터를 RDB 테이블에 저장하지 않는다. Redis만 사용.
- **복수 격자 프리페치**: 스케줄러는 인천대학교 단일 격자(nx=54, ny=124)만 프리페치. 다중 캠퍼스 지원 불가.
- **시간대별 예보 목록 반환**: 현재 시각 기준 1건만 반환. 24시간/48시간 시계열 반환 불가.
- **인증/권한**: 퍼블릭 API. JWT 토큰 불필요.
- **WebSocket 날씨 Push**: 날씨 변경을 실시간 구독으로 전달하지 않는다.
- **날씨 알림 FCM**: 날씨 조건에 따른 푸시 알림 발송 불포함.
- **기상 특보/경보**: 단기예보 카테고리 외 특보 데이터 조회 불포함.

---

## 수용 기준 (Acceptance Criteria)

### 위경도 → 격자 좌표 변환

- **Given** 인천대학교 위경도(37.3749, 126.6368) **When** `GridConverter.toGrid()` 호출 **Then** nx=54, ny=124 반환

### 날씨 조회 — 캐시 HIT

- **Given** Redis에 `weather:54:124` 키가 존재할 때 **When** `GET /api/weather?lat=37.3749&lon=126.6368` **Then** 기상청 API 호출 없이 캐시 데이터 반환, HTTP 200

### 날씨 조회 — 캐시 MISS

- **Given** Redis에 `weather:54:124` 키가 없을 때 **When** `GET /api/weather?lat=37.3749&lon=126.6368` **Then** 기상청 API 1회 호출, 결과를 Redis에 저장 후 반환, HTTP 200

### 날씨 조회 — 유효성 오류

- **Given** `lat` 파라미터 없이 요청 **When** `GET /api/weather?lon=126.6368` **Then** 400 BAD_REQUEST
- **Given** 대한민국 범위 밖 위경도(0.0, 0.0) **When** `GET /api/weather?lat=0.0&lon=0.0` **Then** 400 BAD_REQUEST (`WEATHER_INVALID_LOCATION`)

### 스케줄러 프리페치

- **Given** 스케줄러가 실행될 때 **When** `WeatherPrefetchScheduler.prefetch()` **Then** 기상청 API 1회 호출, Redis `weather:54:124` 키에 데이터 저장

### 기상청 API 오류 처리

- **Given** 기상청 API가 HTTP 500 반환 **When** 날씨 조회 요청 **Then** `WEATHER_API_ERROR` 에러, HTTP 500
- **Given** 스케줄러 프리페치 중 기상청 API 오류 **When** 예외 발생 **Then** `logger.error` 기록, 앱 정상 운영 유지

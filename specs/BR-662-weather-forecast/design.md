# BR-662 — 도메인 설계: 날씨 API (기상청 단기예보 Redis 캐싱)

---

## 엔티티 / 값 객체

JPA 엔티티 없음. DB에 저장하지 않으며 Redis 전용 POJO만 사용한다.

### `WeatherCacheDto` (Redis 저장용 POJO)

| 필드 | 타입 | 제약 |
|------|------|------|
| nx | int | 기상청 격자 X 좌표 |
| ny | int | 기상청 격자 Y 좌표 |
| baseDate | String | 발표 기준 날짜 (yyyyMMdd) |
| baseTime | String | 발표 기준 시각 (HHmm) |
| tmp | String | 기온 (°C) |
| reh | String | 습도 (%) |
| wsd | String | 풍속 (m/s) |
| vec | String | 풍향 (0~360°) |
| sky | String | 하늘 상태 코드 |
| pty | String | 강수 형태 코드 |
| cachedAt | Instant | 캐시 저장 시각 |

- `@NoArgsConstructor` + `@AllArgsConstructor` + `@Getter` — Jackson 역직렬화 요건
- Redis 키: `weather:{nx}:{ny}`, TTL: 10,800초 (3시간)

### `ResponseWeatherDto` (API 응답 POJO)

`WeatherCacheDto`와 동일 필드 구성. `cachedAt` 제외.  
`WeatherCacheDto.toResponse()` 정적 팩토리로 변환.

---

## 애그리거트 경계

해당 없음. JPA 엔티티·연관관계·영속성 컨텍스트를 사용하지 않는다.

---

## 연관관계

없음.

---

## DB 스키마 변경

없음. Redis만 사용하며 RDB 테이블·컬럼·인덱스 변경 없음.

---

## 도메인 계층 구조

```
domain/weather/
├── controller/
│   ├── WeatherController.java          (신규)
│   └── WeatherApiSpecification.java    (신규)
├── service/
│   ├── WeatherService.java             (신규)  ← 캐시 조회/저장 오케스트레이션
│   └── WeatherCacheService.java        (신규)  ← Redis read/write 전담
├── client/
│   ├── KmaForecastClient.java          (신규)  ← 기상청 단기예보 API, RestClient
│   └── dto/
│       ├── KmaForecastResponse.java    (신규)  ← 기상청 API 응답 최상위 DTO
│       └── KmaForecastItem.java        (신규)  ← items.item[] 요소 DTO
├── dto/
│   ├── WeatherCacheDto.java            (신규)
│   └── response/
│       └── ResponseWeatherDto.java     (신규)
└── util/
    └── GridConverter.java              (신규)  ← 순수 계산 유틸 (static 메서드)

global/scheduler/
└── WeatherPrefetchScheduler.java       (신규)
```

### 수정할 기존 파일

| 파일 | 변경 내용 |
|------|-----------|
| `global/exception/ErrorCode.java` | `WEATHER_API_ERROR`, `WEATHER_INVALID_LOCATION` 추가 |
| `global/config/SecurityConfig.java` | `GET /weather/**` → `.permitAll()` 추가 |

---

## 클래스별 설계 상세

### `WeatherController`

- `@RestController`, `@RequestMapping("/weather")`, `@RequiredArgsConstructor`, `@Validated`
- `GET /weather?lat={lat}&lon={lon}` → `ResponseEntity<ResponseWeatherDto>`
- `lat`, `lon`: `@RequestParam double`, Spring이 타입 불일치 시 자동 400 반환
- 위경도 범위 검증은 `WeatherService`에서 `CustomException(WEATHER_INVALID_LOCATION)` 발생

### `WeatherService`

- `@Service`, `@RequiredArgsConstructor`
- `getWeather(double lat, double lon)`:
  1. 위경도 범위 검증 (벗어나면 `WEATHER_INVALID_LOCATION`)
  2. `GridConverter.toGrid(lat, lon)` → `(nx, ny)`
  3. `WeatherCacheService.get(nx, ny)` 조회
  4. HIT → `WeatherCacheDto.toResponse()` 반환
  5. MISS → `KmaForecastClient.fetch(nx, ny)` 호출 → `WeatherCacheService.put(nx, ny, dto)` 저장 → 반환

### `WeatherCacheService`

- `@Service`, `@RequiredArgsConstructor`
- 주입: `RedisTemplate<String, Object>`, `ObjectMapper`
- `get(int nx, int ny) → Optional<WeatherCacheDto>`:
  - `redisTemplate.opsForValue().get("weather:{nx}:{ny}")` 조회
  - `Object` 결과를 `objectMapper.convertValue(result, WeatherCacheDto.class)`로 변환
- `put(int nx, int ny, WeatherCacheDto dto)`:
  - `redisTemplate.opsForValue().set(key, dto, Duration.ofSeconds(10_800))`

### `KmaForecastClient`

- `@Component`, `@Slf4j`
- `@Value("${weather.api.service-key}")` 주입
- `@PostConstruct`로 `RestClient` 초기화 (connect timeout 3s, read timeout 10s)
- `fetch(int nx, int ny) → WeatherCacheDto`:
  1. 현재 시각 기준 base_date / base_time 계산 (발표 시각: 02·05·08·11·14·17·20·23시)
  2. `RestClient.get()` 호출 → 4xx/5xx → `CustomException(WEATHER_API_ERROR)`
  3. `response.getResponse().getHeader().getResultCode()` ≠ `"00"` → `CustomException(WEATHER_API_ERROR)`
  4. `items.item[]`에서 TMP·REH·WSD·VEC·SKY·PTY 카테고리 필터링해 `WeatherCacheDto` 생성

### `GridConverter`

- 순수 유틸 클래스, 인스턴스화 불가 (`private` 생성자)
- `toGrid(double lat, double lon) → int[]` : `[nx, ny]`
- 기상청 공식 람베르트 정형원추도법 공식 사용 (상수: RE=6371.00877, GRID=5.0, SLAT1=30.0, SLAT2=60.0, OLON=126.0, OLAT=38.0, XO=43, YO=136)

### `WeatherPrefetchScheduler`

- `@Component`, `@RequiredArgsConstructor`, `@Slf4j`
- `@Scheduled(cron = "0 10 2,5,8,11,14,17,20,23 * * *")`
- 인천대학교 고정 격자(nx=54, ny=124)로 `KmaForecastClient.fetch()` 호출
- 결과를 `WeatherCacheService.put(54, 124, dto)` 저장
- 예외 발생 시 `log.error()` 기록, 앱 중단 없이 종료 (`try-catch`)

---

## 비목표

- JPA 엔티티·DB 테이블 설계 — Redis만 사용
- `WeatherCacheDto`에 `@Entity`, `@RedisHash` 등 어노테이션 — 단순 POJO
- 복수 격자 프리페치 스케줄러 — 단일 좌표만
- 응답 DTO에 `cachedAt` 노출 — API 응답에서 제외
- `StringRedisTemplate` 사용 — 복잡한 객체이므로 `RedisTemplate<String, Object>` 사용
- `GridConverter`를 Spring Bean으로 등록 — 순수 static 유틸로 충분

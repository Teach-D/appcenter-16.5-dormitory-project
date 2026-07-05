# 날씨 API (BR-662) 명세서

> Base URL: `https://{host}/weather`
> 인증: 불필요 (퍼블릭 API)

---

## 날씨 조회

| 항목 | 내용 |
|------|------|
| **메서드** | `GET` |
| **경로** | `/weather` |
| **인증** | 없음 |
| **설명** | 위경도를 기상청 격자 좌표로 변환하고, Redis 캐시에서 현재 단기예보 1건을 반환한다. 캐시 미스 시 기상청 단기예보 API를 직접 호출한다. |

### Request

#### Query Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `lat` | `Double` | ✅ | 위도 (33.0 ~ 38.9, 대한민국 범위) |
| `lon` | `Double` | ✅ | 경도 (124.0 ~ 132.0, 대한민국 범위) |

**요청 예시**

```
GET /weather?lat=37.3749&lon=126.6368
```

### Response

#### 성공 응답 — `200 OK`

| 필드 | 타입 | 설명 |
|------|------|------|
| `nx` | `Int` | 기상청 격자 X 좌표 |
| `ny` | `Int` | 기상청 격자 Y 좌표 |
| `baseDate` | `String` | 발표 기준 날짜 (`yyyyMMdd`) |
| `baseTime` | `String` | 발표 기준 시각 (`HHmm`) |
| `tmp` | `String` | 기온 (°C) |
| `reh` | `String` | 습도 (%) |
| `wsd` | `String` | 풍속 (m/s) |
| `vec` | `String` | 풍향 (0 ~ 360°) |
| `sky` | `String` | 하늘 상태 코드 (`1`=맑음, `3`=구름많음, `4`=흐림) |
| `pty` | `String` | 강수 형태 코드 (`0`=없음, `1`=비, `2`=비/눈, `3`=눈, `4`=소나기) |

```json
{
  "nx": 54,
  "ny": 124,
  "baseDate": "20260705",
  "baseTime": "1400",
  "tmp": "28",
  "reh": "60",
  "wsd": "2.5",
  "vec": "180",
  "sky": "1",
  "pty": "0"
}
```

#### 에러 응답

| 상태 코드 | ErrorCode | code | 발생 조건 |
|-----------|-----------|------|-----------|
| `400 Bad Request` | `VALIDATION_FAILED` | 5001 | `lat` 또는 `lon` 파라미터 누락 |
| `400 Bad Request` | `VALIDATION_FAILED` | 5001 | `lat` 또는 `lon`에 숫자가 아닌 값 전달 |
| `400 Bad Request` | `WEATHER_INVALID_LOCATION` | 25002 | 위경도가 대한민국 범위(위도 33.0~38.9, 경도 124.0~132.0) 외부 |
| `500 Internal Server Error` | `WEATHER_API_ERROR` | 25001 | 기상청 API HTTP 오류(4xx/5xx) 또는 응답 코드 비정상(`resultCode ≠ "00"`) |

**에러 응답 형식**

```json
{
  "code": 25002,
  "name": "WEATHER_INVALID_LOCATION",
  "message": "[Weather] 대한민국 범위를 벗어난 위경도입니다.",
  "errors": null
}
```

**에러 시나리오 예시**

| 요청 예시 | 발생 에러 |
|-----------|-----------|
| `GET /weather?lon=126.6368` | 400 `VALIDATION_FAILED` (lat 누락) |
| `GET /weather?lat=abc&lon=126.6368` | 400 `VALIDATION_FAILED` (타입 불일치) |
| `GET /weather?lat=0.0&lon=0.0` | 400 `WEATHER_INVALID_LOCATION` |
| `GET /weather?lat=37.3749&lon=126.6368` (기상청 API 다운) | 500 `WEATHER_API_ERROR` |

---

## 하늘 상태 / 강수 형태 코드표

### SKY (하늘 상태)

| 코드 | 의미 |
|------|------|
| `1` | 맑음 |
| `3` | 구름많음 |
| `4` | 흐림 |

### PTY (강수 형태)

| 코드 | 의미 |
|------|------|
| `0` | 강수 없음 |
| `1` | 비 |
| `2` | 비/눈 (진눈깨비) |
| `3` | 눈 |
| `4` | 소나기 |

---

## 캐싱 동작 참고

| 상황 | 처리 방식 |
|------|-----------|
| Redis 캐시 HIT (`weather:{nx}:{ny}` 존재) | 기상청 API 호출 없이 즉시 반환 (응답 지연 ≈40ms) |
| Redis 캐시 MISS | 기상청 단기예보 API 호출 후 TTL 3시간으로 Redis 저장 (응답 지연 ≈320ms) |
| 스케줄러 프리페치 (매일 02:10, 05:10, 08:10, 11:10, 14:10, 17:10, 20:10, 23:10) | 인천대학교 격자(nx=54, ny=124) 데이터를 미리 캐싱 — 클라이언트 응답에는 직접 영향 없음 |

---

## 추론 항목

> 코드가 아직 구현되지 않은 상태이므로, design.md 기준으로 작성되었습니다.
> `/implement` 완료 후 실제 동작과 대조하여 수정하세요.

- **`lat`/`lon` 누락 시 응답**: `MissingServletRequestParameterException` 발생 → `GlobalExceptionHandler.handleMissingRequestParam()` → `VALIDATION_FAILED` (400)
- **`lat`/`lon` 타입 불일치 시 응답**: `MethodArgumentTypeMismatchException` 발생 → `GlobalExceptionHandler.handleMethodArgumentTypeMismatch()` → `VALIDATION_FAILED` (400)
- **ErrorCode 번호**: `WEATHER_INVALID_LOCATION(25002)`, `WEATHER_API_ERROR(25001)` — 기존 최대값 24xxx(Place) 다음 번호로 설정
- **응답 필드 타입 `String`**: 기상청 API가 모든 카테고리 값을 문자열로 반환하므로 파싱 없이 그대로 전달

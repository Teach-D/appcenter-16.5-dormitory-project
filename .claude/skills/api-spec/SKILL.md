---
name: api-spec
description: 기획서나 구현할 기능 설명을 받아 UniDorm 백엔드 컨벤션에 맞는 상세 API 명세서를 생성하는 스킬. "API 명세서 만들어줘", "명세서 만들어줘", "API 설계해줘", "엔드포인트 설계해줘"라는 요청이 오면 반드시 이 스킬을 사용한다. 기획서나 기능 설명 없이 요청이 오면 먼저 기능 설명을 요청한다.
---

# API Spec Generator

기획서 또는 기능 설명을 받아 UniDorm 백엔드 컨벤션에 맞는 API 명세서를 생성한다.

결과는 **채팅창에만** 출력한다. 파일을 생성하거나 수정하지 않는다.

---

## Step 1 — 도메인 파악

- 기획서에서 도메인명을 추론하거나 사용자에게 확인한다.
- 기존 13개 도메인 중 하나라면 기존 URL 패턴을 참고한다:
  `user`, `announcement`, `complaint`, `groupOrder`, `roommate`, `notification`, `calender`, `fcm`, `coupon`, `feature`, `report`, `survey`, `tip`
- 신규 도메인이면 도메인명 복수형을 기본 경로로 사용한다.

---

## Step 2 — 엔드포인트 설계

### URL 규칙
- 기본 경로: `/{도메인s}` 복수형 — 예: `/complaints`, `/group-orders`
- 복합 단어: kebab-case — 예: `group-orders`, `user-notifications`
- 경로 변수: `{entityId}` 형태 — 예: `/{complaintId}`
- 관리자 전용 URL: `/admin/{도메인s}` 접두사 사용
- 서브 리소스: `/{상위Id}/{하위도메인s}` — 예: `/{complaintId}/replies`
- 상태 변경 등 특정 동작: `PATCH /{id}/status`, `/{id}/like` 등

### HTTP 메서드
| 용도 | 메서드 |
|------|--------|
| 생성 | POST |
| 전체 수정 | PUT |
| 부분 수정 / 상태 변경 | PATCH |
| 조회 | GET |
| 삭제 | DELETE |

### 권한 태그
- `[AUTH]` — 로그인한 사용자 필요 (`@AuthenticationPrincipal`)
- `[ADMIN]` — 관리자 전용
- `[DORMITORY]` — 기숙사 담당자
- 태그 없음 — 누구나 접근 가능

---

## Step 3 — 요청/응답 타입 결정

### Request 방식 선택
| 상황 | 방식 |
|------|------|
| JSON 데이터만 | `application/json` + Body |
| 파일 포함 | `multipart/form-data` (JSON part + file part) |
| 단순 값 1~2개 | `@RequestParam` 또는 `@PathVariable` |

### DTO 네이밍 규칙
- 요청 DTO: `Request{Action}{Entity}Dto`
  - Action 예시: `Create`, `Update`, `Search`, `Reply`, `Status`
- 응답 DTO: `Response{Entity}Dto`
  - 상세 조회: `Response{Entity}DetailDto`
  - 목록/단순 조회: `Response{Entity}Dto` (내용이 간단하면 재사용)
- 페이징: `Page<Response{Entity}Dto>` 또는 커서 파라미터(`lastId`, `size`)

---

## Step 4 — 에러 코드 선택

기능과 직접 관련된 에러 코드만 포함한다.

### 공통 (항상 고려)
- `USER_NOT_FOUND` (404) — 사용자 미존재
- `JWT_ENTRY_POINT` (401) — 미인증 접근
- `JWT_ACCESS_DENIED` (403) — 권한 없음
- `VALIDATION_FAILED` (400) — 입력값 오류

### 도메인별 주요 에러 코드
| 도메인 | 에러 코드 |
|--------|---------|
| complaint | `COMPLAINT_NOT_FOUND`, `COMPLAINT_NOT_OWNED_BY_USER`, `COMPLAINT_ALREADY_REPLIED`, `COMPLAINT_REPLY_NOT_FOUND` |
| groupOrder | `GROUP_ORDER_NOT_FOUND`, `GROUP_ORDER_NOT_OWNED_BY_USER`, `ALREADY_GROUP_ORDER_LIKE_USER`, `GROUP_ORDER_TITLE_DUPLICATE` |
| roommate | `ROOMMATE_BOARD_NOT_FOUND`, `ROOMMATE_FORBIDDEN_ACCESS`, `ROOMMATE_MATCHING_ALREADY_REQUESTED`, `ROOMMATE_MATCHING_NOT_FOUND` |
| survey | `SURVEY_NOT_FOUND`, `SURVEY_NOT_OWNED_BY_USER`, `ALREADY_SURVEY_RESPONSE`, `SURVEY_CLOSED`, `SURVEY_NOT_IN_PERIOD` |
| announcement | `ANNOUNCEMENT_NOT_REGISTERED`, `ANNOUNCEMENT_FORBIDDEN` |
| tip | `TIP_NOT_FOUND`, `TIP_NOT_OWNED_BY_USER`, `ALREADY_TIP_LIKE_USER` |
| fcm | `FCM_TOKEN_NOT_FOUND`, `FCM_SEND_FAILED`, `FCM_OUTBOX_NOT_FOUND` |
| coupon | `COUPON_NOT_FOUND` |
| report | `REPORT_NOT_REGISTERED` |

신규 도메인이라 아직 에러 코드가 없는 경우, 필요한 에러 코드명을 명세에 포함하고 출력 후 `ErrorCode.java`에 추가가 필요하다고 안내한다.

---

## Step 5 — DTO 필드 정의

각 DTO의 필드를 테이블로 정의한다.

### 타입 기준
| 상황 | 타입 |
|------|------|
| PK / FK | `Long` |
| 일반 수 | `Integer` |
| 문자열 | `String` |
| 불린 | `Boolean` |
| 날짜시간 | `LocalDateTime` |
| 날짜만 | `LocalDate` |
| 열거형 | 실제 Enum 클래스명 (예: `ComplaintStatus`) |
| 리스트 | `List<타입>` |

Nullable 필드는 비고에 `nullable` 표기한다.

---

## 출력 형식

아래 형식을 정확히 따른다.

````
## {기능명} API 명세서

### Endpoints

---

#### {HTTP메서드} {URL}
- **권한**: [AUTH] / [ADMIN] / 없음
- **설명**: 한 줄 설명
- **Request**:
  - Content-Type: `application/json`
  - Body: `Request{Action}{Entity}Dto`
- **Response**: `Response{Entity}Dto`
- **에러**:
  - `ERROR_CODE` (HTTP상태코드) — 설명

---

(엔드포인트 반복)

---

### Schemas

#### Request{Action}{Entity}Dto

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| fieldName | String | Y | 설명 |
| fieldName | Long | N | nullable, 설명 |

#### Response{Entity}Dto

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 고유 ID |
| fieldName | String | 설명 |
````

---

## 출력 후 안내

명세서 출력 후 다음을 안내한다:
1. 추가/수정이 필요한 엔드포인트가 있으면 말해달라
2. 신규 에러 코드가 있다면 `ErrorCode.java`에 추가가 필요하다
3. 명세 확정 후 `/feature` 스킬로 TDD 구현을 바로 시작할 수 있다

# BR-686 룸메이트 등록 학기 저장 — API 명세서

> Base URL: `/roommates`  
> 인증: Bearer Token (JWT) — 별도 표기가 없는 한 모든 엔드포인트에 필요  
> 이 명세는 BR-686으로 **변경되는 부분**만 기술한다. 나머지 필드는 기존과 동일하다.

---

## 변경 개요

`ResponseRoommatePostDto`를 반환하는 모든 엔드포인트의 응답 본문에  
`year` (Integer, nullable)와 `semester` (Integer, nullable) 두 필드가 추가된다.

| 추가 응답 필드 | 타입 | 설명 |
|---|---|---|
| `year` | `Integer` (nullable) | 게시글 생성 년도 (예: 2026). 1·2·7·8월 외 생성 시 null |
| `semester` | `Integer` (nullable) | 학기 번호. 1·2월 생성→1, 7·8월 생성→2, 그 외→null |

---

## 1. 룸메이트 게시글 등록

| 항목 | 내용 |
|---|---|
| **메서드** | `POST` |
| **경로** | `/roommates` |
| **인증** | Bearer Token |
| **설명** | 체크리스트와 게시글을 함께 생성한다. 학기는 서버 시각 기준으로 자동 계산된다. 요청 본문에 학기 필드 없음. |

### Request Body

Content-Type: `application/json`

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `title` | `String` | ✅ | 게시글 제목 |
| `dormPeriod` | `Set<DormDay>` | ✅ | 기숙사 입사 기간 (예: `["MON_5", "SAT_5"]`) |
| `dormType` | `DormType` | ✅ | 기숙사 동 (예: `DORM_1`) |
| `college` | `College` | ✅ | 단과대 |
| `religion` | `ReligionType` | ❌ | 종교 |
| `mbti` | `String` | ❌ | MBTI |
| `smoking` | `SmokingType` | ❌ | 흡연 여부 |
| `snoring` | `SnoringType` | ❌ | 코골이 여부 |
| `toothGrind` | `TeethGrindingType` | ❌ | 이갈이 여부 |
| `sleeper` | `SleepSensitivityType` | ❌ | 수면 민감도 |
| `showerHour` | `ShowerTimeType` | ❌ | 샤워 시간대 |
| `showerTime` | `ShowerDurationType` | ❌ | 샤워 소요 시간 |
| `bedTime` | `BedTimeType` | ❌ | 취침 시간 |
| `arrangement` | `CleanlinessType` | ❌ | 정리 정돈 성향 |
| `comment` | `String` | ❌ | 자유 코멘트 |

### Response

#### 성공 응답 — `201 Created`

기존 응답 필드에 `year`, `semester`가 추가된다.

```json
{
  "id": 1,
  "title": "2026 1학기 룸메이트 구합니다",
  "type": "ROOMMATE",
  "createDate": "2026-02-10T14:30:00",
  "dormPeriod": ["MON_5"],
  "dormType": "DORM_1",
  "college": "ENGINEERING",
  "religion": "NONE",
  "mbti": "INFJ",
  "smoking": "NON_SMOKER",
  "snoring": "NON_SNORER",
  "toothGrind": "NON_GRINDER",
  "sleeper": "PREFER_DARKNESS",
  "showerHour": "MORNING",
  "showerTime": "WITHIN_10_MINUTES",
  "bedTime": "EARLY_SLEEPER",
  "arrangement": "NEAT",
  "comment": "조용한 룸메이트를 원합니다",
  "roommateBoardLike": 0,
  "userId": 42,
  "userName": "홍길동",
  "isMatched": false,
  "userProfileImageUrl": "https://...",
  "year": 2026,
  "semester": 1
}
```

> 7·8월 외 달에 생성된 경우 `"year": null, "semester": null`

#### 에러 응답

| 상태 코드 | 발생 조건 |
|---|---|
| `404 Not Found` | 인증된 유저가 DB에 없음 (ROOMMATE_USER_NOT_FOUND) |

---

## 2. 게시글 목록 조회 (최신순)

| 항목 | 내용 |
|---|---|
| **메서드** | `GET` |
| **경로** | `/roommates/list` |
| **인증** | 불필요 |
| **변경 사항** | 응답 배열의 각 항목에 `year`, `semester` 필드 추가 |

응답 스키마는 **1번 항목의 성공 응답**과 동일하다. (배열 형태)

---

## 3. 게시글 단일 조회

| 항목 | 내용 |
|---|---|
| **메서드** | `GET` |
| **경로** | `/roommates/{boardId}` |
| **인증** | 불필요 |
| **변경 사항** | 응답에 `year`, `semester` 필드 추가 |

### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `boardId` | `Long` | ✅ | 조회할 게시글 ID |

응답 스키마는 **1번 항목의 성공 응답**과 동일하다.

---

## 4. 그 외 `ResponseRoommatePostDto` 반환 엔드포인트

아래 엔드포인트도 동일하게 응답에 `year`, `semester` 추가.  
요청 파라미터·본문에는 변경 없음.

| 메서드 | 경로 | 설명 |
|---|---|---|
| `PUT` | `/roommates` | 게시글·체크리스트 수정 |
| `GET` | `/roommates/latest10/random` | 최신 10개 중 무작위 1개 |
| `GET` | `/roommates/list/scroll` | 최신순 커서 페이지네이션 |

---

## 추론 항목

> 아래 항목은 코드에서 명시적으로 확인되지 않아 추론했습니다.

- `year` / `semester` 필드는 응답에만 추가되며 요청(Request Body)에는 포함되지 않는다: 서버 자동 계산 방식으로 명세에서 확인됨.
- 비인증 조회 엔드포인트(목록·단일조회)의 인증 여부: 기존 컨트롤러에 `@AuthenticationPrincipal` 없음을 확인.

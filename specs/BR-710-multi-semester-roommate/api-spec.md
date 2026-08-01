# BR-710 룸메이트 학기별 복수 보유 — 변경 API 명세서

> Base URL: `http://localhost:8080`
>
> 이 문서는 BR-710으로 **추가·변경되는 엔드포인트만** 기술한다.
> 변경 없는 GET 엔드포인트(목록 조회, 상세 조회, 유사도, 스크롤 등)는 포함하지 않는다.

---

## 공통

### 인증

모든 엔드포인트는 `Authorization: Bearer <JWT>` 헤더 필요.
(단, 목록 조회류는 비로그인 허용이나 이 명세서 범위 아님)

### 공통 에러 응답 형식

```json
{
  "status": 409,
  "code": 7004,
  "message": "[Roommate] 이미 작성된 게시글이 있습니다."
}
```

### Enum 직렬화 규칙

모든 Enum은 **한글 description 문자열**로 직렬화·역직렬화된다.
`SemesterType`만 예외로 **정수**(`1` / `2` / `3` / `4`)를 사용한다.

| Enum | 허용 값 |
|------|---------|
| `DormDay` | `"월"` `"화"` `"수"` `"목"` `"금"` `"토"` `"일"` |
| `DormType` | `"1기숙사"` `"2기숙사"` `"3기숙사"` |
| `ReligionType` | `"기독교"` `"불교"` `"천주교"` `"이슬람교"` `"힌두교"` `"유대교"` `"무교"` `"기타"` |
| `SmokingType` | `"피워요"` `"안피워요"` |
| `SnoringType` | `"골아요"` `"안골아요"` |
| `TeethGrindingType` | `"갈아요"` `"안갈아요"` |
| `SleepSensitivityType` | `"밝아요"` `"어두워요"` `"몰라요"` |
| `ShowerTimeType` | `"아침"` `"저녁"` `"둘다"` |
| `ShowerDurationType` | `"10분 이내"` `"30분 이내"` `"1시간 이내"` |
| `BedTimeType` | `"일찍 자요"` `"늦게 자요"` `"때마다 달라요"` |
| `CleanlinessType` | `"깔끔해요"` `"개방적이에요"` `"애매해요"` |
| `SemesterType` | `1` (1학기) `2` (2학기) `3` (여름방학) `4` (겨울방학) |

---

## 1. 룸메이트 게시글·체크리스트 생성

> **변경 내용**: 동일 학기 중복 생성 시 409 반환 추가 (경로·메서드·바디 변경 없음)

| 항목 | 내용 |
|------|------|
| **메서드** | `POST` |
| **경로** | `/roommates` |
| **인증** | Bearer Token |
| **설명** | 현재 매칭 기간(year + semester)에 체크리스트와 게시글을 함께 생성한다. year·semester는 서버가 자동 계산하며 클라이언트가 전달하지 않는다. |

### Request Body

Content-Type: `application/json`

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| `title` | `String` | ✅ | 게시글 제목 |
| `dormPeriod` | `Set<DormDay>` | ✅ | 기숙사 입실 요일 (한글 배열) |
| `dormType` | `DormType` | ✅ | 기숙사 구분 |
| `college` | `College` | ✅ | 소속 대학 |
| `religion` | `ReligionType` | ✅ | 종교 |
| `mbti` | `String` | ✅ | MBTI (예: `"INFJ"`) |
| `smoking` | `SmokingType` | ✅ | 흡연 여부 |
| `snoring` | `SnoringType` | ✅ | 코골이 여부 |
| `toothGrind` | `TeethGrindingType` | ✅ | 이갈이 여부 |
| `sleeper` | `SleepSensitivityType` | ✅ | 수면 예민도 |
| `showerHour` | `ShowerTimeType` | ✅ | 샤워 시간대 |
| `showerTime` | `ShowerDurationType` | ✅ | 샤워 소요 시간 |
| `bedTime` | `BedTimeType` | ✅ | 취침 시간 유형 |
| `arrangement` | `CleanlinessType` | ✅ | 정리정돈 성향 |
| `comment` | `String` | ❌ | 한마디 |

```json
{
  "title": "2026년 1학기 룸메이트 구해요",
  "dormPeriod": ["월", "화", "수", "목", "금"],
  "dormType": "1기숙사",
  "college": "공과대",
  "religion": "무교",
  "mbti": "INFJ",
  "smoking": "안피워요",
  "snoring": "안골아요",
  "toothGrind": "안갈아요",
  "sleeper": "어두워요",
  "showerHour": "저녁",
  "showerTime": "10분 이내",
  "bedTime": "일찍 자요",
  "arrangement": "깔끔해요",
  "comment": "조용하고 깔끔하게 지내고 싶어요"
}
```

### Response

#### 성공 — `201 Created`

| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | `Long` | 생성된 게시글(RoommateBoard) ID |
| `title` | `String` | 제목 |
| `dormPeriod` | `List<DormDay>` | 정렬된 요일 배열 |
| `dormType` | `DormType` | 기숙사 구분 |
| `college` | `College` | 소속 대학 |
| `religion` | `ReligionType` | 종교 |
| `mbti` | `String` | MBTI |
| `smoking` | `SmokingType` | 흡연 |
| `snoring` | `SnoringType` | 코골이 |
| `toothGrind` | `TeethGrindingType` | 이갈이 |
| `sleeper` | `SleepSensitivityType` | 수면 예민도 |
| `showerHour` | `ShowerTimeType` | 샤워 시간대 |
| `showerTime` | `ShowerDurationType` | 샤워 소요 시간 |
| `bedTime` | `BedTimeType` | 취침 유형 |
| `arrangement` | `CleanlinessType` | 정리 성향 |
| `comment` | `String` | 한마디 |
| `userId` | `Long` | 작성자 ID |
| `userName` | `String` | 작성자 이름 |
| `createDate` | `String (ISO 8601)` | 작성 시각 |
| `isMatched` | `Boolean` | 매칭 완료 여부 (생성 직후 항상 `false`) |
| `year` | `Integer` | 등록 연도 (서버 자동 설정) |
| `semester` | `Integer` | 학기 코드 (서버 자동 설정) |

```json
{
  "id": 42,
  "title": "2026년 1학기 룸메이트 구해요",
  "dormPeriod": ["월", "화", "수", "목", "금"],
  "dormType": "1기숙사",
  "college": "공과대",
  "religion": "무교",
  "mbti": "INFJ",
  "smoking": "안피워요",
  "snoring": "안골아요",
  "toothGrind": "안갈아요",
  "sleeper": "어두워요",
  "showerHour": "저녁",
  "showerTime": "10분 이내",
  "bedTime": "일찍 자요",
  "arrangement": "깔끔해요",
  "comment": "조용하고 깔끔하게 지내고 싶어요",
  "userId": 101,
  "userName": "홍길동",
  "createDate": "2026-08-01T10:30:00",
  "isMatched": false,
  "year": 2026,
  "semester": 1
}
```

#### 에러 응답

| 상태 코드 | ErrorCode | 발생 조건 |
|-----------|-----------|-----------|
| `401 Unauthorized` | — | JWT 미포함 또는 만료 |
| `404 Not Found` | `7001` | 로그인 유저 미존재 |
| `409 Conflict` | `7004` | **현재 학기에 이미 게시글이 존재** ← BR-710 신규 |

---

## 2. 룸메이트 게시글·체크리스트 수정

> **변경 내용**: `PUT /roommates` → `PUT /roommates/{boardId}` (Breaking Change)

| 항목 | 내용 |
|------|------|
| **메서드** | `PUT` |
| **경로** | `/roommates/{boardId}` |
| **인증** | Bearer Token |
| **설명** | boardId로 특정 게시글을 지정해 체크리스트 내용을 수정한다. 소유자만 수정 가능. |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `boardId` | `Long` | ✅ | 수정할 RoommateBoard ID |

#### Request Body

`POST /roommates`의 Request Body와 동일한 구조.

```json
{
  "title": "수정된 제목",
  "dormPeriod": ["월", "화", "수"],
  "dormType": "1기숙사",
  "college": "공과대",
  "religion": "무교",
  "mbti": "ENFP",
  "smoking": "안피워요",
  "snoring": "골아요",
  "toothGrind": "안갈아요",
  "sleeper": "몰라요",
  "showerHour": "아침",
  "showerTime": "30분 이내",
  "bedTime": "늦게 자요",
  "arrangement": "개방적이에요",
  "comment": "수정된 한마디"
}
```

### Response

#### 성공 — `200 OK`

`POST /roommates` 성공 응답과 동일한 구조 (`ResponseRoommatePostDto`).

#### 에러 응답

| 상태 코드 | ErrorCode | 발생 조건 |
|-----------|-----------|-----------|
| `401 Unauthorized` | — | JWT 미포함 또는 만료 |
| `403 Forbidden` | `7007` | boardId의 소유자가 요청자가 아님 |
| `404 Not Found` | `7002` | boardId에 해당하는 게시글 없음 |

---

## 3. 룸메이트 게시글·체크리스트 삭제

> **변경 내용**: `DELETE /roommates` → `DELETE /roommates/{boardId}` (Breaking Change)

| 항목 | 내용 |
|------|------|
| **메서드** | `DELETE` |
| **경로** | `/roommates/{boardId}` |
| **인증** | Bearer Token |
| **설명** | boardId로 특정 게시글과 연결된 체크리스트를 함께 삭제한다. 소유자만 삭제 가능. 연결된 채팅방의 board/checklist 참조는 null 처리되며 채팅 이력은 보존된다. |

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `boardId` | `Long` | ✅ | 삭제할 RoommateBoard ID |

### Response

#### 성공 — `204 No Content`

응답 바디 없음.

#### 에러 응답

| 상태 코드 | ErrorCode | 발생 조건 |
|-----------|-----------|-----------|
| `401 Unauthorized` | — | JWT 미포함 또는 만료 |
| `403 Forbidden` | `7009` | boardId의 소유자가 요청자가 아님 |
| `404 Not Found` | `7002` | boardId에 해당하는 게시글 없음 |

---

## 변경 요약

| 엔드포인트 | 변경 전 | 변경 후 | 유형 |
|-----------|---------|---------|------|
| 생성 | `POST /roommates` | `POST /roommates` (경로 동일) | 동작 변경 (409 추가) |
| 수정 | `PUT /roommates` | `PUT /roommates/{boardId}` | **Breaking** |
| 삭제 | `DELETE /roommates` | `DELETE /roommates/{boardId}` | **Breaking** |

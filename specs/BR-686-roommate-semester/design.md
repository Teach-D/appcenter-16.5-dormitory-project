# BR-686 설계 — 룸메이트 체크리스트·게시글 등록 학기 저장

## 엔티티 / 값 객체

### RoommateCheckList (수정)

기존 엔티티에 두 필드만 추가한다.

| 필드 | Java 타입 | DB 컬럼 | 제약 |
|---|---|---|---|
| `year` | `Integer` | `registration_year` | NULL 허용. `year`는 MySQL 예약어라 컬럼명을 `registration_year`로 지정 |
| `semester` | `Integer` | `semester` | NULL 허용. 값은 1 또는 2 |

### RoommateBoard (수정)

동일한 두 필드 추가.

| 필드 | Java 타입 | DB 컬럼 | 제약 |
|---|---|---|---|
| `year` | `Integer` | `registration_year` | NULL 허용 |
| `semester` | `Integer` | `semester` | NULL 허용 |

---

## 애그리거트 경계

변경 없음. `RoommateBoard` → `RoommateCheckList` 기존 1:1 구조 유지.

---

## 연관관계

변경 없음.

---

## DB 스키마 변경

```sql
-- roommate_check_list 테이블
ALTER TABLE roommate_check_list
    ADD COLUMN registration_year INT NULL,
    ADD COLUMN semester          INT NULL;

-- roommate_board 테이블
ALTER TABLE roommate_board
    ADD COLUMN registration_year INT NULL,
    ADD COLUMN semester          INT NULL;
```

---

## 도메인 계층 구조

```
domain/roommate/
├── entity/
│   ├── RoommateCheckList.java   ← 수정: year·semester 필드 + @Builder 파라미터 추가
│   └── RoommateBoard.java       ← 수정: year·semester 필드 + @Builder 파라미터 추가
├── service/
│   └── RoommateService.java     ← 수정: createRoommateCheckListandBoard()에 학기 계산 로직 추가
└── dto/response/
    ├── ResponseRoommatePostDto.java       ← 수정: year·semester 필드 + @Builder 파라미터 추가
    └── ResponseRoommateCheckListDto.java  ← 수정: year·semester 필드 + from() 매핑 추가
```

**새로 생성하는 클래스 없음.**

---

## 학기 계산 로직 (RoommateService 내부)

`createRoommateCheckListandBoard()` 에서 `LocalDate.now().getMonthValue()` 로 현재 월을 읽어 인라인으로 계산한다.

```
month ∈ {1, 2}  →  year = 현재 년도, semester = 1
month ∈ {7, 8}  →  year = 현재 년도, semester = 2
그 외            →  year = null,    semester = null
```

동일한 시각을 기준으로 계산하므로 `RoommateCheckList`와 `RoommateBoard`에 같은 값이 들어간다.

---

## 비목표

- 학기 기준 조회 필터링 API: 설계하지 않는다.
- 기존 레코드 backfill: 기존 행의 `registration_year`, `semester`는 NULL로 방치한다.
- 게시글 수정(`updateTitle`) 시 학기 재계산: 수정 흐름에는 손대지 않는다.
- `SemesterType` 같은 별도 enum: semester 값이 1·2뿐이라 Integer로 충분하다.

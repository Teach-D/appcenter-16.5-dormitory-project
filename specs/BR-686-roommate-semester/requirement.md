# BR-686 룸메이트 체크리스트·게시글 등록 학기 저장

## 기능 요약

룸메이트 사전 체크리스트(`RoommateCheckList`)와 게시글(`RoommateBoard`) 생성 시,
서버 시계의 현재 월을 기준으로 등록 학기(년도 + 학기 번호)를 자동 계산해 각 엔티티에 저장한다.

---

## 동작 명세

**정상 흐름**

1. 클라이언트가 룸메이트 게시글 생성 API를 호출한다.
2. 서버는 현재 시각의 월(month)을 확인한다.
3. 월에 따라 학기를 계산한다:
   - 1월 또는 2월 → `semester = 1`, `year = 현재 년도`
   - 7월 또는 8월 → `semester = 2`, `year = 현재 년도`
   - 그 외 월 → `semester = null`, `year = null`
4. 계산된 `year` + `semester` 값을 `RoommateCheckList`와 `RoommateBoard` 양쪽에 모두 저장한다.
5. 응답 DTO에 `year`, `semester` 필드를 포함해 반환한다.

---

## 도메인 데이터

### RoommateCheckList 추가 필드

| 필드명 | 타입 | 설명 | 제약 |
|---|---|---|---|
| `year` | Integer | 등록 년도 (예: 2026) | nullable |
| `semester` | Integer | 학기 번호 (1 또는 2) | nullable |

### RoommateBoard 추가 필드

| 필드명 | 타입 | 설명 | 제약 |
|---|---|---|---|
| `year` | Integer | 등록 년도 | nullable |
| `semester` | Integer | 학기 번호 (1 또는 2) | nullable |

---

## 비즈니스 규칙 / 제약

- 학기 계산은 서버 시각 기준으로 생성 시점에 한 번만 수행한다.
- 클라이언트가 학기를 직접 전달하거나 수정하는 API는 제공하지 않는다.
- 기존 데이터(이 기능 배포 전 생성된 레코드)의 `year`, `semester`는 null이다.
- `semester` 값은 1 또는 2만 허용한다 (null 포함).

---

## 예외 · 경계 상황

- 1·2월, 7·8월 외 월에 생성된 경우 → `year = null`, `semester = null` (예외 없이 정상 저장)
- 1월/2월 동안 2개 이상 게시글 생성 가능 여부 → 기존 중복 방지 정책 그대로 유지 (이 명세 범위 외)

---

## 비목표 (Non-goals)

- 학기 기준 게시글 목록 필터링·조회 API 추가는 포함하지 않는다.
- 게시글 수정(`updateTitle`) 시 학기 재계산은 하지 않는다.
- 기존 레코드 backfill 마이그레이션은 포함하지 않는다.
- 3~6월, 9~12월 등록 허용/차단 여부 변경은 포함하지 않는다.

---

## 수용 기준 (Acceptance Criteria)

**AC-1: 1·2월 생성 시 1학기 저장**

- Given: 현재 월이 1월 또는 2월이고 현재 년도가 2026년
- When: 룸메이트 게시글 생성 API 호출
- Then: `RoommateCheckList.year = 2026`, `RoommateCheckList.semester = 1`
       `RoommateBoard.year = 2026`, `RoommateBoard.semester = 1`

**AC-2: 7·8월 생성 시 2학기 저장**

- Given: 현재 월이 7월 또는 8월이고 현재 년도가 2026년
- When: 룸메이트 게시글 생성 API 호출
- Then: `RoommateCheckList.year = 2026`, `RoommateCheckList.semester = 2`
       `RoommateBoard.year = 2026`, `RoommateBoard.semester = 2`

**AC-3: 그 외 월 생성 시 null 저장**

- Given: 현재 월이 1·2·7·8월이 아닌 임의의 월
- When: 룸메이트 게시글 생성 API 호출
- Then: `RoommateCheckList.year = null`, `RoommateCheckList.semester = null`
       `RoommateBoard.year = null`, `RoommateBoard.semester = null`

**AC-4: 응답 DTO에 학기 정보 포함**

- Given: 학기 정보가 저장된 게시글이 존재
- When: 게시글 조회 API 호출
- Then: 응답에 `year`, `semester` 필드가 올바른 값으로 포함됨

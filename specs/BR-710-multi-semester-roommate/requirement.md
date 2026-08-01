# BR-710 — 유저별 학기별 룸메이트 게시글·체크리스트 복수 보유

## 기능 요약

`RoommateCheckList`, `RoommateBoard`, `MyRoommate` 세 엔티티의 User 연관관계를 `@OneToOne` → `@ManyToOne`으로 변경하고, 고유 제약을 `(user, year, semester)` 복합 유니크로 전환한다. 이로써 한 유저가 학기마다 독립적인 게시글·체크리스트·룸메이트 이력을 보유할 수 있게 된다.

---

## 동작 명세

### 게시글·체크리스트 생성 (`POST /roommates`)
1. 현재 매칭 기간(`periodResolver.resolveCurrent`)으로 `year` + `semester` 계산
2. 동일 `(userId, year, semester)` 조합의 `RoommateCheckList`가 이미 존재하면 409 반환
3. 없으면 기존 로직 그대로 `RoommateCheckList` + `RoommateBoard` 생성

### 게시글·체크리스트 수정 (`PUT /roommates/{boardId}`)
- `boardId`로 `RoommateBoard`를 직접 조회
- 요청자 소유 확인 후 수정
- (기존 `PUT /roommates`에서 변경 — breaking change)

### 게시글·체크리스트 삭제 (`DELETE /roommates/{boardId}`)
- `boardId`로 `RoommateBoard`를 직접 조회
- 요청자 소유 확인 후 삭제
- (기존 `DELETE /roommates`에서 변경 — breaking change)

### 유사도 조회 (`GET /roommates/similar`, `GET /roommates/list/similar/scroll/me`)
- 내 게시글 조회를 `findByUserId` → `findByUserIdAndYearAndSemester(userId, currentYear, currentSemester)`로 변경

### MyRoommate 생성 (매칭 COMPLETED 시점)
- 매칭 완료 시 현재 `year` + `semester`를 `MyRoommate`에 함께 저장
- 동일 `(userId, year, semester)` 조합이 이미 존재하면 저장하지 않음(멱등 처리)

### MyRoommate 조회 (`GET /my-roommates/info`)
- `findByUserIdAndYearAndSemester(userId, currentYear, currentSemester)` 로 현재 학기 데이터 반환

### 채팅방 목록의 `isRoommate` 플래그
- `RoommateChattingRoomService.findRoommateChatRoomListByUser` 내부의 isRoommate 판단을 현재 학기 스코프로 제한
- 기존: `findByUserIdAndRoommateId(userId, partnerId)` — 전 학기 포함 검색
- 변경: `findByUserIdAndRoommateIdAndYearAndSemester(userId, partnerId, currentYear, currentSemester)` — 현재 학기만
- 결과: 이전 학기에 매칭됐던 상대와의 채팅방은 `isRoommate=false`로 표시됨

### `cancelMatching` — MyRoommate 삭제 스코프
- 기존: `deleteByUserIdAndRoommateId` — 두 유저 간 전 학기 행 일괄 삭제
- 변경: 현재 학기 `(userId, roommateId, year, semester)` 행만 삭제

---

## 도메인 데이터

### RoommateCheckList (변경)
| 필드 | 변경 전 | 변경 후 |
|------|---------|---------|
| user 연관관계 | `@OneToOne`, `user_id UNIQUE` | `@ManyToOne`, unique 제거 |
| 복합 유니크 | 없음 | `(user_id, registration_year, semester)` |

### RoommateBoard (변경)
| 필드 | 변경 전 | 변경 후 |
|------|---------|---------|
| user 연관관계 | `@OneToOne`, `user_id UNIQUE` | `@ManyToOne`, unique 제거 |
| 복합 유니크 | 없음 | `(user_id, registration_year, semester)` |

### MyRoommate (변경)
| 필드 | 변경 전 | 변경 후 |
|------|---------|---------|
| user 연관관계 | `@ManyToOne`, `member_id UNIQUE` | `@ManyToOne`, `member_id` unique 제거 |
| year | 없음 | `Integer registration_year` 추가 |
| semester | 없음 | `SemesterType semester` 추가 |
| 복합 유니크 | 없음 | `(member_id, registration_year, semester)` |

---

## 비즈니스 규칙 / 제약

1. **학기당 최대 1개**: 동일 `(user, year, semester)` 조합은 `RoommateCheckList`, `RoommateBoard`, `MyRoommate` 각각 하나만 존재할 수 있다.
2. **학기 경계**: year + semester는 항상 `periodResolver.resolveCurrent(LocalDate.now())`로 계산한다. 클라이언트가 직접 지정하지 않는다.
3. **수정·삭제 권한**: `board.getUser().getId().equals(userId)` 검증. 소유자가 아니면 403.
4. **기존 레거시 데이터**: `year`/`semester`가 null인 기존 행은 조회 시 `isCurrentPeriod = false`로 처리하며, 생성 중복 검사 대상에서 제외한다(null ≠ 현재 학기).
5. **MyRoommate 멱등**: 같은 `(user, year, semester)` 조합이 이미 존재하면 중복 생성하지 않고 기존 행을 재사용한다.

---

## 예외 · 경계 상황

| 상황 | 처리 |
|------|------|
| 현재 학기에 이미 게시글이 있는 유저가 `POST /roommates` 요청 | 409 CONFLICT (`ROOMMATE_BOARD_ALREADY_EXISTS` 또는 기존 `ErrorCode`) |
| `PUT/DELETE /roommates/{boardId}` — boardId 없음 | 404 NOT_FOUND |
| `PUT/DELETE /roommates/{boardId}` — 소유자 불일치 | 403 FORBIDDEN |
| 현재 학기에 내 게시글이 없는데 유사도 조회 | 404 (`ROOMMATE_BOARD_NOT_FOUND`) |
| 현재 학기에 `MyRoommate`가 없는데 조회 | 404 (`MY_ROOMMATE_NOT_REGISTERED`) |
| MyRoommate 생성 시 동일 학기 이미 존재 | 멱등 처리 (예외 없이 기존 반환) |

---

## 비목표 (Non-goals)

- 과거 학기 게시글의 수정·삭제 지원 (현재 학기 것만 수정·삭제)
- 학기별 게시글 목록 API 신규 추가 (기존 스크롤 조회에 `year/semester` 필터가 이미 있음)
- MyRoommate 규칙(`rule`) 학기별 분리 (규칙은 현재 학기 MyRoommate에 그대로 연결)
- RoommateMatching 엔티티 변경
- RoommateNotificationFilter 변경

---

## 수용 기준 (Acceptance Criteria)

### RoommateCheckList / RoommateBoard

- **AC-1** Given 1학기에 게시글이 없는 유저 A, When `POST /roommates`, Then 201 반환 + DB에 (A, 현재year, 현재semester) 행 생성
- **AC-2** Given 1학기 게시글이 있는 유저 A, When 같은 학기에 `POST /roommates`, Then 409 반환
- **AC-3** Given 1학기 게시글이 있는 유저 A, When 2학기(다음 OPEN 기간)에 `POST /roommates`, Then 201 반환 + 두 번째 행 생성
- **AC-4** Given 1학기 boardId=10 소유자 A, When `PUT /roommates/10` 요청, Then 200 반환 + 해당 행 수정
- **AC-5** Given boardId=10 소유자가 A인데 B가 `PUT /roommates/10`, Then 403 반환
- **AC-6** Given 1학기 boardId=10 소유자 A, When `DELETE /roommates/10`, Then 204 반환 + 해당 행·체크리스트 삭제
- **AC-7** Given 현재 학기에 내 게시글이 있는 유저 A, When `GET /roommates/similar`, Then 현재 학기 내 체크리스트 기준 유사도 목록 반환
- **AC-8** Given 현재 학기에 내 게시글이 없는 유저 A, When `GET /roommates/similar`, Then 404 반환

### MyRoommate

- **AC-9** Given 1학기에 매칭 COMPLETED 이벤트 발생, Then `MyRoommate`에 (user, year=1학기year, semester=FIRST) 행 저장
- **AC-10** Given 1학기 MyRoommate가 있는 유저, When 2학기에 매칭 COMPLETED, Then 두 번째 MyRoommate 행 저장 (1학기 행 유지)
- **AC-11** Given 현재 학기 MyRoommate가 있는 유저, When `GET /my-roommates/info`, Then 현재 학기 룸메이트 정보 반환
- **AC-12** Given 동일 (user, year, semester) MyRoommate가 이미 존재, When 다시 생성 시도, Then 예외 없이 기존 행 재사용

### isRoommate / cancelMatching 스코프

- **AC-13** Given A가 1학기에 B와 매칭, 2학기에 C와 매칭됨, When 채팅방 목록 조회(현재=2학기), Then B와의 채팅방 `isRoommate=false`, C와의 채팅방 `isRoommate=true`
- **AC-14** Given A가 2학기에 C와 매칭, When `cancelMatching` 실행, Then `MyRoommate(A,C,2학기)` 행만 삭제, `MyRoommate(A,B,1학기)` 행은 유지

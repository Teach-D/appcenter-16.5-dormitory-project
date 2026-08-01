# BR-710 — 도메인 설계

---

## 엔티티 / 값 객체

### RoommateCheckList (수정)
| 필드 | 타입 | 제약 | 변경 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 유지 |
| user | User | FK `user_id`, NOT NULL | **@OneToOne → @ManyToOne** |
| year | Integer | `registration_year` | 유지 |
| semester | SemesterType | INT (converter) | 유지 |
| 복합 유니크 | — | `(user_id, registration_year, semester)` | **신규 추가** |
| 나머지 필드 | — | — | 유지 |

### RoommateBoard (수정)
| 필드 | 타입 | 제약 | 변경 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 유지 |
| user | User | FK `user_id` | **@OneToOne → @ManyToOne** |
| year | Integer | `registration_year` | 유지 |
| semester | SemesterType | INT (converter) | 유지 |
| 복합 유니크 | — | `(user_id, registration_year, semester)` | **신규 추가** |
| 나머지 필드 | — | — | 유지 |

### MyRoommate (수정)
| 필드 | 타입 | 제약 | 변경 |
|------|------|------|------|
| id | Long | PK, AUTO_INCREMENT | 유지 |
| user | User | FK `member_id`, unique 제거 | **unique=true 제거** |
| roommate | User | FK `roommate_id`, unique 유지 | 유지 |
| year | Integer | `registration_year`, nullable | **신규 컬럼** |
| semester | SemesterType | INT (SemesterTypeConverter), nullable | **신규 컬럼** |
| 복합 유니크 | — | `(member_id, registration_year, semester)` | **신규 추가** |
| rule | List\<String\> | — | 유지 |

### User (수정)
| 필드 | 변경 |
|------|------|
| `@OneToOne(mappedBy="user") RoommateCheckList roommateCheckList` | **제거** |
| `@OneToOne(mappedBy="user") RoommateBoard roommateBoard` | **제거** |
| `hasRoommateCheckList()` 메서드 | **제거** |
| `update()` 내 `roommateCheckList.syncUserInfo()` 호출 | **제거** (UserService로 이동) |

---

## 애그리거트 경계

- `RoommateBoard` ↔ `RoommateCheckList`: **Board가 Checklist를 @OneToOne으로 참조** (변경 없음). Board가 루트이고, Checklist는 Board를 통해 접근.
- `MyRoommate`: 독립 애그리거트. User 두 명과 연관 + 학기 정보 보유.
- **User는 더 이상 RoommateCheckList/RoommateBoard를 직접 참조하지 않는다.** 필요하면 repository를 통해 조회.

---

## 연관관계

```
User ─── (ManyToOne) ──► RoommateCheckList   (user 1 : checklist N)
User ─── (ManyToOne) ──► RoommateBoard        (user 1 : board N)
User ─── (ManyToOne) ──► MyRoommate.user      (user 1 : myRoommate N)
User ─── (OneToOne)  ──► MyRoommate.roommate  (변경 없음)

RoommateBoard ─── (@OneToOne) ──► RoommateCheckList  (변경 없음)
```

fetch 전략: 모두 `FetchType.LAZY` 유지.

연관관계 주인: `RoommateCheckList.user`, `RoommateBoard.user` (FK 보유측).

---

## DB 스키마 변경

### 수동 실행 필요 (ddl-auto=update는 기존 제약 DROP 불가)

```sql
-- 1. 기존 UNIQUE 제약 이름 조회
SELECT TABLE_NAME, CONSTRAINT_NAME
FROM information_schema.TABLE_CONSTRAINTS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME IN ('roommate_check_list', 'roommate_board', 'my_roommate')
  AND CONSTRAINT_TYPE = 'UNIQUE';

-- 2. FK 임시 제거 (유니크 인덱스가 FK에 묶여 있어 직접 DROP 불가)
ALTER TABLE my_roommate         DROP FOREIGN KEY FKqanysibkmn5ybq9r8b25u6ut3;
ALTER TABLE roommate_board      DROP FOREIGN KEY FKmbwldrt009uqb703dbdp3v99k;
ALTER TABLE roommate_check_list DROP FOREIGN KEY FKcqsgrm7fdy0glsdn69x9fo904;

-- 3. 기존 user_id / member_id 단독 UNIQUE 제거
ALTER TABLE my_roommate         DROP INDEX UKd8trq76dkmm9avodgq6rjdlxh;  -- member_id
ALTER TABLE roommate_board      DROP INDEX UK65faxv4e0immtb8ihq108c7xr;  -- user_id
ALTER TABLE roommate_check_list DROP INDEX UKp4cqpp1nxdxlkn7stgum35yg8;  -- user_id

-- 4. FK 재추가 (일반 인덱스로)
ALTER TABLE my_roommate         ADD CONSTRAINT FKqanysibkmn5ybq9r8b25u6ut3 FOREIGN KEY (member_id) REFERENCES user(id);
ALTER TABLE roommate_board      ADD CONSTRAINT FKmbwldrt009uqb703dbdp3v99k FOREIGN KEY (user_id)   REFERENCES user(id);
ALTER TABLE roommate_check_list ADD CONSTRAINT FKcqsgrm7fdy0glsdn69x9fo904 FOREIGN KEY (user_id)   REFERENCES user(id);

-- 5. my_roommate 신규 컬럼 추가 (ddl-auto=update가 자동 추가하므로 이미 존재하면 생략)
ALTER TABLE my_roommate
    ADD COLUMN registration_year INT NULL,
    ADD COLUMN semester          INT NULL;

-- 6. 복합 UNIQUE 추가 (ddl-auto=update가 @Table uniqueConstraints 읽어 자동 추가하므로 이미 존재하면 생략)
ALTER TABLE roommate_check_list
    ADD CONSTRAINT uq_checklist_user_period  UNIQUE (user_id, registration_year, semester);
ALTER TABLE roommate_board
    ADD CONSTRAINT uq_board_user_period      UNIQUE (user_id, registration_year, semester);
ALTER TABLE my_roommate
    ADD CONSTRAINT uq_myroommate_user_period UNIQUE (member_id, registration_year, semester);
```

> **실제 적용 결과 (2026-08-02)**: 앱 기동 시 `ddl-auto=update`가 컬럼(5단계)·복합 유니크(6단계)를 자동 추가함. FK DROP → 단독 유니크 DROP → FK 재추가(2~4단계)만 수동 실행 필요했음.

> **레거시 NULL 데이터**: `registration_year`/`semester`가 NULL인 기존 행은 NULL ≠ NULL 특성으로 복합 유니크 충돌 없이 공존한다.

---

## 도메인 계층 구조

### 신규 생성 없음. 수정 대상 클래스 목록:

```
domain/roommate/
├── entity/
│   ├── RoommateCheckList.java         수정  (@ManyToOne, @Table uniqueConstraints 추가)
│   ├── RoommateBoard.java             수정  (@ManyToOne, @Table uniqueConstraints 추가)
│   └── MyRoommate.java                수정  (unique 제거, year/semester 필드 추가, @Table uniqueConstraints)
│
├── repository/
│   ├── RoommateCheckListRepository.java   수정  (existsByUserIdAndYearAndSemester 추가)
│   ├── RoommateBoardRepository.java       수정  (findByUserIdAndYearAndSemester, existsByUserIdAndYearAndSemester, findAllByUserIdOrderByIdDesc 추가)
│   └── MyRoommateRepository.java          수정  (findByUserIdAndYearAndSemester, findByUserIdAndRoommateIdAndYearAndSemester, deleteByUserIdAndRoommateIdAndYearAndSemester 추가)
│
├── service/
│   ├── RoommateService.java           수정  (중복 생성 409 체크, update/delete boardId 파라미터 변경, 유사도 조회 semester 스코프)
│   ├── RoommateMatchingService.java   수정  (registerMyRoommate year/semester 추가, cancelMatching semester 스코프 삭제, getRoommateBoard → repository 조회)
│   ├── RoommateChattingRoomService.java 수정 (createChatRoom checklist 조회 변경, isRoommate semester 스코프, findByUserId → board 직접 참조)
│   ├── MyRoommateService.java         수정  (모든 findByUserId → semester 스코프)
│   ├── QuickMessageService.java       수정  (findByUserId → semester 스코프)
│   └── RoommateQueryService.java      수정  (findByUserId → findAllByUserIdOrderByIdDesc, 다학기 지원)
│
└── controller/
    ├── RoommateController.java        수정  (PUT /roommates/{boardId}, DELETE /roommates/{boardId})
    └── RoommateApiSpecification.java  수정  (PUT/DELETE 시그니처 변경)

domain/user/
└── entity/
    └── User.java                      수정  (roommateCheckList, roommateBoard 필드·메서드 제거)

domain/user/
└── service/
    └── UserService.java               수정  (hasRoommateCheckList → repository 직접 조회로 교체, update()에서 syncUserInfo 분리)
```

---

## 핵심 로직 변경 상세

### 1. 중복 생성 방지 (`RoommateService.createRoommateCheckListandBoard`)
```
기존: 없음
변경: period 계산 후 existsByUserIdAndYearAndSemester → true면 ROOMMATE_BOARD_ALREADY_EXISTS(409) 던짐
```

### 2. 수정/삭제 API 파라미터 (`RoommateService`)
```
기존: updateRoommateChecklistAndBoard(dto, userId)     — findByUserId로 board 탐색
변경: updateRoommateChecklistAndBoard(dto, boardId, userId) — findById(boardId)로 board 탐색
```
```
기존: deleteRoommateBoard(userId)     — findByUserId로 board 탐색
변경: deleteRoommateBoard(boardId, userId) — findById(boardId)로 board 탐색
```

### 3. 유사도 조회 내 "내 게시글" 탐색 (`RoommateService`)
```
기존: roommateBoardRepository.findByUserId(userId)
변경: roommateBoardRepository.findByUserIdAndYearAndSemester(userId, currentYear, currentSemester)
```

### 4. MyRoommate 생성 (`RoommateMatchingService.registerMyRoommate`)
```
기존: MyRoommate.builder().user(sender).roommate(receiver).build()
변경: + year(currentYear).semester(currentSemester) 추가
     + sender/receiver.getRoommateBoard() 제거 → roommateBoardRepository.findByUserIdAndYearAndSemester 로 교체
```

### 5. 매칭 취소 삭제 스코프 (`RoommateMatchingService.cancelMatching`)
```
기존: deleteByUserIdAndRoommateId(senderId, receiverId)  — 전학기 일괄 삭제
변경: deleteByUserIdAndRoommateIdAndYearAndSemester(...)  — 현재 학기만 삭제
     + sender/receiver.getRoommateBoard() 제거 → repository 조회로 교체
```

### 6. 채팅방 생성 시 checklist 조회 (`RoommateChattingRoomService.createChatRoom`)
```
기존: host.getRoommateCheckList(), guest.getRoommateCheckList()
변경: host checklist  → roommateBoard.getRoommateCheckList()  (board가 이미 FK 보유)
     guest checklist → roommateCheckListRepository.findFirstByUserIdAndYearAndSemester(guestId, year, semester)
```

### 7. isRoommate 플래그 (`RoommateChattingRoomService.findRoommateChatRoomListByUser`)
```
기존: myRoommateRepository.findByUserIdAndRoommateId(userId, partnerId).isPresent()
변경: myRoommateRepository.findByUserIdAndRoommateIdAndYearAndSemester(userId, partnerId, currentYear, currentSemester).isPresent()
```

### 8. 채팅방 목록의 guestBoardTitle (`RoommateChattingRoomService.findRoommateChatRoomListByUser`)
```
기존: roommateBoardRepository.findByUserId(guest.getId()).map(RoommateBoard::getTitle).orElse(null)
변경: room.getRoommateBoard() != null ? room.getRoommateBoard().getTitle() : null  (board가 채팅방에 이미 연결)
```

### 9. hasRoommateCheckList (`UserService`)
```
기존: user.hasRoommateCheckList()  (User.roommateCheckList 필드 직접 접근)
변경: roommateCheckListRepository.existsByUserIdAndYearAndSemester(userId, currentYear, currentSemester)
```

### 10. User.update() 내 syncUserInfo (`UserService`)
```
기존: User.update()에서 this.roommateCheckList.syncUserInfo(...)
변경: User.update()에서 해당 호출 제거
     UserService.update()에서 별도로 현재학기 checklist 조회 후 syncUserInfo 호출
```

---

## 비목표

- 새 클래스·파일 생성 없음 (모두 기존 파일 수정)
- `RoommateNotificationFilter` 변경 없음
- `RoommateMatching` 엔티티 변경 없음
- 과거 학기 게시글 수정·삭제 API 추가 없음
- `RoommateChattingRoom` 엔티티 구조 변경 없음
- `RoommateQueryService.findLikedByUser` 변경 없음 (좋아요는 board 기준이므로 다학기 자연 지원)

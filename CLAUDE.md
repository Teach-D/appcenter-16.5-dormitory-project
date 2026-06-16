# UniDorm — CLAUDE.md

## 참조 문서

| 파일 | 읽는 시점 |
|------|----------|
| `docs/api-spec.md` | API 추가/수정, DTO 설계, 권한 확인 |
| `docs/db-schema.md` | 테이블 구조, FK, 컬럼 제약조건, 마이그레이션 작성 |
| `docs/architecture.md` | 기능 흐름, 외부 시스템 연동, 스케줄러/비동기 패턴 확인 (§14 작업 체크리스트) |
| `docs/github.md` | PR/이슈 작성, 브랜치 네이밍, 커밋 메시지 |
| `docs/domain-dependencies.json` | X 변경 시 영향받는 도메인 확인 |

## 코딩 규칙

코드 작성 전 항상 확인:
- `.claude/rules/antipatterns.md` — Spring 아키텍처, Lombok/엔티티, API/예외
- `.claude/rules/antipatterns-jpa.md` — JPA, N+1, 트랜잭션, QueryDSL

## 명령어

```bash
./gradlew build              # 빌드
./gradlew test               # 테스트
./gradlew compileJava        # QueryDSL Q클래스 생성
./gradlew clean compileJava  # Q클래스 초기화
./gradlew flywayInfo         # 마이그레이션 상태 확인
```

## 네이밍 규칙

`{Domain}Controller` + `{Domain}ApiSpecification` (Swagger 인터페이스 분리) · `{Domain}Service` · `{Entity}Repository` · `Request{Action}{Entity}Dto` / `Response{Entity}Dto` · `{Name}Type` / `{Name}Status` · DB 컬럼 snake_case

## 인증/보안

- 권한 3가지: `USER`, `ADMIN`, `DORMITORY`
- 새 공개 경로 추가 시 `SecurityConfig.java`의 `permitAll()` 목록에 반드시 추가
- Oracle DB: `@OracleRepository` qualifier — `global/config/OracleConfig.java`

## 도메인별 함정 (non-obvious)

| 도메인 | 함정 |
|--------|------|
| `calender` | 오탈자 아님 — DB·엔티티·패키지 모두 `calender`(e 하나). `calendar`로 쓰면 불일치 |
| `fcm` | `@Async` 메서드에 `@Transactional` 필수 (별도 스레드 → 컨텍스트 없음 → LazyInit 예외). `@Modifying`에도 `@Transactional` 필수 |
| `coupon` | 재고 차감 시 반드시 `findByIdWithLock`(비관적 락). ADMIN에게 발급 금지 로직 필수 |
| `roommate` | `RoommateCheckList` → `RoommateBoard` 순서로 생성. `MyRoommate`는 양방향(user_id·roommate_id 각각 UQ) → 두 row 동시 생성 |
| `survey` | 응답 시 상태(OPEN) + 날짜 범위(start_date ≤ now ≤ end_date) 이중 검증 필수 |
| `notification` | 발송 전 `User.receiveNotificationTypes` 필터링 필수. DORMITORY 타입만 공지 수신 |
| `announcement` | FCM 전체 발송 시 `bulkEnqueueOutbox` 패턴 사용 (N+1 방지) |
| `complaint` | 답변 첨부파일은 `crawled_announcement_file` 테이블 공유 |
| `tip` | 댓글 soft-delete(`is_deleted=true`). `deleteById()` 금지 |
| `report` | `ResponseReportDto` 금지 → `ResponseEntity<Void>` + HTTP 201 |
| `feature` | 미등록 key → 예외 아닌 `false` 반환 (`orElseThrow` 금지) |

## 완료 체크리스트

1. **컴파일** — PostToolUse 훅이 `.java` 수정 시 자동 실행 (실패 시 즉시 수정)
2. **테스트** — asyncRewake 훅이 백그라운드 자동 실행 (실패 시 Claude에 알림)
3. **antipatterns 자가 검토** — `.claude/rules/antipatterns.md`, `antipatterns-jpa.md` 확인
4. **AI 기여도 기록** — PR 완료 시 `.claude/ai-metrics.md`에 보완비율·clarification 횟수 기록

## 세션 실패 기록
### 2026-06-16 06:19 UTC (세션: 5cd535ff)

- `Bash(ls "C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-projec)` → `Exit code 2`
- `Bash(gh auth status 2>&1)` → `Exit code 127`
- `Read(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\.c)` → `<tool_use_error>Cancelled: parallel tool call Bash(gh auth status 2>&1) errored</tool_use_error>`
- `Bash(powershell -Command "gh auth status 2>&1")` → `Exit code 1`
- `Read(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\.c)` → `<tool_use_error>Cancelled: parallel tool call Bash(powershell -Command "gh auth status 2>&1…) errored</tool_use_error>`
- `Write(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `<tool_use_error>File has not been read yet. Read it first before writing to it.</tool_use_error>`
- `Write(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `PreToolUse:Write hook error: [bash .claude/hooks/check-checkout.sh]: No stderr output`
- `Edit(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `PreToolUse:Edit hook error: [bash .claude/hooks/check-checkout.sh]: No stderr output`

### 2026-06-15 22:44 UTC (세션: 0d77f741)

- `Bash(gh pr list --head feat/openchat-image-message-639 --base dev 2>&1)` → `Exit code 127`
- `Read(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `File does not exist. Note: your current working directory is C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project.`

### 2026-06-15 07:21 UTC (세션: 97b01754)

- `Read(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `File does not exist. Note: your current working directory is C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project.`
- `Bash(gh issue list --repo Teach-D/appcenter-16.5-dormitory-project --limit 5 2>&1)` → `Exit code 127`
- `Bash(cd "C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-projec)` → `Exit code 2`
- `Bash(cd "C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-projec)` → `Exit code 127`
- `Bash(cd "C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-projec)` → `Exit code 2`

### 2026-06-15 06:59 UTC (세션: 97b01754)

- `Read(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `File does not exist. Note: your current working directory is C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project.`
- `Bash(gh issue list --repo Teach-D/appcenter-16.5-dormitory-project --limit 5 2>&1)` → `Exit code 127`
- `Bash(cd "C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-projec)` → `Exit code 2`
- `Bash(cd "C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-projec)` → `Exit code 127`

### 2026-06-14 09:54 UTC (세션: 98d8f513)

- `Write(C:\Users\wkadh\OneDrive\바탕 화면\coding\project\appcenter-16.5-dormitory-project\do)` → `<tool_use_error>File has not been read yet. Read it first before writing to it.</tool_use_error>`
- `Bash(gh auth status 2>&1)` → `Exit code 127`
- `Bash(git status --porcelain 2>&1)` → `<tool_use_error>Cancelled: parallel tool call Bash(gh auth status 2>&1) errored</tool_use_error>`
- `Agent(학번 공개 기능 구현)` → `The user doesn't want to proceed with this tool use. The tool use was rejected (eg. if it was a file edit, the new_string was NOT written to`

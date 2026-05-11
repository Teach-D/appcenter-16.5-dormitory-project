# UniDorm 프로젝트 - Claude Code 가이드

## 상세 스펙 문서 (docs/)

코딩 시 아래 내용과 관련된 작업이 있으면 해당 파일을 참조할 것.

| 파일 | 참조 시점 |
|------|----------|
| `docs/api-spec.md` | API 엔드포인트 추가/수정, DTO 설계, 권한 확인 |
| `docs/db-schema.md` | 테이블 구조 확인, FK 관계, 컬럼 제약조건, 마이그레이션 작성 |
| `docs/architecture.md` | 기능 흐름 파악, 외부 시스템 연동, 스케줄러/비동기 패턴 확인 |
| `docs/github.md` | PR/이슈 작성, 브랜치 네이밍, 커밋 메시지, CI/CD |
| `docs/domain-dependencies.json` | "X 변경 시 영향받는 도메인" 확인, 의존 관계 쿼리 |

## 코딩 규칙 (.claude/rules/)

코드 작성 시 아래 규칙 파일을 참조할 것.

| 파일 | 참조 시점 |
|------|----------|
| `.claude/rules/antipatterns.md` | 코드 작성 전 항상 확인 (Spring 아키텍처, Lombok/엔티티, API/예외) |
| `.claude/rules/antipatterns-jpa.md` | JPA 엔티티/쿼리 작성 시 (N+1, 트랜잭션, QueryDSL) |

## 프로젝트 개요

**UniDorm** - 인천대학교 기숙사 통합 앱/웹 서비스
- Spring Boot 3.4.4 / Java 17
- 기숙사생을 위한 공지, 민원, 룸메이트 매칭, 공동구매, 알림, 쿠폰 관리 서비스

## 기술 스택

Spring Boot 3.4.4 / Java 17 · MySQL(primary) + Oracle(학교DB) + Redis · JWT · WebSocket(STOMP) · FCM · QueryDSL 5 · Flyway · Selenium

## 패키지 구조

```
com.example.appcenter_project
├── domain/          # 13개 비즈니스 도메인
│   └── {domain}/
│       ├── controller/   # REST + ApiSpecification (Swagger 인터페이스)
│       ├── service/
│       ├── entity/
│       ├── repository/   # JpaRepository + QuerydslRepository
│       ├── dto/
│       │   ├── request/
│       │   └── response/
│       └── enums/
├── common/          # 공통 컴포넌트 (file, image, like, metrics)
├── global/          # 인프라 설정 (SecurityConfig, JwtFilter, 예외처리)
└── shared/          # 공통 enum, utils
```

### 도메인 목록
`user`, `announcement`, `complaint`, `groupOrder`, `roommate`, `notification`, `calender`, `fcm`, `coupon`, `feature`, `report`, `survey`, `tip`

### 핵심 진입점
- 메인 클래스: `src/main/java/com/example/appcenter_project/AppcenterProjectApplication.java`
- 보안 설정: `global/config/SecurityConfig.java`
- MySQL 설정: `global/config/MySqlConfig.java` (Primary)

## 도메인 작업 빠른 진입

자주 수정되는 도메인의 핵심 파일과 수정 패턴:

| 도메인 | 핵심 파일 | 주의사항 |
|--------|-----------|----------|
| `fcm` | `FcmMessageService`, `FcmOutboxProcessor`, `FcmTokenRepository` | @Async 메서드 → @Transactional 필수, @Modifying → @Transactional 필수 |
| `groupOrder` | `GroupOrderService`, `GroupOrderQuerydslRepositoryImpl`, `GroupOrderRepository` | 목록 조회 N+1 주의, 복잡한 조건은 QuerydslRepositoryImpl에 구현 |
| `announcement` | `AnnouncementNotificationService`, `AnnouncementCrawlScheduler` | FCM 전체 발송 시 bulkEnqueueOutbox 패턴 사용 (N+1 방지) |
| `complaint` | `ComplaintService`, `ComplaintQuerydslRepositoryImpl` | 답변 첨부파일은 `crawled_announcement_file` 테이블 공유 |
| `user` | `UserService`, `SecurityConfig` | 새 공개 경로 추가 시 SecurityConfig.permitAll() 목록 수정 필수 |
| `roommate` | `RoommateService`, `RoommateMatchingService` | RoommateCheckList → RoommateBoard 순서로 생성. MyRoommate는 양방향(user_id·roommate_id 각각 UQ) → 두 row 동시 생성 |
| `coupon` | `CouponService`, `CouponLocalCache` | 재고 차감 시 반드시 `findByIdWithLock`(비관적 락) 사용. ADMIN에게 발급 금지 로직 필수 |
| `survey` | `SurveyService` | 응답 전 상태(OPEN) + 날짜 범위(start_date ≤ now ≤ end_date) 이중 검증 필수 |
| `notification` | `NotificationService` | 발송 전 User.receiveNotificationTypes 필터링 필수. DORMITORY 타입만 공지 수신 |
| `tip` | `TipService` | 댓글 soft-delete 패턴(is_deleted=true) 사용. 실제 삭제 금지 |
| `calender` | `CalenderService` | 테이블명 오탈자 주의: `calender`(e 하나) — DB·엔티티·서비스 모두 동일 |
| `feature` | `FeatureService` | 기능 플래그 ON/OFF 토글. `feature_key`로 조회 후 `flag` boolean 확인 |
| `report` | `ReportService` | 신고 접수만 저장, 관리자가 별도 처리. 응답 DTO 없이 void 반환 |

## 네이밍·작업 레시피

네이밍 규칙: `{Domain}Controller` + `{Domain}ApiSpecification` (Swagger 분리) · `{Domain}Service` · `{Entity}Repository` · `Request{Action}{Entity}Dto` · `Response{Entity}Dto` · `{Name}Type` / `{Name}Status` · DB 컬럼 snake_case

작업 유형별 수정 파일 체크리스트 → `docs/architecture.md` §14 참조

## 주요 명령어

```bash
./gradlew build          # 빌드
./gradlew test           # 테스트
./gradlew compileJava    # QueryDSL Q클래스 생성
./gradlew clean compileJava  # Q클래스 초기화
./gradlew bootRun        # 로컬 실행 (application.yml 필요)
./gradlew flywayInfo     # 마이그레이션 상태 확인
```

## 인증/보안 패턴

- JWT 필터: `global/security/jwt/` · 권한: `USER`, `ADMIN`, `DORMITORY`
- 공개 경로: `SecurityConfig.java`의 `permitAll()` 목록에 추가
- 현재 사용자: `@AuthenticationPrincipal` 또는 `SecurityContextHolder`
- Oracle Repository: `@OracleRepository` qualifier (`global/config/OracleConfig.java`)

## 코드 작성 시 주의사항

1. **QueryDSL**: 복잡한 조회는 `*QuerydslRepositoryImpl`에 구현
2. **@Async**: `AsyncConfig`의 스레드풀 사용, 내부에서 LAZY 로딩 시 `@Transactional` 추가 필수
3. **@Modifying**: JpaRepository의 @Modifying 쿼리에는 반드시 `@Transactional` 추가
4. **캐싱**: Redis 우선, 로컬 Caffeine 폴백

## TDD · Flyway

TDD: 테스트 먼저(Red→Green→Refactor) · 위치: `src/test/.../domain/{domain}/{Class}Test.java` · `@ExtendWith(MockitoExtension.class)` + `@Mock` / `@InjectMocks`  
Flyway: `src/main/resources/db/migration/V{n}__{설명}.sql` · 버전 단조 증가 · **적용된 파일 절대 수정 금지**

## 바이브 코딩 완료 체크리스트

코딩 작업을 마치기 전 반드시 수행:

1. **컴파일** — PostToolUse 훅이 .java 수정 시 자동 실행 (실패 시 즉시 수정)
2. **테스트** — 도메인 테스트 asyncRewake 훅이 백그라운드 자동 실행 (실패 시 Claude에 알림)
3. **antipatterns 자가 검토** — `.claude/rules/antipatterns.md`, `antipatterns-jpa.md` 기준 확인
4. **AI 기여도 기록** — PR 완료 시 `.claude/ai-metrics.md` 에 보완비율·clarification 횟수 기록

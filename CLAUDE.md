# UniDorm 프로젝트 - Claude Code 가이드

## 프로젝트 개요

**UniDorm** - 인천대학교 기숙사 통합 앱/웹 서비스
- Spring Boot 3.4.4 / Java 17
- 기숙사생을 위한 공지, 민원, 룸메이트 매칭, 공동구매, 알림, 쿠폰 관리 서비스

## 기술 스택

| 분류 | 기술 |
|------|------|
| Framework | Spring Boot 3.4.4, Spring Security, Spring Data JPA |
| ORM | Hibernate 6, QueryDSL 5.0.0, MyBatis |
| DB | MySQL (primary), Oracle (학교 DB), Redis (캐시) |
| Auth | JWT (Access: 24h, Refresh: 30일) |
| 실시간 | WebSocket (STOMP) |
| 알림 | Firebase FCM |
| 문서 | SpringDoc OpenAPI 2.7.0 (Swagger) |
| 기타 | Selenium (공지 크롤링), Resilience4j (Circuit Breaker), Flyway (마이그레이션) |

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

## 네이밍 컨벤션

- **Controller**: `{Domain}Controller` + `{Domain}ApiSpecification` (Swagger 인터페이스 분리)
- **Service**: `{Domain}Service`
- **Repository**: `{Entity}Repository`, `{Entity}QuerydslRepositoryImpl`
- **Entity**: 단수 명사 (e.g., `User`, `GroupOrder`)
- **Request DTO**: `Request{Action}{Entity}Dto` (e.g., `RequestCreateComplaintDto`)
- **Response DTO**: `Response{Entity}Dto` (e.g., `ResponseComplaintDto`)
- **Enum**: `{Name}Type`, `{Name}Status`
- **DB 컬럼/테이블**: snake_case

## 주요 명령어

```bash
# 빌드
./gradlew build

# 테스트 실행
./gradlew test

# 특정 테스트만 실행
./gradlew test --tests "com.example.appcenter_project.*"

# QueryDSL Q클래스 생성
./gradlew compileJava

# Q클래스 초기화
./gradlew clean compileJava

# 로컬 실행 (application.yml 필요)
./gradlew bootRun
```

## DB 마이그레이션 (Flyway)

- 파일 위치: `src/main/resources/db/migration/`
- 네이밍: `V{버전}__{설명}.sql` (e.g., `V3__add_tip_table.sql`)
- 로컬에서는 `flyway.enabled=false` (ddl-auto: create 사용)
- 프로덕션에서는 Flyway 활성화

## 인증/보안 패턴

- JWT 필터: `global/security/jwt/`
- 권한: `USER`, `ADMIN`, `DORMITORY`
- 공개 경로 예외: `SecurityConfig.java`의 `permitAll()` 목록에 추가
- 현재 사용자 조회: `SecurityContextHolder` 또는 `@AuthenticationPrincipal`

## 엔티티 작성 규칙

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "table_name")
public class EntityName extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 정적 팩토리 메서드 사용
    public static EntityName create(...) { ... }

    // update 메서드 엔티티 내부 정의
    public void update(...) { ... }
}
```

## API 응답 패턴

- 성공: `ResponseEntity<ResponseDto>` 또는 직접 DTO 반환
- 예외: `global/exception/` 의 커스텀 예외 사용
- Swagger: `ApiSpecification` 인터페이스에 `@Operation`, `@ApiResponse` 정의

## 멀티 데이터소스

- MySQL 설정: `global/config/MySqlConfig.java` (`@Primary`)
- Oracle 설정: `global/config/OracleConfig.java`
- Oracle용 Repository는 별도 패키지로 분리하고 `@OracleRepository` qualifier 사용

## CI/CD

- **dev 브랜치**: `dev-deploy.yml` → 포트 8055, `inu-network`
- **main 브랜치**: `main-deploy.yml` → 포트 8056, `unidorm-network`
- Docker Hub 이미지로 빌드 후 SSH 배포

## 코드 작성 시 주의사항

1. **Lombok**: `@Builder` 대신 정적 팩토리 메서드 선호
2. **QueryDSL**: 복잡한 조회는 `*QuerydslRepositoryImpl`에 구현
3. **비동기**: `AsyncConfig`의 스레드풀 사용 (`@Async`)
4. **캐싱**: Redis 우선, 로컬 Caffeine 폴백
5. **테스트**: `src/test/java/` 에 서비스 단위 테스트 작성
6. **AOP 메트릭**: API 엔드포인트에 `@TrackApi` 어노테이션 추가

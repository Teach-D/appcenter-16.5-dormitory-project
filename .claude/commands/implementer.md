다음 기능을 이 프로젝트에 구현해줘: $ARGUMENTS

## 구현 프로세스

### 1단계: 코드 분석
- 요구사항과 관련된 도메인 파악
- 기존 유사 기능 코드 읽기 (참고할 패턴 파악)
- 영향받는 파일 목록 파악

### 2단계: 설계 확인 (구현 전 출력)
```
## 구현 계획
- 도메인: {domain}
- 생성 파일: [목록]
- 수정 파일: [목록]
- DB 변경: 있음/없음
- 주요 로직: [간략 설명]
```

### 3단계: 구현

**엔티티 패턴**
```java
@Entity @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "table_name")
public class EntityName extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public static EntityName create(...) { ... }  // 정적 팩토리
    public void update(...) { ... }                // 수정 메서드
}
```

**Service 패턴**
```java
@Service @RequiredArgsConstructor @Transactional
public class DomainService {
    @Transactional(readOnly = true)
    public ResponseDto find(...) { ... }

    public ResponseDto save(...) { ... }
}
```

**Controller 패턴**
```java
@RestController @RequestMapping("/endpoint") @RequiredArgsConstructor
public class DomainController implements DomainApiSpecification {
    @TrackApi
    @PostMapping
    public ResponseEntity<ResponseDto> create(...) { ... }
}
```

**DTO 네이밍**
- Request: `Request{Action}{Entity}Dto` (e.g., `RequestCreateTipDto`)
- Response: `Response{Entity}Dto` (e.g., `ResponseTipDto`)

**예외 처리**
- `global/exception/` 의 커스텀 예외 사용
- `.orElseThrow()` 패턴, `Optional.get()` 직접 호출 금지

**Swagger**
- `ApiSpecification` 인터페이스에 `@Operation`, `@ApiResponse` 분리 정의

### 4단계: SecurityConfig 업데이트
- 새 엔드포인트의 접근 권한 설정 (`permitAll()` 또는 `hasRole()`)

### 5단계: DB 마이그레이션 (필요 시)
- `src/main/resources/db/migration/` 기존 파일 목록 확인
- `V{다음번호}__{snake_case_설명}.sql` 생성
- MySQL 문법: `CREATE TABLE IF NOT EXISTS`, `ALTER TABLE ADD COLUMN IF NOT EXISTS`

### 6단계: 완료 보고
```
## 구현 완료

### 변경된 파일
- (생성) src/.../EntityName.java
- (수정) src/.../SecurityConfig.java

### API
- POST /endpoint - 설명
- GET /endpoint/{id} - 설명

### 추가 작업 필요
- (있으면 명시)
```

## 중요 원칙
- 코드를 읽지 않고 추측으로 구현하지 말 것
- 기존 코드 패턴을 반드시 참고할 것
- 과도한 추상화나 불필요한 기능 추가 금지
- 요청된 기능만 정확히 구현

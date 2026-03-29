다음 에러를 분석하고 원인과 해결책을 찾아줘: $ARGUMENTS

## 분석 프로세스

### 1단계: 에러 분류
에러 메시지를 보고 유형 파악:
- `NullPointerException` / `NullPointerException`
- `LazyInitializationException` → 트랜잭션 밖 지연 로딩
- `DataIntegrityViolationException` → DB 제약조건 위반
- `BeanCreationException` → Spring 빈 설정 오류
- `JwtException` → JWT 토큰 문제
- `HttpMessageNotReadableException` → 요청 DTO 역직렬화 실패
- `MethodArgumentNotValidException` → validation 실패
- `QueryTimeoutException` / `CannotAcquireLockException` → DB 성능/잠금
- `ConnectException` → 외부 서비스(Redis, FCM, Oracle) 연결 실패
- `CircuitBreakerOpenException` → Resilience4j Circuit Breaker 오픈

### 2단계: 스택 트레이스 분석
에러가 발생한 클래스/메서드를 찾아서 해당 파일 읽기:
- `com.example.appcenter_project` 패키지 라인 우선 확인
- 원인이 되는 코드 라인 특정

### 3단계: 관련 코드 탐색
- 에러 발생 지점 파일 읽기
- 연관된 엔티티/서비스/설정 파일 확인
- `application.yml` 설정 확인이 필요한 경우

### 4단계: 해결책 제시

출력 형식:
```
## 원인
(에러가 발생한 이유를 코드 레벨에서 설명)

## 해결 방법
(코드 수정 예시 포함)

## 재발 방지
(같은 패턴의 에러를 피하는 방법)
```

## 자주 발생하는 패턴 (이 프로젝트)

| 에러 | 원인 | 해결 |
|------|------|------|
| `LazyInitializationException` | @Transactional 없는 서비스에서 연관 엔티티 접근 | `@Transactional` 추가 또는 `fetch join` |
| `could not initialize proxy` | 동일 원인 | `@EntityGraph` 또는 즉시 로딩 고려 |
| `No serializer found` | 엔티티 직접 반환 | DTO 변환 후 반환 |
| `Circular view path` | Controller에서 String 반환 | `@RestController` 확인 |
| `Table doesn't exist` | Flyway 미실행 또는 ddl-auto 설정 | `spring.jpa.hibernate.ddl-auto` 확인 |
| `Cannot acquire lock` | 동시 요청 충돌 | `@Transactional(isolation=...)` 또는 Redis 분산락 |
| `Redis connection refused` | Redis 미실행 | 로컬 Redis 실행 또는 캐시 비활성화 |

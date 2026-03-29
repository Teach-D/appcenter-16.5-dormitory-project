다음 기능을 이 프로젝트에 구현해줘: $ARGUMENTS

## 구현 프로세스

### 0단계: GitHub Issue 연결
구현 전에 반드시 Issue를 확인하고 기록한다.

1. `.claude/issues.md`를 읽어서 이 기능과 관련된 기존 이슈가 있는지 확인
2. 없으면 사용자에게 다음 중 선택하도록 질문:
   - **A) Claude가 이슈 생성**: `gh issue create --repo Teach-D/appcenter-16.5-dormitory-project --title "{기능 제목}" --body "{간략한 설명}"` 실행 후 생성된 번호를 `.claude/issues.md`에 기록
   - **B) 사용자가 직접 생성**: "이슈를 직접 만들고 번호를 알려주세요. `/record-issue <번호>` 로 기록할 수 있습니다." 안내
3. 이슈 번호가 확정되면 `.claude/issues.md`에 기록 후 다음 단계 진행

### 1단계: 기획 확인 (구현 전 필수)
코드를 읽거나 구현하기 전에, 요구사항을 정확히 파악하기 위해 아래 항목 중 불명확한 것들을 **한 번에 모아서** 질문한다.
(이미 명확한 항목은 생략, 불필요한 질문 금지)

체크할 항목:
- **대상**: 누가 이 기능을 사용하나? (일반 사용자 / 관리자 / 특정 역할)
- **트리거**: 어떤 상황에서 동작하나? (어떤 API 호출 시 / 특정 이벤트 발생 시)
- **입출력**: 어떤 데이터를 받아서 어떤 결과를 돌려줘야 하나?
- **예외 케이스**: 실패 시 어떻게 처리해야 하나? (에러 응답 / 무시 / 재시도)
- **범위**: 이번 작업에 포함되는 것과 제외되는 것은?

질문 후 답변을 받으면 바로 2단계로 진행한다.

### 2단계: 코드 분석
- 요구사항과 관련된 도메인 파악
- 기존 유사 기능 코드 읽기 (참고할 패턴 파악)
- 영향받는 파일 목록 파악

### 3단계: 설계 확인 (구현 전 출력)
```
## 구현 계획
- 도메인: {domain}
- 생성 파일: [목록]
- 수정 파일: [목록]
- DB 변경: 있음/없음
- 주요 로직: [간략 설명]
```

### 4단계: 구현

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

### 5단계: SecurityConfig 업데이트
- 새 엔드포인트의 접근 권한 설정 (`permitAll()` 또는 `hasRole()`)

### 6단계: DB 마이그레이션 (필요 시)
- `src/main/resources/db/migration/` 기존 파일 목록 확인
- `V{다음번호}__{snake_case_설명}.sql` 생성
- MySQL 문법: `CREATE TABLE IF NOT EXISTS`, `ALTER TABLE ADD COLUMN IF NOT EXISTS`

### 7단계: 완료 보고
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

### 8단계: 커밋 준비
완료 보고 직후 자동으로 아래 순서를 진행한다.

1. **코드 설명**: 변경된 파일별로 핵심 로직을 간략히 설명
2. **git add 승인 요청**: "위 파일들을 git add 할까요?" 라고 물어보고 대기
3. 승인하면 → `git add` 실행 (변경된 파일만 명시적으로 지정, `git add .` 금지)
4. **커밋 메시지 3개 추천**: 아래 형식으로 번호 붙여서 제안
   ```
   1) feat: {영문 설명} #{이슈번호}
   2) feat: {다른 표현} #{이슈번호}
   3) {다른 prefix}: {또 다른 표현} #{이슈번호}
   ```
   - Conventional Commits 규칙 준수 (feat/fix/refactor/chore 등)
   - 이슈 번호는 `.claude/issues.md`에서 현재 작업과 연결된 번호 사용
5. 사용자가 번호를 선택하면 → 해당 메시지로 `git commit -m` 실행

## 중요 원칙
- 코드를 읽지 않고 추측으로 구현하지 말 것
- 기존 코드 패턴을 반드시 참고할 것
- 과도한 추상화나 불필요한 기능 추가 금지
- 요청된 기능만 정확히 구현

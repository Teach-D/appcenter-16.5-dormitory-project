$ARGUMENTS 에 대한 테스트 코드를 작성하거나 실행해줘. (인자 없으면 현재 작업 중인 코드 기준)

## 테스트 작성 가이드

### 위치
`src/test/java/com/example/appcenter_project/domain/{domain}/`

### 기본 구조
```java
@ExtendWith(MockitoExtension.class)
class {DomainName}ServiceTest {

    @InjectMocks
    private {DomainName}Service {domainName}Service;

    @Mock
    private {EntityName}Repository {entityName}Repository;

    // 필요한 Mock 추가

    @Test
    @DisplayName("성공: {동작 설명}")
    void {메서드명}_success() {
        // given
        // when
        // then (Assertions.assertThat 또는 verify)
    }

    @Test
    @DisplayName("실패: {실패 조건 설명}")
    void {메서드명}_fail_{reason}() {
        // given - 실패 조건 설정
        // when & then
        assertThrows({CustomException}.class, () -> {
            {domainName}Service.{method}(...);
        });
    }
}
```

### 테스트 대상 우선순위
1. Service 레이어 단위 테스트 (MockitoExtension)
2. 복잡한 비즈니스 로직 (룸메이트 매칭, 공동구매 참여 조건 등)
3. 예외 케이스 (존재하지 않는 엔티티, 권한 없음, 중복 등)

### 테스트 실행 명령
```bash
# 전체 테스트
./gradlew test

# 특정 클래스만
./gradlew test --tests "com.example.appcenter_project.domain.{domain}.*ServiceTest"

# 특정 메서드만
./gradlew test --tests "*.{ClassName}.{methodName}"
```

### 주의사항
- Repository는 Mock 처리 (DB 연결 불필요)
- `when(...).thenReturn(...)` 패턴으로 Mock 설정
- 엔티티 생성 시 정적 팩토리 메서드 `create()` 사용
- given/when/then 주석으로 섹션 구분

테스트 코드 작성 후 `./gradlew test --tests` 명령으로 실제 실행 결과도 확인해줘.

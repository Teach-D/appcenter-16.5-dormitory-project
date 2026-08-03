# 안티패턴 규칙 — 테스트 / Fixture / Mockito

Fixture 작성 및 테스트 코드 리뷰 시 반드시 확인한다.

---

## Mockito

### Mockito.when() 내포 Fixture를 willReturn() 인자로 인라인 금지

```java
// BAD — createUserWithId() 내부에 Mockito.when() 포함 → UnfinishedStubbingException
given(userRepository.findById(1L))
    .willReturn(Optional.of(Fixture.createUserWithId(1L)));

// GOOD — 변수에 먼저 저장 후 사용
User user = Fixture.createUserWithId(1L);
given(userRepository.findById(1L)).willReturn(Optional.of(user));
```

- Java는 메서드 인자를 먼저 평가한다. `given()` 의 pending stubbing 도중 `Mockito.when()` 이 새로 호출되면 `UnfinishedStubbingException` 발생.
- 실제 발생 사례: BR-668 개인 채팅방 생성 테스트 (2026-07-05)

### Fixture 헬퍼는 실제 객체 반환 우선 — mock 사용 금지

```java
// BAD — Mockito.mock() + when() 조합 → 위 인라인 안티패턴과 조합 시 폭탄
public static User createUserWithId(Long id) {
    User user = Mockito.mock(User.class);
    Mockito.when(user.getId()).thenReturn(id);
    return user;
}

// GOOD — 도메인에 이미 있는 테스트용 팩토리 메서드 활용
public static User createUserWithId(Long id) {
    return User.createForTest(id, "user-" + id, DormType.DORM_1);
}
```

- mock 객체는 stubbing되지 않은 메서드가 null을 반환해 다른 검증에서 예상치 못한 실패를 유발한다.
- 엔티티에 `createForTest()` 정적 팩토리가 없으면 추가한다 (`User.java` 참고).

### @ExtendWith(MockitoExtension.class) — STRICT_STUBS 기본 동작 인지

- `MockitoExtension` 은 기본적으로 `Strictness.STRICT_STUBS` 적용.
- 설정만 하고 사용하지 않은 stubbing → `UnnecessaryStubbingException`.
- 테스트에서 실제로 호출되는 메서드만 stubbing한다.

# 안티패턴 규칙 — JPA / QueryDSL

JPA 엔티티·쿼리 작성 시 반드시 확인한다.

---

## N+1 쿼리

### 연관관계 목록 조회 시 N+1 금지 → fetch join

```java
// BAD — 루프마다 SELECT 발생
List<Complaint> complaints = complaintRepository.findAll();
complaints.forEach(c -> c.getUser().getName()); // N+1

// GOOD — QuerydslRepositoryImpl에서 fetch join
@Override
public List<Complaint> findAllWithUser() {
    return queryFactory
        .selectFrom(complaint)
        .join(complaint.user, user).fetchJoin()
        .fetch();
}
```

### @FetchType.EAGER 전역 설정 금지

```java
// BAD
@ManyToOne(fetch = FetchType.EAGER)
private User user;

// GOOD — 기본 LAZY, 필요한 쿼리에서만 fetch join
@ManyToOne(fetch = FetchType.LAZY)
private User user;
```

### 영속성 컨텍스트 밖 지연 로딩 금지

- 트랜잭션이 끝난 뒤(Controller, DTO 변환 후) 연관 객체에 접근하면 `LazyInitializationException`
- Service 레이어 내 `@Transactional` 범위 안에서 필요한 연관관계를 모두 로딩 후 DTO로 변환

### @Async 메서드 내부 LAZY 로딩 금지 → @Transactional 추가

```java
// BAD — @Async는 별도 스레드, 원래 트랜잭션 컨텍스트 없음 → LazyInitializationException
@Async("fcmExecutor")
public void sendNotification(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(...);
    user.getFcmTokens();  // LAZY → 예외 발생
}

// GOOD — @Transactional로 새 트랜잭션 열기, 또는 호출 전 fetch join으로 DTO로 변환해서 전달
@Async("fcmExecutor")
@Transactional
public void sendNotification(Long userId) {
    User user = userRepository.findById(userId).orElseThrow(...);
    user.getFcmTokens();  // 트랜잭션 내 → 정상 로딩
}
```

- 실제 발생 사례: `FcmMessageService` @Async 메서드 LazyInitializationException (2026-04-03)

---

## 트랜잭션

### 읽기 전용 메서드에 readOnly = true 누락 금지

```java
// BAD
@Transactional
public ResponseComplaintDto findById(Long id) { ... }

// GOOD
@Transactional(readOnly = true)
public ResponseComplaintDto findById(Long id) { ... }
```

- `readOnly = true`: 스냅샷 저장 생략, 플러시 모드 비활성화 → 성능 향상

### 불필요하게 넓은 @Transactional 범위 금지

```java
// BAD — 외부 API 호출까지 트랜잭션에 포함
@Transactional
public void processOrder(Long orderId) {
    Order order = orderRepository.findById(orderId).orElseThrow(...);
    externalApiClient.notify(order);  // 외부 API 호출이 트랜잭션 안
    order.complete();
}

// GOOD — DB 변경만 트랜잭션, 외부 호출은 트랜잭션 밖
public void processOrder(Long orderId) {
    completeOrder(orderId);           // @Transactional 메서드
    externalApiClient.notify(orderId);
}
```

### @Transactional 없이 엔티티 상태 변경 금지

- 변경 감지(Dirty Checking)는 트랜잭션 안에서만 동작
- 상태를 변경하는 Service 메서드에는 반드시 `@Transactional` 적용

### @Modifying 쿼리에 @Transactional 누락 금지

```java
// BAD — @Modifying은 쓰기 작업, @Transactional 없으면 TransactionRequiredException
@Modifying
@Query("DELETE FROM FcmToken t WHERE t.token = :token")
void deleteByToken(@Param("token") String token);

// GOOD
@Modifying
@Transactional
@Query("DELETE FROM FcmToken t WHERE t.token = :token")
void deleteByToken(@Param("token") String token);
```

- 실제 발생 사례: FCM UNREGISTERED 토큰 삭제 시 TransactionRequiredException (2026-04-27)

### 전체 발송 N+1 — 유저별 반복 조회 금지 → bulk 조회

```java
// BAD — 유저 N명 × (findById + findAllByUser + save) = N*3 쿼리
users.forEach(user -> {
    User u = userRepository.findById(user.getId()).orElseThrow(...);
    List<FcmToken> tokens = fcmTokenRepository.findAllByUser(u);
    tokens.forEach(t -> fcmOutboxRepository.save(FcmOutbox.create(t, title, body)));
});

// GOOD — 전체 유저 토큰 한 번에 조회 후 bulk insert (쿼리 2회)
List<FcmToken> allTokens = fcmTokenRepository.findAllByUserIn(users);
fcmOutboxRepository.saveAll(allTokens.stream()
    .map(t -> FcmOutbox.create(t, title, body)).toList());
```

- 실제 발생 사례: `AnnouncementNotificationService` 공지 전체 발송 N+1 (2026-04-27)

---

## QueryDSL

### 복잡한 쿼리를 JpaRepository에 @Query로 직접 작성 금지

```java
// BAD — JpaRepository에 JPQL 직접 작성
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    @Query("SELECT c FROM Complaint c JOIN FETCH c.user WHERE c.status = :status")
    List<Complaint> findByStatusWithUser(@Param("status") ComplaintStatus status);
}

// GOOD — QuerydslRepositoryImpl에 구현
@Repository
@RequiredArgsConstructor
public class ComplaintQuerydslRepositoryImpl implements ComplaintQuerydslRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Complaint> findByStatusWithUser(ComplaintStatus status) {
        return queryFactory
            .selectFrom(complaint)
            .join(complaint.user, user).fetchJoin()
            .where(complaint.status.eq(status))
            .fetch();
    }
}
```

### BooleanBuilder 대신 BooleanExpression 사용

```java
// BAD
BooleanBuilder builder = new BooleanBuilder();
if (status != null) builder.and(complaint.status.eq(status));
if (keyword != null) builder.and(complaint.title.contains(keyword));

// GOOD
private BooleanExpression statusEq(ComplaintStatus status) {
    return status != null ? complaint.status.eq(status) : null;
}
private BooleanExpression titleContains(String keyword) {
    return keyword != null ? complaint.title.contains(keyword) : null;
}

// 호출: .where(statusEq(status), titleContains(keyword))
// null 조건은 QueryDSL이 자동으로 무시
```

### offset 페이징에서 count 쿼리 분리

```java
// BAD — 대용량 데이터에서 count + 데이터 쿼리가 매번 join
return queryFactory.selectFrom(complaint)
    .join(complaint.user, user).fetchJoin()
    ...
    .fetchResults(); // deprecated, count 쿼리도 join 포함

// GOOD — count 쿼리 별도 최적화
List<Complaint> content = queryFactory.selectFrom(complaint)...fetch();
long total = queryFactory.select(complaint.count()).from(complaint)
    .where(condition).fetchOne();
return new PageImpl<>(content, pageable, total);
```

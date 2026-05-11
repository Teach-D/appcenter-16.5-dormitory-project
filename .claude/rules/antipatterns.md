# 안티패턴 규칙 — Spring 아키텍처 / Lombok·엔티티 / API·예외

코드 작성 전 반드시 확인한다.

---

## Spring 아키텍처

### Controller에 비즈니스 로직 작성 금지

```java
// BAD
@PostMapping
public ResponseEntity<?> create(@RequestBody RequestCreateDto dto) {
    User user = userRepository.findById(dto.getUserId())
        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    Complaint complaint = Complaint.create(user, dto.getContent());
    complaintRepository.save(complaint);
    return ResponseEntity.ok().build();
}

// GOOD
@PostMapping
public ResponseEntity<?> create(@RequestBody @Valid RequestCreateComplaintDto dto) {
    complaintService.createComplaint(dto);
    return ResponseEntity.ok().build();
}
```

### 필드 주입(@Autowired) 금지 → 생성자 주입

```java
// BAD
@Service
public class ComplaintService {
    @Autowired
    private ComplaintRepository complaintRepository;
}

// GOOD
@Service
@RequiredArgsConstructor
public class ComplaintService {
    private final ComplaintRepository complaintRepository;
}
```

### Service 간 직접 순환 의존 금지

- A → B → A 형태의 순환 참조는 스프링 컨텍스트 로드 실패 원인
- 공통 로직은 별도 Service 또는 도메인 이벤트로 분리

---

## Lombok / 엔티티 패턴

### @Builder 직접 노출 금지 → 정적 팩토리 메서드

```java
// BAD
@Builder
public class Complaint { ... }

// 호출부: Complaint.builder().user(user).content(content).build()

// GOOD
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Complaint extends BaseTimeEntity {

    public static Complaint create(User user, String content) {
        Complaint complaint = new Complaint();
        complaint.user = user;
        complaint.content = content;
        return complaint;
    }
}
```

### @Setter 엔티티 사용 금지 → update() 메서드

```java
// BAD
complaint.setContent("수정된 내용");

// GOOD
complaint.updateContent("수정된 내용");
```

### @AllArgsConstructor 엔티티 사용 금지

- 필드 순서 변경 시 컴파일 오류 없이 잘못된 값이 주입될 수 있음
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + 정적 팩토리 사용

### Entity 직접 API 응답 금지 → DTO 변환

```java
// BAD
@GetMapping("/{id}")
public Complaint getComplaint(@PathVariable Long id) {
    return complaintService.findById(id);  // 엔티티 직접 반환
}

// GOOD
@GetMapping("/{id}")
public ResponseComplaintDto getComplaint(@PathVariable Long id) {
    return complaintService.findById(id);  // DTO 반환
}
```

---

## API / 예외 처리

### RuntimeException 직접 throw 금지 → CustomException + ErrorCode

```java
// BAD
throw new RuntimeException("사용자를 찾을 수 없습니다.");
throw new IllegalArgumentException("잘못된 요청");

// GOOD
throw new CustomException(ErrorCode.USER_NOT_FOUND);
throw new CustomException(ErrorCode.INVALID_REQUEST);
```

### 예외 catch 후 무시 금지

```java
// BAD
try {
    someOperation();
} catch (Exception e) {
    // 아무것도 안 함
}

// GOOD — 재throw 또는 적절한 처리
try {
    someOperation();
} catch (Exception e) {
    throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
}
```

### Request DTO @Valid 누락 금지

```java
// BAD
public ResponseEntity<?> create(@RequestBody RequestCreateComplaintDto dto) { ... }

// GOOD
public ResponseEntity<?> create(@RequestBody @Valid RequestCreateComplaintDto dto) { ... }
```

### 잘못된 HTTP 상태코드 반환 금지

| 상황 | 올바른 코드 |
|------|-----------|
| 리소스 없음 | 404 NOT_FOUND |
| 권한 없음 | 403 FORBIDDEN |
| 유효성 실패 | 400 BAD_REQUEST |
| 중복 데이터 | 409 CONFLICT |
| 정상 생성 | 201 CREATED |

---

## 도메인별 실전 주의사항

### 외부 API 전달 시 LocalDate/LocalDateTime 사용 금지 → Instant

```java
// BAD — 서버 JVM 타임존 기준, 외부 시스템과 불일치
Map<String, Object> props = Map.of("last_active_date", LocalDate.now().toString());

// GOOD — UTC ISO 8601, 타임존 무관
Map<String, Object> props = Map.of("last_active_date", Instant.now().toString());
```

- Mixpanel, Sentry 등 외부 API로 날짜/시간 전달 시 항상 `Instant` 사용
- 실제 발생 사례: Mixpanel `last_active_date` 타임존 불일치 (2026-04-27)

### @Scheduled 메서드에서 @Transactional 직접 사용 금지

```java
// BAD — @Scheduled + @Transactional 조합, Dirty Checking 미작동 케이스 있음
@Scheduled(cron = "0 0 1 * * *")
@Transactional
public void cleanupOldOrders() {
    List<GroupOrder> old = groupOrderRepository.findOldOrders();
    old.forEach(o -> o.updateStatus(GroupOrderStatus.EXPIRED)); // 미반영될 수 있음
}

// GOOD — @Transactional 작업을 별도 메서드로 분리
@Scheduled(cron = "0 0 1 * * *")
public void cleanupOldOrders() {
    groupOrderService.expireOldOrders(); // @Transactional 메서드
}
```

### coupon 재고 차감 시 비관적 락 필수

```java
// BAD — 동시 요청 시 race condition, 재고 초과 발급
Coupon coupon = couponRepository.findById(couponId).orElseThrow(...);
coupon.decreaseStock();

// GOOD — FOR UPDATE로 row lock
Coupon coupon = couponRepository.findByIdWithLock(couponId).orElseThrow(...);
coupon.decreaseStock();
```

- 쿠폰 외에도 재고·좌석 등 수량 차감 로직에 동일하게 적용

### notification 발송 전 수신 타입 필터링 필수

```java
// BAD — 모든 유저에게 발송
List<User> users = userRepository.findAll();

// GOOD — 수신 설정한 유저만 필터링
List<User> users = userRepository.findAll().stream()
    .filter(u -> u.getReceiveNotificationTypes().contains(notificationType))
    .toList();
```

### calender 도메인 철자 주의

- DB 테이블명: `calender` (e 하나) — 오탈자지만 이미 운영 중이라 변경 불가
- 엔티티·서비스·패키지·쿼리 모두 `calender`로 통일. `calendar`로 작성하면 불일치 발생

### survey 응답 시 상태·날짜 이중 검증 필수

```java
// BAD — 상태만 체크, 날짜 범위 미검증
Survey survey = surveyRepository.findById(id).orElseThrow(...);
if (survey.getStatus() != SurveyStatus.OPEN) throw new CustomException(...);

// GOOD — 상태 + 날짜 범위 동시 검증
Survey survey = surveyRepository.findById(id).orElseThrow(...);
LocalDate now = LocalDate.now();
if (survey.getStatus() != SurveyStatus.OPEN
        || now.isBefore(survey.getStartDate())
        || now.isAfter(survey.getEndDate())) {
    throw new CustomException(ErrorCode.SURVEY_NOT_AVAILABLE);
}
```

### feature flag 미등록 key → 예외 아닌 false 반환

```java
// BAD — 등록되지 않은 key 요청 시 예외 발생 → 클라이언트 오류
Feature feature = featureRepository.findByKey(key).orElseThrow(...);

// GOOD — 미등록 key는 기능 OFF(false)로 처리
boolean enabled = featureRepository.findByKey(key)
    .map(Feature::isFlag)
    .orElse(false);
```

### tip 댓글 실제 삭제 금지 → soft-delete

```java
// BAD — 실제 row 삭제 시 답글·신고 FK 제약 위반 가능
tipCommentRepository.deleteById(commentId);

// GOOD — is_deleted = true 로 soft-delete, row 유지
TipComment comment = tipCommentRepository.findById(commentId).orElseThrow(...);
comment.softDelete();
```

### report API 응답 DTO 금지 → void + 201 CREATED

- 신고(report)는 접수 저장만 하고 관리자가 별도 처리 — `ResponseReportDto` 설계 금지
- Controller 반환: `ResponseEntity<Void>` with HTTP 201

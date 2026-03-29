현재 변경된 코드 또는 $ARGUMENTS 에서 지정한 파일/도메인을 코드 리뷰해줘.

## 리뷰 기준

### 1. 프로젝트 컨벤션 준수
- 네이밍: Controller/Service/Repository/Dto 클래스명 패턴
- 패키지 위치가 올바른지 (domain/common/global/shared 구분)
- Request/Response DTO 네이밍 (`Request{Action}{Entity}Dto`, `Response{Entity}Dto`)

### 2. JPA/엔티티 패턴
- `BaseTimeEntity` 상속 여부
- `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 적용
- `@Builder` 대신 정적 팩토리 메서드 `create()` 사용 여부
- 연관관계 무한 순환 참조 위험 (`@JsonIgnore` 또는 DTO 변환)
- N+1 문제 가능성 (`@EntityGraph`, `fetch join`, `@BatchSize` 등)
- 영속성 컨텍스트 밖에서 지연 로딩 호출 여부

### 3. 트랜잭션
- `@Transactional` 위치가 Service 레이어인지
- 조회 메서드에 `@Transactional(readOnly = true)` 적용 여부
- 트랜잭션 전파 이슈 없는지

### 4. 보안
- SQL Injection 위험 (QueryDSL/JPQL 파라미터 바인딩 확인)
- 인증/인가 처리 누락된 엔드포인트
- 민감 정보 응답에 포함 여부 (비밀번호, 개인정보)
- `SecurityConfig` 미등록 공개 API

### 5. 성능
- 페이지네이션 누락 (전체 조회 API)
- 캐싱 적용 가능한 조회 미적용
- 불필요한 DB 쿼리 (동일 쿼리 반복)

### 6. 예외 처리
- 도메인 예외 `global/exception/` 커스텀 예외 사용 여부
- `Optional.get()` 직접 호출 대신 `.orElseThrow()` 사용 여부
- 적절한 HTTP 상태코드 반환

### 7. API 문서 (Swagger)
- `ApiSpecification` 인터페이스에 `@Operation`, `@ApiResponse` 정의
- `@TrackApi` 어노테이션 추가 여부

### 8. 코드 품질
- Lombok 중복 어노테이션
- 불필요한 import
- 하드코딩된 값 (상수화 권장)

## 출력 형식

각 항목에 대해:
- ✅ 문제없음 (간략히)
- ⚠️ 개선 권장 (이유 + 수정 제안)
- ❌ 필수 수정 (이유 + 코드 예시)

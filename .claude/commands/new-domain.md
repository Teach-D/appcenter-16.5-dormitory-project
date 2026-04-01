새로운 도메인 모듈을 생성해줘. 도메인 이름: $ARGUMENTS

다음 순서로 파일을 생성하되, 프로젝트의 기존 도메인 패턴을 정확히 따라야 해:

## 참고 도메인
기존 도메인 중 구조가 유사한 것을 먼저 읽어서 패턴 파악 후 진행해.
예: complaint 도메인 (entity, dto, service, controller 패턴 참고)

## 생성할 파일 목록

패키지: `com.example.appcenter_project.domain.{도메인명}/`

1. **entity/{EntityName}.java**
   - `BaseTimeEntity` 상속
   - `@Entity`, `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
   - 정적 팩토리 메서드 `create()` 포함
   - update 메서드 엔티티 내부 정의

2. **repository/{EntityName}Repository.java**
   - `JpaRepository<{EntityName}, Long>` 상속

3. **repository/{EntityName}QuerydslRepository.java** (복잡한 조회가 필요한 경우)
   - 인터페이스 정의

4. **repository/{EntityName}QuerydslRepositoryImpl.java** (복잡한 조회가 필요한 경우)
   - `JPAQueryFactory` 사용
   - QueryDSL Q클래스 활용

5. **dto/request/Request{Action}{EntityName}Dto.java** (Create, Update 등)
   - `@NotNull`, `@NotBlank` 등 validation 어노테이션
   - `@Getter`, `@NoArgsConstructor`

6. **dto/response/Response{EntityName}Dto.java**
   - `@Getter`, `@Builder` 또는 정적 팩토리 메서드
   - Entity → DTO 변환 메서드 포함

7. **service/{DomainName}Service.java**
   - `@Service`, `@RequiredArgsConstructor`, `@Transactional`
   - CRUD 기본 메서드
   - `@Transactional(readOnly = true)` 조회 메서드

8. **controller/{DomainName}ApiSpecification.java**
   - Swagger 어노테이션만 담는 인터페이스
   - `@Tag(name = "...")`
   - 각 메서드에 `@Operation`, `@ApiResponse`

9. **controller/{DomainName}Controller.java**
   - `ApiSpecification` 구현
   - `@RestController`, `@RequestMapping`, `@RequiredArgsConstructor`
   - `@TrackApi` 어노테이션 추가 (API 메트릭 추적)

## 추가로 확인할 것

- `global/config/SecurityConfig.java` - 새 엔드포인트의 접근 권한 설정 필요 여부
- `src/main/resources/db/migration/` - 새 테이블 마이그레이션 SQL 필요 여부 (V{다음번호}__{설명}.sql)

생성 완료 후, 각 파일의 위치와 주요 내용을 요약해줘.

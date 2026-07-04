# UniDorm — CLAUDE.md

인천대학교 기숙사 통합 앱/웹 서비스(UniDorm) 백엔드 프로젝트.
Claude Code가 이 저장소에서 작업할 때 반드시 이 문서를 먼저 확인한다.

---

## 절대 규칙 (위반 금지)

1. **코딩 전에 먼저 생각한다**
   - 가정은 숨기지 않고 명시한다. 불확실하면 코드보다 질문이 먼저다.
   - 해석이 여럿이면 조용히 하나 고르지 말고 둘 다 제시하고 고르게 한다.
   - 더 단순한 방법이 보이면 말하고, 필요하면 반대 의견을 낸다.
   - 무엇이 헷갈리는지 이름 붙여 묻는다. "잘 모르겠습니다"는 금지.

2. **최소 코드 원칙** — 지금 실패하는 테스트를 통과시키는 데 필요한 최소한만 작성한다.
   - 요청하지 않은 방어 로직·추상화·유틸·설정 가능성·유연성 미리 만들기 금지.
   - "나중에 쓸 것 같아서" 금지. 필요해지는 순간 그 테스트와 함께 추가한다.
   - single-use 코드에 추상화 금지. 200줄인데 50줄로 될 것 같으면 다시 쓴다.
   - 판단 기준: "시니어 엔지니어가 과하다고 할까?" → 그렇다면 단순화.

3. **비목표 우선**: 모든 기능 작업은 목표뿐 아니라 "하지 않을 것(Non-goals)"을 먼저 확인한다.
   범위 밖 파일/엔드포인트/기능을 만들지 않는다.

4. **외과적 변경** — UniDorm은 운영 중인 실서비스다. 기존 코드를 건드릴 때 특히 적용.
   - 건드려야 하는 것만 건드린다. 인접 코드·주석·포맷을 "개선"하지 않는다.
   - 안 깨진 걸 리팩터하지 않는다. 내 스타일과 달라도 기존 스타일을 따른다.
   - 관련 없는 죽은 코드를 발견하면 삭제하지 말고 언급만 한다.
   - 내 변경으로 안 쓰이게 된 import/변수/함수만 정리한다.
   - `/implement`로 명세 기반 새 코드를 작성하는 경우에는 이 조항이 방해하지 않는다.

5. **명세가 진실의 원천**: `specs/BR-xxx/` 아래 문서와 코드가 어긋나면 명세를 먼저 고치고 코드를 맞춘다.

6. **추적성**: 커밋·PR·이슈·테스트에 관련 BR 번호(BR-xxx)를 명시한다.

---

## 기술 스택

Java 17, Spring Boot 3.4.4, Spring Security + JWT, JPA + QueryDSL, MySQL, Redis, WebSocket(STOMP), FCM, Selenium, JUnit 5 + Mockito + Testcontainers.

---

## 프로젝트 폴더 구조

```
src/main/java/com/example/appcenter_project/
├─ common/          # BaseTimeEntity, 공용 file/image/like
├─ domain/          # 16개 도메인 (핵심 비즈니스)
│  └─ {도메인}/
│     ├─ controller/   # {Domain}Controller + {Domain}ApiSpecification
│     ├─ service/      # {Domain}Service
│     ├─ repository/   # {Entity}Repository + QueryDSL Impl
│     ├─ entity/       # JPA 엔티티
│     ├─ dto/          # request/ + response/
│     └─ enums/
├─ global/          # exception, security, config, scheduler, aspect
└─ shared/          # 공용 유틸/enum
```

**의존 방향**: `controller → service → repository → entity`. 도메인 간 호출은 service 레벨에서만.
**주입**: 필드 주입 금지, 모두 생성자 주입(`@RequiredArgsConstructor` + `final`).

---

## 워크플로우
- 새 기능: `/specify` → `/design` → `/api-spec` → `/issue` → `/tdd` → `/implement` → (reviewer 검증)
- 버그 수정 등 소규모: `/quickfix` (전체 파이프라인 생략)

## 테스트
- 작업을 검증 가능한 목표로 바꾼다: "버그 수정" → "재현 테스트를 쓰고 통과시킨다", "검증 추가" → "잘못된 입력 테스트를 쓰고 통과시킨다".
- 구현 전 실패 테스트 먼저(RED). 통과(GREEN) 후 리팩터.
- "일단 되게 해"는 금지. 강한 성공 기준 없이 에이전트 루프를 돌리지 않는다.
- 실행: `./gradlew test`

## 규칙 참조

엔티티·서비스·쿼리 작성 시, 또는 코드 리뷰·버그 수정 전 아래 파일을 확인한다.

- `.claude/rules/antipatterns.md` — Spring 아키텍처, Lombok/엔티티 패턴, API/예외 처리 안티패턴
- `.claude/rules/antipatterns-jpa.md` — N+1, 트랜잭션, QueryDSL 안티패턴

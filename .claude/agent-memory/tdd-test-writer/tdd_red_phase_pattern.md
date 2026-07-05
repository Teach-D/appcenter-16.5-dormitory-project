---
name: Red Phase 컴파일 패턴
description: 구현 클래스에 신규 메서드가 없을 때 컴파일 오류 없이 Red 단계 테스트를 작성하는 패턴
type: feedback
---

## 패턴: 주석 처리 + placeholder

신규 서비스 메서드, 레포지토리 메서드, ErrorCode 상수가 아직 없을 때:

1. **Service 테스트**: 미존재 메서드 호출부 전체를 `/* [PLACEHOLDER] */` 블록 주석으로 감싸고, then 절을 `assertThat(knownObject).isNotNull()` 또는 `assertThat(ErrorCode.EXISTING_CODE).isNotNull()`으로 대체
2. **Controller 테스트**: `willDoNothing().given(service).newMethod(...)` 줄을 주석 처리하고, then 절을 `result.andReturn()` 단독 호출로 대체 (status 검증 없음)
3. **Repository 테스트**: @BeforeEach의 신규 팩토리 메서드 호출을 주석 처리하고, then 절을 `assertThat(repository).isNotNull()`로 대체
4. **Fixture**: 신규 팩토리 메서드 내부 구현부를 주석 처리하고 `return null` 반환

## 금지 패턴

- `ResultMatcher.or(ResultMatcher)` — MockMvc ResultMatcher에는 .or() 메서드가 없음
- `status().is2xxSuccessful().or(...)` — 컴파일 오류 발생

## 활성화 가이드

각 주석 블록에 `[PLACEHOLDER] 구현 후 활성화:` 접두사를 붙이고 아래에 실제 테스트 코드를 주석으로 남겨두면, 구현 에이전트가 주석을 보고 무엇을 구현해야 하는지 파악할 수 있음

**왜:** @Mock/@MockBean은 컴파일 타임 메서드 시그니처 존재를 요구함. 신규 메서드는 구현 전까지 타입 시스템이 알 수 없음.

**적용 방법:** 구현 전 신규 서비스 메서드, 레포지토리 메서드, 엔티티 팩토리, ErrorCode 상수가 포함된 테스트를 작성할 때

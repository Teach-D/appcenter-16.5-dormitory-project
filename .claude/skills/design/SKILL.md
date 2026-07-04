---
name: design
description: specs/BR-<번호>-*/requirement.md를 읽고 도메인 모델과 계층 구조를 설계한다. "도메인 설계해줘", "design 해줘", "/design" 등 도메인 설계 요청 시 사용한다.
argument-hint: [BR번호]
allowed-tools: Read, Write, Grep, Glob
---
# 도메인 설계

인자: $ARGUMENTS

## 대화 규칙

모호한 점이 있어 사용자에게 물어봐야 할 때는 **한 번에 하나씩** `AskUserQuestion` 툴을 사용한다.
여러 질문을 한꺼번에 나열하지 않는다. 답변을 받은 후 다음 질문으로 진행한다.

## 실행 순서

**1단계 — 명세 읽기**
`specs/BR-$1-*/requirement.md` 를 찾아 읽는다.
인자 없이 호출된 경우: 현재 브랜치명에서 BR 번호를 추출한다 (`git branch --show-current` 출력에서 숫자 파싱).

**2단계 — 코드베이스 조사**
설계 전 기존 패턴을 파악한다.
- 유사 도메인의 엔티티·서비스·레포지토리 구조를 Grep/Glob으로 확인
- 네이밍 컨벤션, 연관관계 패턴, enum 사용 방식 등 기존 스타일에 맞춘다

**3단계 — design.md 작성**
`specs/BR-$1-*/design.md` 에 아래 형식으로 작성한다.

---

## 엔티티 / 값 객체

각 엔티티의 핵심 필드, 타입, 제약조건을 기술한다.

## 애그리거트 경계

애그리거트 루트와 내부 객체를 구분하고, 경계를 넘는 참조 방식(ID 참조 vs 객체 참조)을 명시한다.

## 연관관계

엔티티 간 관계(1:N, N:M 등)와 fetch 전략(LAZY/EAGER), 연관관계 주인을 명시한다.

## DB 스키마 변경

새 테이블·컬럼·인덱스가 있으면 DDL 수준으로 명시한다. 없으면 "없음"으로 표기.

## 도메인 계층 구조

```
domain/{도메인명}/
├── controller/
├── service/
├── repository/
├── entity/
├── dto/
│   ├── request/
│   └── response/
└── enums/
```

새로 생성할 클래스 목록과 수정할 기존 클래스를 구분해 나열한다.

## 비목표

requirement.md의 비목표와 어긋나는 설계 요소는 넣지 않는다. 이번 설계에서 의도적으로 제외한 항목을 명시한다.

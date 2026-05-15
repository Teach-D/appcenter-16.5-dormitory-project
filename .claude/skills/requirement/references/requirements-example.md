# 요구사항 명세서

> **이 파일은 참고 예시입니다.** 실제 작업 시 이 구조와 수준으로 작성하세요.
> 예시 주제: 기숙사 공동구매 기능

---

## 1. 개요

- **서비스 목적**: 기숙사생이 공동구매 게시글을 올리고 참여자를 모아 함께 구매할 수 있는 기능
- **핵심 사용자**: 기숙사 입주 학생(USER), 기숙사 관리자(DORMITORY)
- **범위**
  - In Scope: 게시글 CRUD, 참여 신청/취소, 마감(모집 완료/기간 만료), 참여자 목록 조회
  - Out of Scope: 실제 결제 연동, 배송 추적, 외부 쇼핑몰 API 연동

---

## 2. 도메인 모델 후보

### 엔티티 목록

| 엔티티 | 핵심 속성 |
|--------|-----------|
| `GroupOrder` | id, title, description, targetCount, currentCount, deadline, status, createdBy |
| `GroupOrderParticipant` | id, groupOrder, user, joinedAt |
| `User` | id, name, role |

### 엔티티 간 관계

- `GroupOrder` 1 ↔ N `GroupOrderParticipant`
- `User` 1 ↔ N `GroupOrderParticipant` (한 유저가 여러 공동구매 참여 가능)
- `User` 1 ↔ N `GroupOrder` (작성자)

### 상태 다이어그램 — GroupOrder.status

```
OPEN → CLOSED (목표 인원 달성 또는 수동 마감)
OPEN → EXPIRED (deadline 경과)
CLOSED → (종료, 변경 불가)
EXPIRED → (종료, 변경 불가)
```

---

## 3. 비즈니스 규칙

1. **BR-01** 게시글 작성자만 수정·삭제·마감 가능
   - 위반 시: 403 FORBIDDEN
2. **BR-02** OPEN 상태인 게시글만 참여 신청 가능
   - 위반 시: 400 BAD_REQUEST (GROUP_ORDER_NOT_OPEN)
3. **BR-03** deadline 초과 시 자동으로 EXPIRED 처리 (스케줄러)
   - 처리: 매일 01:00 배치
4. **BR-04** currentCount가 targetCount에 도달하면 자동 CLOSED 전환
   - 처리: 참여 신청 완료 시점에 즉시 체크
5. **BR-05** 동일 사용자는 같은 공동구매에 중복 참여 불가
   - 위반 시: 409 CONFLICT (ALREADY_PARTICIPATED)
6. **BR-06** 게시글 작성자는 본인 게시글에 참여 불가
   - 위반 시: 400 BAD_REQUEST (OWNER_CANNOT_PARTICIPATE)
7. **BR-07** DORMITORY 역할은 모든 게시글 삭제 가능 (관리자 권한)

---

## 4. 사용자 & 권한

| 역할 | 접근 가능 리소스 |
|------|-----------------|
| `USER` | 게시글 CRUD(본인), 참여 신청/취소, 목록·상세 조회 |
| `DORMITORY` | 게시글 전체 삭제, 목록·상세 조회 |
| 비인증 | 없음 (전체 인증 필요) |

---

## 5. 주요 시나리오

### Happy Path

1. USER가 공동구매 게시글을 작성한다 (targetCount=5, deadline=3일 후)
2. 다른 USER 4명이 순차적으로 참여 신청한다
3. 4번째 참여로 currentCount=5(=targetCount)가 되면 status가 CLOSED로 자동 전환된다
4. 이후 참여 신청 시 GROUP_ORDER_NOT_OPEN 오류 반환

### 예외 시나리오

| 시나리오 | 처리 방식 |
|----------|-----------|
| deadline 지난 OPEN 게시글에 참여 신청 | 스케줄러 선 EXPIRED 처리 or 신청 시점 deadline 재검증 후 400 반환 |
| 참여 취소 후 재신청 | 허용 (BR-05는 현재 참여 중인 경우만 제한) |
| 목표 인원 0명으로 게시글 작성 | targetCount ≥ 2 유효성 검증, 위반 시 400 |
| 게시글 삭제 시 참여자가 존재 | 참여자 전체 연쇄 삭제 (Cascade) |

---

## 6. 비기능 요구사항

- **성능**: 목록 조회 응답 200ms 이내 (페이지네이션 필수, 기본 20건)
- **동시성**: 참여 신청 시 currentCount 증가 — 동시 요청 race condition 방지 필요 (비관적 락 또는 낙관적 락)
- **데이터 보존**: 게시글 삭제 시 hard delete (기숙사 민원과 달리 보존 불필요)
- **외부 연동**: 없음

---

## 7. 미결 사항 (TBD)

- [ ] 참여 취소 허용 여부: 마감 전까지만 허용할지, 언제든 허용할지
- [ ] 게시글에 이미지 첨부 기능 포함 여부
- [ ] 참여자 모집 완료 시 참여자 전원에게 FCM 알림 발송 여부

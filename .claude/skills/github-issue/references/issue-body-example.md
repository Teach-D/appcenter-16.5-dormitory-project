# GitHub 이슈 본문 예시

> **이 파일은 참고 예시입니다.** `gh issue create --body` 에 들어갈 본문을 이 수준으로 작성하세요.
> 예시는 3개 이슈 유형(feat/chore/feat with edge case)을 보여줍니다.

---

## 예시 1 — feat 이슈 (단일 엔드포인트)

**제목**: `[feat] 공동구매 참여 신청·취소`

```markdown
## 개요
기숙사생이 공동구매 게시글에 참여 신청하거나 취소할 수 있는 기능을 구현한다.
참여 신청 완료 시 currentCount가 targetCount에 도달하면 게시글 status가 자동으로 CLOSED로 전환된다.

## 작업 목록
- [ ] GroupOrderParticipant 엔티티 생성
- [ ] POST /group-orders/{groupOrderId}/participants — 참여 신청
- [ ] DELETE /group-orders/{groupOrderId}/participants — 참여 취소
- [ ] 참여 신청 시 currentCount 증가 및 CLOSED 자동 전환 로직
- [ ] ErrorCode 추가: ALREADY_PARTICIPATED, OWNER_CANNOT_PARTICIPATE, PARTICIPANT_NOT_FOUND
- [ ] 단위 테스트 작성

## API 설계
| Method | URL | 권한 | 설명 |
|--------|-----|------|------|
| POST | /group-orders/{groupOrderId}/participants | [AUTH] | 참여 신청 |
| DELETE | /group-orders/{groupOrderId}/participants | [AUTH] | 참여 취소 |

## 비즈니스 규칙
- BR-05: 동일 사용자 중복 참여 불가 → 409 ALREADY_PARTICIPATED
- BR-06: 작성자는 본인 게시글 참여 불가 → 400 OWNER_CANNOT_PARTICIPATE
- BR-04: currentCount == targetCount 도달 시 즉시 CLOSED 전환

## 엣지 케이스
- [ ] 참여 취소 후 재신청: 허용 (현재 참여 중인 경우만 중복 제한)
- [ ] CLOSED/EXPIRED 게시글 참여 신청: 400 GROUP_ORDER_NOT_OPEN 반환
- [ ] 참여 신청과 동시에 targetCount 도달: 비관적 락으로 race condition 방지

## 에러 케이스
| 상황 | HTTP Status | code |
|------|-------------|------|
| 게시글 미존재 | 404 | GROUP_ORDER_NOT_FOUND |
| OPEN 상태 아님 | 400 | GROUP_ORDER_NOT_OPEN |
| 이미 참여 중 | 409 | ALREADY_PARTICIPATED |
| 작성자 본인 참여 | 400 | OWNER_CANNOT_PARTICIPATE |
| 참여 내역 없음 (취소 시) | 404 | PARTICIPANT_NOT_FOUND |

## 참고
- 선행 이슈: #102 (공동구매 게시글 CRUD)
- 관련 문서: `docs/api-spec.md`
```

---

## 예시 2 — chore 이슈 (공통 설정)

**제목**: `[chore] 공동구매 공통 에러 코드 추가`

```markdown
## 개요
공동구매 기능 구현에 필요한 에러 코드를 ErrorCode.java에 추가한다.
이 이슈가 완료되어야 이후 feat 이슈들이 컴파일 에러 없이 구현을 시작할 수 있다.

## 작업 목록
- [ ] GROUP_ORDER_NOT_FOUND (404) 추가
- [ ] GROUP_ORDER_NOT_OWNED_BY_USER (403) 추가
- [ ] GROUP_ORDER_NOT_OPEN (400) 추가
- [ ] ALREADY_PARTICIPATED (409) 추가
- [ ] OWNER_CANNOT_PARTICIPATE (400) 추가
- [ ] PARTICIPANT_NOT_FOUND (404) 추가

## 참고
- 선행 이슈: 없음
- 관련 문서: `src/main/java/com/example/appcenter_project/global/exception/ErrorCode.java`
```

---

## 예시 3 — feat 이슈 (CRUD 묶음)

**제목**: `[feat] 공동구매 게시글 CRUD`

```markdown
## 개요
기숙사생이 공동구매 게시글을 작성·조회·수정·삭제할 수 있는 기능을 구현한다.
목록 조회는 status 필터링과 페이지네이션을 지원하며, 상세 조회에는 참여자 목록이 포함된다.

## 작업 목록
- [ ] GroupOrder 엔티티 생성 (status OPEN/CLOSED/EXPIRED)
- [ ] Flyway 마이그레이션 작성 (group_order, group_order_participant 테이블)
- [ ] POST /group-orders — 게시글 생성
- [ ] GET /group-orders — 게시글 목록 조회 (status 필터, 페이지네이션)
- [ ] GET /group-orders/{groupOrderId} — 게시글 상세 조회 (참여자 포함)
- [ ] PATCH /group-orders/{groupOrderId} — 게시글 수정 (작성자만)
- [ ] DELETE /group-orders/{groupOrderId} — 게시글 삭제 (작성자 또는 DORMITORY)
- [ ] GroupOrderQuerydslRepositoryImpl 구현 (목록 조회 N+1 방지)
- [ ] 단위 테스트 작성

## API 설계
| Method | URL | 권한 | 설명 |
|--------|-----|------|------|
| POST | /group-orders | [AUTH] | 게시글 생성 |
| GET | /group-orders | [AUTH] | 목록 조회 |
| GET | /group-orders/{groupOrderId} | [AUTH] | 상세 조회 |
| PATCH | /group-orders/{groupOrderId} | [AUTH] | 수정 |
| DELETE | /group-orders/{groupOrderId} | [AUTH][DORMITORY] | 삭제 |

## 비즈니스 규칙
- BR-01: 게시글 작성자만 수정·삭제 가능 (DORMITORY 제외)
- BR-02: OPEN 상태인 게시글만 수정 가능
- BR-07: DORMITORY 역할은 모든 게시글 삭제 가능

## 엣지 케이스
- [ ] targetCount가 2 미만인 경우: 400 VALIDATION_FAILED
- [ ] deadline이 현재 시각 이전인 경우: 400 VALIDATION_FAILED
- [ ] 목록 조회 N+1: GroupOrderQuerydslRepositoryImpl에서 fetch join 처리

## 에러 케이스
| 상황 | HTTP Status | code |
|------|-------------|------|
| 게시글 미존재 | 404 | GROUP_ORDER_NOT_FOUND |
| 작성자 아님 | 403 | GROUP_ORDER_NOT_OWNED_BY_USER |
| OPEN 상태 아님 | 400 | GROUP_ORDER_NOT_OPEN |

## 참고
- 선행 이슈: #101 (공통 에러 코드 추가)
- 관련 문서: `docs/api-spec.md`
```

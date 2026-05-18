# API 명세서 출력 예시

> **이 파일은 참고 예시입니다.** 실제 출력 시 이 구조와 수준으로 작성하세요.
> 예시 주제: 기숙사 공동구매 기능 (GroupOrder)

---

## 공동구매 API 명세서

### Endpoints

---

#### POST /group-orders
- **권한**: [AUTH]
- **설명**: 공동구매 게시글을 생성한다.
- **Request**:
  - Content-Type: `application/json`
  - Body: `RequestCreateGroupOrderDto`
- **Response**: `ResponseGroupOrderDto`
- **에러**:
  - `USER_NOT_FOUND` (404) — 사용자 미존재
  - `VALIDATION_FAILED` (400) — 필수 필드 누락 또는 targetCount < 2

---

#### GET /group-orders
- **권한**: [AUTH]
- **설명**: 공동구매 게시글 목록을 페이지네이션으로 조회한다.
- **Request**:
  - Query: `status` (GroupOrderStatus, nullable), `page` (int, 기본 0), `size` (int, 기본 20)
- **Response**: `Page<ResponseGroupOrderDto>`
- **에러**:
  - `JWT_ENTRY_POINT` (401) — 미인증 접근

---

#### GET /group-orders/{groupOrderId}
- **권한**: [AUTH]
- **설명**: 공동구매 게시글 상세를 조회한다. 참여자 목록 포함.
- **Request**:
  - Path: `groupOrderId` (Long)
- **Response**: `ResponseGroupOrderDetailDto`
- **에러**:
  - `GROUP_ORDER_NOT_FOUND` (404) — 게시글 미존재

---

#### PATCH /group-orders/{groupOrderId}
- **권한**: [AUTH]
- **설명**: 본인이 작성한 공동구매 게시글의 내용을 수정한다. OPEN 상태에서만 가능.
- **Request**:
  - Path: `groupOrderId` (Long)
  - Content-Type: `application/json`
  - Body: `RequestUpdateGroupOrderDto`
- **Response**: `ResponseGroupOrderDto`
- **에러**:
  - `GROUP_ORDER_NOT_FOUND` (404) — 게시글 미존재
  - `GROUP_ORDER_NOT_OWNED_BY_USER` (403) — 작성자 아님
  - `GROUP_ORDER_NOT_OPEN` (400) — OPEN 상태가 아님

---

#### DELETE /group-orders/{groupOrderId}
- **권한**: [AUTH], [DORMITORY]
- **설명**: 공동구매 게시글을 삭제한다. 작성자 또는 DORMITORY 역할만 가능.
- **Request**:
  - Path: `groupOrderId` (Long)
- **Response**: 없음 (204 No Content)
- **에러**:
  - `GROUP_ORDER_NOT_FOUND` (404) — 게시글 미존재
  - `GROUP_ORDER_NOT_OWNED_BY_USER` (403) — 작성자도 관리자도 아님

---

#### POST /group-orders/{groupOrderId}/participants
- **권한**: [AUTH]
- **설명**: 공동구매에 참여 신청한다. 신청 완료 시 currentCount가 targetCount에 도달하면 자동으로 CLOSED 전환.
- **Request**:
  - Path: `groupOrderId` (Long)
- **Response**: `ResponseGroupOrderDto`
- **에러**:
  - `GROUP_ORDER_NOT_FOUND` (404) — 게시글 미존재
  - `GROUP_ORDER_NOT_OPEN` (400) — OPEN 상태가 아님
  - `ALREADY_PARTICIPATED` (409) — 이미 참여 중
  - `OWNER_CANNOT_PARTICIPATE` (400) — 작성자는 본인 게시글 참여 불가

---

#### DELETE /group-orders/{groupOrderId}/participants
- **권한**: [AUTH]
- **설명**: 공동구매 참여를 취소한다. OPEN 상태에서만 가능.
- **Request**:
  - Path: `groupOrderId` (Long)
- **Response**: 없음 (204 No Content)
- **에러**:
  - `GROUP_ORDER_NOT_FOUND` (404) — 게시글 미존재
  - `GROUP_ORDER_NOT_OPEN` (400) — OPEN 상태가 아님
  - `PARTICIPANT_NOT_FOUND` (404) — 참여 내역 없음

---

#### PATCH /group-orders/{groupOrderId}/status
- **권한**: [AUTH]
- **설명**: 작성자가 공동구매를 수동으로 마감(CLOSED)한다.
- **Request**:
  - Path: `groupOrderId` (Long)
- **Response**: `ResponseGroupOrderDto`
- **에러**:
  - `GROUP_ORDER_NOT_FOUND` (404) — 게시글 미존재
  - `GROUP_ORDER_NOT_OWNED_BY_USER` (403) — 작성자 아님
  - `GROUP_ORDER_NOT_OPEN` (400) — 이미 CLOSED 또는 EXPIRED

---

### Schemas

#### RequestCreateGroupOrderDto

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| title | String | Y | 게시글 제목 (1~100자) |
| description | String | Y | 게시글 내용 (1~1000자) |
| targetCount | Integer | Y | 목표 참여 인원 (최소 2) |
| deadline | LocalDateTime | Y | 모집 마감 일시 (현재 시각 이후) |

#### RequestUpdateGroupOrderDto

| 필드 | 타입 | 필수 | 설명 |
|------|------|:----:|------|
| title | String | N | 수정할 제목 (1~100자), nullable |
| description | String | N | 수정할 내용 (1~1000자), nullable |
| deadline | LocalDateTime | N | 수정할 마감 일시 (현재 시각 이후), nullable |

#### ResponseGroupOrderDto

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | 게시글 고유 ID |
| title | String | 제목 |
| description | String | 내용 |
| targetCount | Integer | 목표 인원 |
| currentCount | Integer | 현재 참여 인원 |
| status | GroupOrderStatus | OPEN / CLOSED / EXPIRED |
| deadline | LocalDateTime | 마감 일시 |
| createdAt | LocalDateTime | 작성 일시 |
| authorId | Long | 작성자 ID |
| authorName | String | 작성자 이름 |

#### ResponseGroupOrderDetailDto

| 필드 | 타입 | 설명 |
|------|------|------|
| (ResponseGroupOrderDto 필드 모두 포함) | | |
| participants | List\<ResponseParticipantDto\> | 참여자 목록 |

#### ResponseParticipantDto

| 필드 | 타입 | 설명 |
|------|------|------|
| userId | Long | 참여자 ID |
| name | String | 참여자 이름 |
| joinedAt | LocalDateTime | 참여 신청 일시 |

---

> **신규 에러 코드 안내**: `ALREADY_PARTICIPATED`, `OWNER_CANNOT_PARTICIPATE`, `GROUP_ORDER_NOT_OPEN`, `PARTICIPANT_NOT_FOUND`는 신규 코드이므로 `ErrorCode.java`에 추가가 필요합니다.
>
> **다음 단계**: 명세 확정 후 `/feature` 스킬로 TDD 구현을 바로 시작할 수 있습니다.

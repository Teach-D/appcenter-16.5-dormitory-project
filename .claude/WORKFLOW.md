# UniDorm × Claude Code 워크플로 가이드

## 전체 흐름

```
/issue {기능 설명}
    └─ 기획 대화 → 이슈 생성 → 브랜치 생성 → checkout

/work
    └─ 작업 선택 → 구현 → git add → commit → push → 체크리스트 ✅

/work  (반복)

/add-task {새 작업}  (필요 시 작업 추가)

/close
    └─ PR 생성 → squash merge → dev checkout
```

---

## 명령어 레퍼런스

### 개발 워크플로

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `/issue {설명}` | 이슈 생성 + 브랜치 생성 + checkout | `/issue 팁 게시판 댓글 기능` |
| `/work` | 다음 작업 구현 → commit → push | `/work` |
| `/work {이슈번호}` | 특정 이슈 작업 진행 | `/work 552` |
| `/add-task {내용}` | 현재 이슈에 작업 추가 | `/add-task 댓글 신고 기능 추가` |
| `/close` | PR 생성 + merge | `/close` |

### 구현 도우미

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `/implementer {설명}` | 기능 구현 (이슈 없이 바로) | `/implementer FCM 알림 발송 기능` |
| `/new-domain {도메인명}` | 새 도메인 전체 스캐폴딩 | `/new-domain survey` |
| `/new-migration {설명}` | Flyway 마이그레이션 SQL 생성 | `/new-migration add tip comment table` |
| `/test {대상}` | 테스트 코드 작성 및 실행 | `/test TipCommentService` |

### 리뷰 & 분석

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `/review {대상}` | 코드 리뷰 (보안/성능/컨벤션) | `/review complaint 도메인` |
| `/debug {에러}` | 에러 원인 분석 및 해결 | `/debug LazyInitializationException in RoommateService` |
| `/api-spec {도메인}` | API 명세 정리 | `/api-spec roommate` |

### PR & Git

| 명령어 | 설명 | 예시 |
|--------|------|------|
| `/pr` | PR 생성 (이슈 없이 현재 브랜치 기준) | `/pr` |
| `/finish` | 현재 변경사항 커밋 준비 | `/finish` |

---

## 컨벤션 요약

### 브랜치
```
teach/{활동}/{설명}-{이슈번호}

teach/feat/tip-comment-552
teach/fix/fcm-token-545
teach/refactor/roommate-service-561
```

### 커밋 메시지
```
{type}: {제목} #{이슈번호}

feat: 팁 게시판 댓글 기능 추가 #552
fix: FCM 토큰 중복 저장 오류 수정 #545
refactor: 룸메이트 서비스 쿼리 개선 #561
docs: 민원 API swagger 업데이트 #548
chore: 빌드 설정 수정 #551
test: TipCommentService 단위 테스트 추가 #552
```
- `add:` 사용 금지 → `feat:` 으로 통일
- 이슈번호 필수, 제목 50자 이내

### 이슈
```
제목: [feat] 팁 게시판 댓글 기능

## 개요
팁 게시글에 댓글을 달 수 있는 기능을 추가한다.

## 작업 목록
- [ ] TipComment 엔티티 생성
- [ ] 댓글 CRUD API 구현
- [ ] SecurityConfig 권한 설정

## API 설계
| Method | URL                      | 권한 | 설명      |
|--------|--------------------------|------|-----------|
| POST   | /tips/{id}/comments      | USER | 댓글 작성 |
| GET    | /tips/{id}/comments      | USER | 댓글 조회 |
| DELETE | /tips/comments/{id}      | USER | 댓글 삭제 |
```
- chore/docs/refactor 이슈는 API 설계 섹션 생략

### PR
```
제목: feat: 팁 게시판 댓글 기능 추가 #552

## 개요
팁 게시글에 댓글을 달 수 있는 기능을 추가했습니다.

## 변경 사항
- [엔티티] TipComment 엔티티 추가
- [API] POST /tips/{id}/comments 댓글 작성 엔드포인트 추가
- [설정] SecurityConfig USER 권한 추가

## 테스트
- [ ] 로컬 빌드 확인 (`./gradlew build`)
- [ ] 댓글 작성/조회/삭제 API 동작 확인

closes #552
```

---

## 자주 쓰는 패턴

### 새 기능 개발 (처음부터 끝까지)
```
1. /issue 팁 게시판 댓글 기능
   → 기획 대화 후 이슈 #552 생성, teach/feat/tip-comment-552 브랜치 checkout

2. /work
   → "1. TipComment 엔티티 생성" 선택 → 구현 → commit → push

3. /work
   → "2. 댓글 CRUD API 구현" → 구현 → commit → push

4. /work
   → "3. SecurityConfig 권한 설정" → 구현 → commit → push

5. /close
   → PR #553 생성 → squash merge → dev checkout
```

### 중간에 작업 추가
```
/work 진행 중 새로운 요구사항 발생 시:

/add-task 댓글 최대 길이 200자 제한 추가
→ 이슈 #552 체크리스트에 추가됨
→ 다음 /work 시 목록에 포함
```

### 빠른 버그 수정
```
/issue FCM 토큰 중복 저장 오류
→ 이슈 생성 + 브랜치 생성

/work
→ 원인 파악 + 수정 + commit + push

/close
→ PR 생성 + merge
```

### 에러 발생 시
```
/debug [에러 메시지 또는 스택 트레이스 붙여넣기]
→ 관련 파일 자동 탐색 + 원인 분석 + 수정 제안
```

---

## 에이전트 (Claude가 자동으로 활용)

| 에이전트 | 역할 | 자동 호출 시점 |
|---------|------|--------------|
| `domain-expert` | 도메인 모델 설계, 비즈니스 로직 분석 | 복잡한 기능 설계 시 |
| `spring-reviewer` | 보안/JPA/트랜잭션 코드 리뷰 | `/review` 실행 시 |
| `db-migration-helper` | Flyway 마이그레이션 생성 | `/new-migration` 실행 시 |

---

## 자동화 (Hooks)

| 트리거 | 동작 |
|--------|------|
| `.java` 파일 수정 (Edit/Write) | `./gradlew compileJava -q` 자동 실행 → 컴파일 에러 즉시 감지 |

---

## GitHub MCP 연동

Claude Code가 직접 수행할 수 있는 GitHub 작업:
- 이슈 생성 / 조회 / 업데이트 (체크리스트 ✅)
- 브랜치 생성
- PR 생성 / 머지
- PR 리뷰 코멘트 조회

토큰 만료 시 재발급 후:
```
setx GITHUB_PERSONAL_ACCESS_TOKEN "ghp_새토큰"
```
→ Claude Code 재시작

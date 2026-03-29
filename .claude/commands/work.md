현재 이슈의 다음 작업을 구현해줘. $ARGUMENTS

## 진행 순서

### 1단계: 현재 이슈 확인
`.claude/issues.md`를 읽어서 현재 작업 이슈와 남은 작업 목록 파악.

$ARGUMENTS 에 이슈 번호가 있으면 해당 이슈 기준으로 진행.
없으면 issues.md의 현재 이슈 기준.

GitHub MCP `get_issue` 도구로 최신 체크리스트 상태 조회
(issues.md와 GitHub 이슈가 다르면 GitHub 기준으로 따름)

남은 작업 목록 출력:
```
## #{번호} {이슈 제목}

남은 작업:
1. [ ] {작업A}
2. [ ] {작업B}
3. [ ] {작업C}

어떤 작업을 진행할까요? (번호 입력 또는 "다음"으로 순서대로 진행)
```

### 2단계: 작업 선택
사용자가 번호를 선택하거나 "다음"이라고 하면 해당 작업 구현 시작.

### 3단계: 관련 코드 분석
- 작업과 관련된 도메인 기존 코드 읽기
- 영향받는 파일 파악

구현 전 계획 출력:
```
## 구현 계획: {작업명}
- 생성 파일: [목록]
- 수정 파일: [목록]
- DB 변경: 있음/없음
```

### 4단계: 구현
프로젝트 컨벤션에 따라 구현:

**엔티티**: `BaseTimeEntity` 상속, 정적 팩토리 `create()`, `@NoArgsConstructor(access = PROTECTED)`
**Service**: `@Transactional`, 조회는 `readOnly = true`
**Controller**: `implements {Domain}ApiSpecification`, `@TrackApi`
**DTO**: `Request{Action}{Entity}Dto` / `Response{Entity}Dto`
**예외**: `global/exception/` 커스텀 예외, `.orElseThrow()`
**Swagger**: `ApiSpecification` 인터페이스에 분리

SecurityConfig 권한 설정 및 Flyway 마이그레이션 파일 필요 시 생성.

### 5단계: git add → commit → push
구현 완료 후 자동으로 진행:

1. 변경 파일 확인 및 출력
2. "git add 할까요?" 확인
3. 승인 시 변경된 파일 명시적으로 add (`git add .` 금지)
4. 커밋 메시지 3개 제안:
   ```
   1) {type}: {제목} #{이슈번호}
   2) {type}: {다른 표현} #{이슈번호}
   3) {다른 type}: {또 다른 표현} #{이슈번호}
   ```

   **type 규칙** (이 프로젝트 컨벤션):
   - `feat` 새 기능 / `fix` 버그 수정 / `refactor` 코드 개선
   - `chore` 설정/도구 / `docs` 문서·Swagger / `test` 테스트 / `style` 포맷팅
   - `add:` 사용 금지 → `feat:`으로 대체
   - 한글 허용, 제목 50자 이내, 이슈번호 필수
5. 선택하면 commit 후 push:
   ```bash
   git push origin {현재 브랜치}
   ```

### 6단계: 이슈 체크리스트 업데이트 (GitHub MCP)
`get_issue`로 현재 본문 조회 후, 완료된 작업 항목을 `- [x]`로 변경하여 `update_issue`로 업데이트.

`.claude/issues.md`도 동일하게 업데이트.

### 7단계: 다음 작업 안내
```
## 작업 완료: {작업명}

진행 상황: {완료 수}/{전체 수}
- [x] {완료된 작업}
- [ ] {남은 작업1}
- [ ] {남은 작업2}

다음 작업을 진행하려면 `/work`를 입력하세요.
모든 작업이 완료되면 `/close`로 PR 및 머지를 진행하세요.
```

남은 작업이 없으면:
```
🎉 모든 작업이 완료되었습니다!
`/close`로 PR 생성 및 머지를 진행하세요.
```

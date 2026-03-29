다음 기능을 기획부터 머지까지 전체 워크플로로 진행해줘: $ARGUMENTS

## 프로젝트 정보
- GitHub: Teach-D/appcenter-16.5-dormitory-project
- base 브랜치: dev
- 브랜치 네이밍: {닉네임}/{활동}/{설명}-{이슈번호}
  - 닉네임: teach
  - 활동: feat / fix / refactor / chore / docs
  - 설명: kebab-case 영문 (예: user-update, tip-comment)
  - 이슈번호: GitHub 이슈 번호
  - 전체 예시: teach/fix/user-update-513, teach/feat/tip-comment-552

---

## 전체 워크플로

### Step 1: GitHub 이슈 생성 (GitHub MCP)
기능 설명을 바탕으로 이슈를 생성한다.

이슈 형식:
- **제목**: `[{type}] {기능 설명}` (예: `[feat] 팁 게시판 댓글 기능 추가`)
- **본문**:
  ```
  ## 개요
  (기능 목적 2~3줄)

  ## 구현 내용
  - [ ] (세부 작업 항목들)

  ## API 설계
  | Method | URL | 설명 |
  |--------|-----|------|
  | ...    | ... | ...  |
  ```
- **Labels**: feat/fix/refactor/chore 중 해당 항목

생성된 이슈 번호를 기억한다.

### Step 2: 브랜치 생성 및 체크아웃 (GitHub MCP + Bash)

GitHub MCP로 원격 브랜치 생성:
- base: dev
- 브랜치명: teach/{type}/{이슈-설명}-{이슈번호}
  (예: teach/feat/tip-comment-123)

로컬 체크아웃:
```bash
git fetch origin
git checkout -b teach/{type}/{설명}-{이슈번호} origin/teach/{type}/{설명}-{이슈번호}
```

### Step 3: 기능 구현
아래 프로젝트 컨벤션에 따라 구현:

**엔티티**
```java
@Entity @Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "table_name")
public class EntityName extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public static EntityName create(...) { ... }
    public void update(...) { ... }
}
```

**Service**: `@Transactional`, 조회는 `@Transactional(readOnly = true)`

**Controller**: `implements {Domain}ApiSpecification`, `@TrackApi`

**DTO 네이밍**: `Request{Action}{Entity}Dto`, `Response{Entity}Dto`

**예외**: `global/exception/` 커스텀 예외, `.orElseThrow()`

SecurityConfig 권한 설정 및 Flyway 마이그레이션 파일도 필요 시 생성.

### Step 4: Git 커밋 (Bash)
변경 파일 확인 후 스테이징 및 커밋:
```bash
git status
git add src/
git add .claude/  # claude 설정 변경 있을 경우
git commit -m "{type}: {제목} #{이슈번호}"
```

**type 규칙**: `feat` 새 기능 / `fix` 버그 수정 / `refactor` 코드 개선 / `chore` 설정 / `docs` 문서 / `test` 테스트 / `style` 포맷팅
**제목 규칙**: 한글 허용, 50자 이내, `add:` 사용 금지 → `feat:`으로 대체

### Step 5: PR 생성 (GitHub MCP)
PR 형식:
- **제목**: `{type}: {기능 설명} #{이슈번호}`
- **base**: dev
- **head**: 현재 브랜치
- **본문**:
  ```
  ## 개요
  (변경 목적)

  ## 변경 사항
  - (주요 변경 파일 및 내용)

  ## 테스트
  - [ ] 로컬 빌드 확인
  - [ ] (기능별 확인 항목)

  closes #{이슈번호}
  ```

### Step 6: 머지 여부 확인
PR 생성 후 사용자에게 확인:
> "PR #{번호}가 생성되었습니다. 바로 머지할까요? (dev 브랜치로 squash merge)"

승인 시 GitHub MCP로 squash merge 진행.

---

## 완료 보고

```
## 워크플로 완료

- 이슈: #{번호} - {제목}
- 브랜치: {브랜치명}
- 커밋: {커밋 메시지}
- PR: #{번호} - {상태(생성됨/머지됨)}
```

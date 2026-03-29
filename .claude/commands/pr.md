현재 브랜치의 변경사항을 분석해서 PR을 생성해줘. $ARGUMENTS

## 프로젝트 정보
- GitHub: Teach-D/appcenter-16.5-dormitory-project
- base 브랜치: dev

## 작업 순서

### 1단계: 변경사항 파악
```bash
git branch --show-current
git diff dev...HEAD --name-only
git log dev...HEAD --oneline
```

### 2단계: 변경된 파일 읽기
변경된 주요 파일들을 실제로 읽어서 내용 파악.

### 3단계: PR 초안 출력 후 확인 요청

**PR 제목**: `{type}: {한 줄 요약} #{이슈번호}`
- type: feat / fix / refactor / chore / docs / test / style
- 70자 이내, 이슈번호 필수

**PR 본문 (구조 B)**:
```
## 개요
{변경 목적을 2~3줄로 설명. 이슈의 개요를 기반으로 작성.}

## 변경 사항
- {도메인/레이어별로 그룹핑해서 bullet. 예: [엔티티] TipComment 추가}
- {예: [API] POST /tips/{id}/comments 댓글 작성 엔드포인트 추가}
- {예: [설정] SecurityConfig USER 권한 추가}

## 테스트
- [ ] 로컬 빌드 확인 (`./gradlew build`)
- [ ] {기능별 확인 항목. 예: 댓글 작성/조회/삭제 API 동작 확인}

closes #{이슈번호}
```

"위 내용으로 PR을 생성할까요?" 라고 물어보고 승인을 받는다.

### 4단계: GitHub MCP로 PR 생성 (승인 후)
`create_pull_request` 도구 사용:
- owner: Teach-D
- repo: appcenter-16.5-dormitory-project
- head: 현재 브랜치
- base: dev
- title: 위에서 작성한 제목
- body: 위에서 작성한 본문

### 5단계: 머지 여부 확인
PR 생성 완료 후:
> "PR #{번호}가 생성되었습니다. 바로 dev에 머지할까요?"

승인 시 `merge_pull_request` 도구로 **squash merge** 진행.

## 주의사항
- 제목은 70자 이내
- 보안/인증 관련 변경이 있으면 반드시 본문에 언급
- 미커밋 변경사항이 있으면 먼저 커밋 권고

---
name: GitHub Repository Info
description: UniDorm 프로젝트의 GitHub 저장소 기본 정보 및 이슈/브랜치 컨벤션
type: project
---

저장소: Teach-D/appcenter-16.5-dormitory-project
기본 브랜치: dev (main 아님 — PR 및 브랜치 생성 시 dev를 베이스로 사용)
브랜치 형식: `teach/feat/{기능설명-영문-kebab-case}-{이슈번호}`
이슈 제목 형식: `[{type}] {기능 설명}` (예: `[feat] Mixpanel 서버 사이드 구현`)
커밋 형식: `{type}: {제목} #{이슈번호}`

**Why:** docs/github.md에 정의된 프로젝트 Git 컨벤션 준수
**How to apply:** 이슈 생성 시 제목은 `[feat]` 형식, 브랜치 생성 시 from_branch는 항상 `dev`로 지정

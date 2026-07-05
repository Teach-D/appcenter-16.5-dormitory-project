---
name: quickfix
description: 명세 파이프라인이 과한 소규모 수정(버그, 오타, 로그 추가 등)에 사용. "/quickfix", "빠르게 고쳐줘", "간단히 수정해줘", "오타 수정", "로그 추가" 등 명세 없이 즉시 수정 요청 시 사용한다.
argument-hint: [무엇을 고칠지]
allowed-tools: Read, Edit, Bash(./gradlew *)
---
# 경량 수정

$ARGUMENTS 를 최소 변경으로 수정하라.

- 관련 테스트가 있으면 실행해 회귀 없음을 확인.
- 새 기능/엔드포인트가 필요하다고 판단되면 여기서 멈추고 `/specify` 로 전환하라.

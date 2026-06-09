#!/bin/bash
# PostToolUse hook: Edit|Write (asyncRewake)
# .java 파일 수정 시 해당 도메인 테스트 자동 실행
# 실패 시 exit 2 → Claude에게 테스트 실패 알림

INPUT=$(cat)
FILE=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('file_path',''))" 2>/dev/null)
[ -z "$FILE" ] && FILE=$(echo "$INPUT" | sed -n 's/.*"file_path":"\([^"]*\)".*/\1/p')

echo "$FILE" | grep -qiE '\.java$' || exit 0

# Windows 경로 정규화 (백슬래시 → 슬래시)
FILE_NORM=$(echo "$FILE" | tr '\\' '/')

# 도메인 추출 (예: .../domain/complaint/... → complaint)
DOMAIN=$(echo "$FILE_NORM" | grep -oE '/domain/[^/]+' | head -1 | sed 's|/domain/||')

[ -z "$DOMAIN" ] && exit 0

echo "[test] domain: $DOMAIN"
./gradlew test --tests "com.example.appcenter_project.domain.${DOMAIN}.*" 2>&1 || exit 2

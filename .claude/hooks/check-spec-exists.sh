#!/usr/bin/env bash
set -uo pipefail

INPUT=$(cat)

FILE_PATH=$(echo "$INPUT" | sed -n 's/.*"file_path"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
[ -z "$FILE_PATH" ] && exit 0

NORM=$(echo "$FILE_PATH" | tr '\\' '/')

case "$NORM" in
    *"/src/main/java/"*".java") ;;
    *) exit 0 ;;
esac

case "$NORM" in
    *"/global/"*|*"/common/"*|*"/shared/"*) exit 0 ;;
esac

BRANCH=$(git branch --show-current 2>/dev/null || true)
BR_NUM=$(echo "$BRANCH" | grep -oE '[0-9]+' | head -1)
[ -z "$BR_NUM" ] && exit 0

if ! ls -d specs/BR-${BR_NUM}-*/ >/dev/null 2>&1; then
    echo "[check-spec] BR-${BR_NUM} 명세 폴더 없음: specs/BR-${BR_NUM}-*/" >&2
    echo "[check-spec] CLAUDE.md 규칙 5: 명세가 진실의 원천. /specify → /design → /api-spec 후 구현." >&2
    echo "[check-spec] 대상 파일: $NORM" >&2
    # 초기엔 경고만. 오탐 없다 판단되면 아래를 exit 2로 승격
    exit 0
fi

exit 0

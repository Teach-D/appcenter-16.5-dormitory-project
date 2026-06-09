#!/bin/bash
# PostToolUse hook: Edit|Write
# .java 파일 수정 시 즉시 compileJava 실행 (동기)

INPUT=$(cat)
FILE=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('file_path',''))" 2>/dev/null)
[ -z "$FILE" ] && FILE=$(echo "$INPUT" | sed -n 's/.*"file_path":"\([^"]*\)".*/\1/p')

echo "$FILE" | grep -qiE '\.java$' || exit 0

echo "[compile] $FILE"
./gradlew compileJava

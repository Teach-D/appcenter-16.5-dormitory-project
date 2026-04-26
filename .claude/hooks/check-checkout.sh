#!/bin/bash
# PreToolUse hook for Edit and Write tools
# Blocks file editing until local branch checkout is completed

STATE_FILE=".claude/tmp/pending_checkout"

if [ -f "$STATE_FILE" ]; then
    branch=$(cat "$STATE_FILE")
    echo "[차단] 로컬 브랜치 checkout이 완료되지 않았습니다."
    echo "아래 명령을 Bash 툴로 실행한 뒤 다시 시도하세요:"
    echo "  git fetch origin && git checkout $branch && rm $STATE_FILE"
    exit 2
fi

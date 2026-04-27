#!/bin/bash
# PostToolUse hook for mcp__github__create_branch
# Writes branch name to state file to enforce local checkout before editing

data=$(cat)
branch=$(echo "$data" | python3 -c \
  "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('branch',''))" \
  2>/dev/null)

if [ -n "$branch" ]; then
    mkdir -p .claude/tmp
    echo "$branch" > .claude/tmp/pending_checkout
    echo "[hook] 브랜치 생성 완료: $branch"
    echo "[hook] 로컬 checkout 전까지 Edit/Write가 차단됩니다."
    echo "[hook] 아래 명령을 Bash 툴로 실행하세요:"
    echo "  git fetch origin && git checkout $branch && rm .claude/tmp/pending_checkout"
fi
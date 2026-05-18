#!/bin/bash
# Pre-tool safety check for Bash commands
# Hook: PreToolUse (matcher: Bash)
#
# Claude가 Bash 명령을 실행하기 전에 위험 여부를 검사.
#
# Exit convention (Claude Code hook protocol):
#   exit 2 + stderr → 차단. Claude Code가 stderr를 차단 이유로 표시.
#   exit 0          → 허용. stderr는 Claude Code가 버리므로 audit.log에만 기록.

INPUT=$(cat)

# JSON 파싱 — python3 우선, 없으면 sed fallback (프로젝트 컨벤션)
COMMAND=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('command',''))" 2>/dev/null)
[ -z "$COMMAND" ] && COMMAND=$(echo "$INPUT" | sed -n 's/.*"command"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
[ -z "$COMMAND" ] && COMMAND="$INPUT"

# Audit log — WARN 은 Claude Code가 stderr를 버리므로 log가 유일한 기록 수단
LOG_DIR="${CLAUDE_PROJECT_DIR:-$(pwd)}/.claude/hooks"
LOG_FILE="$LOG_DIR/audit.log"
mkdir -p "$LOG_DIR" 2>/dev/null

log_decision() {
  echo "$(date -u +%FT%TZ) [$1] $COMMAND" >> "$LOG_FILE"
}

# ── BLOCK: 시스템 파괴 명령 ────────────────────────────────────────────────────
BLOCKED_PATTERNS=(
  "rm -rf /([[:space:]]|$)"   # 루트 삭제 (하위 경로는 허용)
  "rm -rf \*"                 # 전체 와일드카드 삭제
  "dd if=/dev/zero"
  "dd if=/dev/random"
  ":\(\)\{:\|:&\};:"          # Fork bomb
  "mkfs\."                    # 파일시스템 포맷
  "format c:"
)

for pattern in "${BLOCKED_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -qE "$pattern"; then
    log_decision "BLOCK:$pattern"
    echo "❌ 차단: 시스템 파괴 명령이 감지됐습니다." >&2
    echo "   패턴: $pattern" >&2
    echo "   명령: $COMMAND" >&2
    exit 2
  fi
done

# ── BLOCK: 보호 브랜치(main/dev) 강제 푸시 ────────────────────────────────────
# 조건 세 가지가 모두 일치해야 차단 (git push + force 플래그 + 보호 브랜치)
if echo "$COMMAND" | grep -qE "git push" && \
   echo "$COMMAND" | grep -qE "(--force|--force-with-lease|-f[[:space:]])" && \
   echo "$COMMAND" | grep -qE "\b(main|dev)\b"; then
  log_decision "BLOCK:force-push-protected-branch"
  echo "❌ 차단: 보호 브랜치(main/dev)에 강제 푸시는 금지됩니다." >&2
  echo "   명령: $COMMAND" >&2
  exit 2
fi

# ── BLOCK: Flyway Clean (DB 전체 오브젝트 삭제) ───────────────────────────────
if echo "$COMMAND" | grep -qE "(flywayClean|flyway.*clean|flyway:clean)"; then
  log_decision "BLOCK:flyway-clean"
  echo "❌ 차단: flywayClean은 DB의 모든 오브젝트를 삭제합니다." >&2
  echo "   의도한 작업이라면 직접 터미널에서 실행하세요." >&2
  exit 2
fi

# ── WARN: 고위험 명령 (허용하되 audit.log 기록) ───────────────────────────────
WARNING_PATTERNS=(
  "rm -rf"
  "git push.*(--force|--force-with-lease|-f )"
  "git reset --hard"
  "git clean -f"
  "git clean -fd"
  "chmod -R 777"
  "sudo rm"
  "DROP TABLE"
  "DROP DATABASE"
  "TRUNCATE"
  "flywayRepair"
  "flywayBaseline"
)

MATCHED=""
for pattern in "${WARNING_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -qiE "$pattern"; then
    MATCHED="${MATCHED:+$MATCHED, }$pattern"
  fi
done

if [ -n "$MATCHED" ]; then
  log_decision "WARN:$MATCHED"
  # stderr는 Claude Code가 exit 0 시 버리지만, 수동 실행 시 사람이 볼 수 있음
  echo "⚠️  고위험 명령 감지 (허용): $MATCHED" >&2
  echo "   명령: $COMMAND" >&2
else
  log_decision "ALLOW"
fi

exit 0

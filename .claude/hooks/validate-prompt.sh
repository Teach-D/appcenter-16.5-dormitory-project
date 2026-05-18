#!/bin/bash
# AI Prompt Firewall
# Hook: UserPromptSubmit
#
# Intercepts user prompts before reaching Claude and blocks/warns on:
#   1. Dangerous system operations
#   2. PII patterns (Korean national ID, credit card)
#   3. Production deployment without approval
#   4. Bulk user data deletion
#   5. Refactoring without a test directory
#
# Compatible with: macOS, Linux, Windows (Git Bash)

INPUT=$(cat)

# JSON 파싱 — python3 우선, 없으면 sed fallback
if command -v python3 &>/dev/null; then
  PROMPT=$(echo "$INPUT" | python3 -c "
import sys, json
try:
    data = json.load(sys.stdin)
    print(data.get('user_prompt') or data.get('prompt') or '', end='')
except Exception:
    pass
")
else
  PROMPT=$(echo "$INPUT" | sed -n 's/.*"user_prompt"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
  if [ -z "$PROMPT" ]; then
    PROMPT=$(echo "$INPUT" | sed -n 's/.*"prompt"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' | head -1)
  fi
fi

[ -z "$PROMPT" ] && exit 0

# ─── 1. 위험 시스템 명령 차단 ─────────────────────────────────────────────────
DANGEROUS_PATTERNS=(
  "rm -rf /"
  "rm -rf ~"
  "delete database"
  "drop database"
  "drop table"
  "truncate table"
  "format disk"
  "dd if="
  "chmod -R 777 /"
  "shutdown -h"
  "reboot"
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
  if echo "$PROMPT" | grep -qi "$pattern"; then
    printf '{"decision": "block", "reason": "Dangerous operation detected: %s"}' "$pattern"
    exit 0
  fi
done

# ─── 2. PII 감지 차단 ─────────────────────────────────────────────────────────
# 주민등록번호: 6자리-7자리 (앞자리 뒷자리 1~4 시작)
if echo "$PROMPT" | grep -qE '[0-9]{6}-[1-4][0-9]{6}'; then
  echo '{"decision": "block", "reason": "PII detected: Korean national ID number pattern. Remove sensitive data before sending."}'
  exit 0
fi

# 신용카드: 4자리 블록 4개
if echo "$PROMPT" | grep -qE '[0-9]{4}[[:space:]-][0-9]{4}[[:space:]-][0-9]{4}[[:space:]-][0-9]{4}'; then
  echo '{"decision": "block", "reason": "PII detected: Credit card number pattern. Remove sensitive data before sending."}'
  exit 0
fi

# ─── 3. Production 배포 차단 ──────────────────────────────────────────────────
if echo "$PROMPT" | grep -qiE "(deploy|push|release|rollout).*(production|prod|운영)"; then
  if [ ! -f ".deployment-approved" ]; then
    echo '{"decision": "block", "reason": "Production deployment requires approval. Create .deployment-approved file to proceed."}'
    exit 0
  fi
fi

# ─── 4. 대량 사용자 데이터 삭제 경고 ─────────────────────────────────────────
if echo "$PROMPT" | grep -qiE "(delete|remove|wipe|purge|truncate).*(user|users|member|account|personal)"; then
  printf '{"additionalContext": "Warning: Bulk user data deletion detected. Ensure compliance with data retention policy before proceeding."}'
  exit 0
fi

# ─── 5. 테스트 없는 리팩터링 경고 ────────────────────────────────────────────
if echo "$PROMPT" | grep -qi "refactor"; then
  if [ ! -d "tests" ] && [ ! -d "test" ] && [ ! -d "src/test" ]; then
    printf '{"additionalContext": "Warning: Refactoring without tests may be risky. Consider writing tests first."}'
  fi
fi

exit 0

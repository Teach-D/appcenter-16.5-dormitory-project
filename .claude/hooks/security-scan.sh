#!/bin/bash
# Security scan before git add
# Hook: PreToolUse (matcher: Bash)
#
# git add 명령 감지 시 스테이징 대상 파일을 정적 분석.
# CRITICAL(private key, AWS key 등) → exit 2 차단
# WARN(하드코딩 자격증명, SQL injection 등) → additionalContext 경고 후 허용

INPUT=$(cat)

# Bash 명령 추출 (프로젝트 컨벤션)
CMD=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('command',''))" 2>/dev/null)
[ -z "$CMD" ] && CMD=$(echo "$INPUT" | sed -n 's/.*"command":"\([^"]*\)".*/\1/p' | head -1)

# git add 명령이 아니면 스킵
echo "$CMD" | grep -qE "^git add" || exit 0

# ── 스캔 대상 파일 결정 ────────────────────────────────────────────────────────
if echo "$CMD" | grep -qE "git add (\.|--all|-A|-u)"; then
  # git add . / -A / -u → 수정된 추적 파일 + 새 파일
  STAGED_FILES=$(git diff --name-only 2>/dev/null)
  NEW_FILES=$(git ls-files --others --exclude-standard 2>/dev/null)
  FILES=$(printf "%s\n%s" "$STAGED_FILES" "$NEW_FILES" | grep -v '^$' | sort -u)
else
  # git add <files> → 명령에서 경로 추출 (플래그 제외)
  FILES=$(echo "$CMD" | sed 's/^git add//' | tr ' ' '\n' | grep -vE '^-|^$' | sort -u)
fi

[ -z "$FILES" ] && exit 0

# Audit log
LOG_FILE="${CLAUDE_PROJECT_DIR:-$(pwd)}/.claude/hooks/audit.log"

CRITICAL=""
WARN=""

scan_file() {
  local f="$1"
  [ -z "$f" ] || [ ! -f "$f" ] && return

  # 바이너리·빌드 산출물·훅 스크립트 스킵
  case "$f" in
    *.png|*.jpg|*.jpeg|*.gif|*.svg|*.ico|*.woff*|*.ttf|*.eot|*.class|*.jar) return ;;
    */build/*|*/.gradle/*|*/.git/*|*/node_modules/*) return ;;
    */.claude/hooks/*) return ;;
  esac

  # ── CRITICAL ──────────────────────────────────────────────────────────────
  # Private key — PEM 헤더 형식(-----BEGIN ... PRIVATE KEY-----)만 매칭
  if grep -qlE "^-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----" "$f" 2>/dev/null; then
    CRITICAL="${CRITICAL}  [CRITICAL] $f : Private key 포함\n"
  fi

  # AWS access key (AKIA...)
  if grep -qE "AKIA[0-9A-Z]{16}" "$f" 2>/dev/null; then
    CRITICAL="${CRITICAL}  [CRITICAL] $f : AWS Access Key ID 패턴 감지\n"
  fi

  # .env 파일에 실제 값이 있는 경우
  if echo "$f" | grep -qE "(\.env$|\.env\.)"; then
    if grep -qvE "^[[:space:]]*(#|$)|=[[:space:]]*$" "$f" 2>/dev/null; then
      CRITICAL="${CRITICAL}  [CRITICAL] $f : .env 파일에 실제 값 포함 — .gitignore 확인 필요\n"
    fi
  fi

  # ── WARN ──────────────────────────────────────────────────────────────────
  # application.yml/properties 하드코딩
  if echo "$f" | grep -qiE "application[-_.]?(local|dev|prod|test)?\.(yml|yaml|properties)$"; then
    if grep -qiE "password:[[:space:]]+['\"]?[a-zA-Z0-9!@#\$%^&*]" "$f" 2>/dev/null; then
      WARN="${WARN}  [WARN] $f : password 하드코딩 의심 (환경변수 권장)\n"
    fi
    if grep -qiE "(jwt[-._]secret|secret[-._]key):[[:space:]]+['\"]?[a-zA-Z0-9]" "$f" 2>/dev/null; then
      WARN="${WARN}  [WARN] $f : JWT secret 하드코딩 의심\n"
    fi
    if grep -qiE "datasource.url.*://[^$].*:[^$].*@" "$f" 2>/dev/null; then
      WARN="${WARN}  [WARN] $f : DB URL에 자격증명 포함 의심\n"
    fi
  fi

  # Java: SQL 문자열 연결 (SQL injection 위험)
  if echo "$f" | grep -qE "\.java$"; then
    if grep -qE '"[[:space:]]*(SELECT|INSERT|UPDATE|DELETE|WHERE|FROM)[^"]*"[[:space:]]*\+' "$f" 2>/dev/null; then
      WARN="${WARN}  [WARN] $f : SQL 문자열 연결 감지 — Prepared Statement 또는 QueryDSL 사용 권장\n"
    fi
    # 로그에 민감 데이터 출력
    if grep -qiE '\blog\.(info|debug|warn|error)\(.*\b(password|secret|token|key)\b' "$f" 2>/dev/null; then
      WARN="${WARN}  [WARN] $f : 로그에 민감 데이터(password/secret/token/key) 출력 의심\n"
    fi
    # 테스트 코드에서만 허용되는 하드코딩 패턴 제외
    if ! echo "$f" | grep -qE "src/test/"; then
      if grep -qiE '(password|passwd)\s*=\s*"[^"]{3,}"' "$f" 2>/dev/null; then
        WARN="${WARN}  [WARN] $f : Java 코드에 password 문자열 하드코딩 의심\n"
      fi
    fi
  fi
}

while IFS= read -r file; do
  scan_file "$file"
done <<< "$FILES"

# ── CRITICAL → 차단 ─────────────────────────────────────────────────────────
if [ -n "$CRITICAL" ]; then
  echo "$(date -u +%FT%TZ) [SECURITY-BLOCK] $CMD" >> "$LOG_FILE"
  printf "$CRITICAL" >> "$LOG_FILE"
  echo "❌ 보안 스캔 실패 — git add가 차단됐습니다." >&2
  echo "" >&2
  printf "$CRITICAL" >&2
  echo "" >&2
  echo "민감한 정보는 환경변수 또는 application-local.yml (.gitignore 등록)로 분리하세요." >&2
  exit 2
fi

# ── WARN → 허용 + additionalContext ─────────────────────────────────────────
if [ -n "$WARN" ]; then
  echo "$(date -u +%FT%TZ) [SECURITY-WARN] $CMD" >> "$LOG_FILE"
  printf "$WARN" >> "$LOG_FILE"

  # additionalContext 로 Claude에게 경고 전달
  WARN_MSG=$(printf "$WARN" | tr '\n' ' ' | sed 's/"/\\"/g')
  printf '{"additionalContext": "보안 스캔 경고: %s환경변수로 분리하거나 로직을 개선하세요."}' "$WARN_MSG"
fi

exit 0

#!/bin/bash
# Auto-format code after writing/editing
# Hook: PostToolUse:Edit|Write
#
# Runs a language-specific formatter in-place after Claude writes or edits a file.
# Silently exits 0 on any failure — formatting is a best-effort enhancement.

INPUT=$(cat)

# JSON 파싱 — post-edit-compile.sh 와 동일한 패턴
FILE=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('file_path',''))" 2>/dev/null)
[ -z "$FILE" ] && FILE=$(echo "$INPUT" | sed -n 's/.*"file_path":"\([^"]*\)".*/\1/p')

[ -z "$FILE" ] || [ ! -f "$FILE" ] && exit 0

# Flyway 마이그레이션 파일 포맷 금지 (Flyway 체크섬 오류 방지)
if echo "$FILE" | grep -qE "db/migration/V[0-9]"; then
  exit 0
fi

case "$FILE" in
  *.java)
    if command -v google-java-format &>/dev/null; then
      google-java-format -i "$FILE" 2>/dev/null && echo "[format] $FILE (google-java-format)"
    fi
    ;;
  *.py)
    if command -v ruff &>/dev/null; then
      ruff format -q "$FILE" 2>/dev/null && echo "[format] $FILE (ruff)"
    elif command -v black &>/dev/null; then
      black -q "$FILE" 2>/dev/null && echo "[format] $FILE (black)"
    fi
    ;;
  *.js|*.jsx|*.ts|*.tsx)
    if command -v prettier &>/dev/null; then
      prettier --write --log-level=error "$FILE" 2>/dev/null && echo "[format] $FILE (prettier)"
    fi
    ;;
  *.go)
    if command -v gofmt &>/dev/null; then
      gofmt -w "$FILE" 2>/dev/null && echo "[format] $FILE (gofmt)"
    fi
    ;;
  *.rs)
    if command -v rustfmt &>/dev/null; then
      rustfmt "$FILE" 2>/dev/null && echo "[format] $FILE (rustfmt)"
    fi
    ;;
esac

exit 0

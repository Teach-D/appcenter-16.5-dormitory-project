#!/usr/bin/env bash
set -uo pipefail

CHANGED=$(git diff --name-only --diff-filter=AM HEAD 2>/dev/null | grep '\.java$' || true)
UNTRACKED=$(git ls-files --others --exclude-standard 2>/dev/null | grep '\.java$' || true)

if [ -z "$CHANGED" ] && [ -z "$UNTRACKED" ]; then
    exit 0
fi

echo "[on-stop] Java 변경 감지 → compileJava" >&2
if ! ./gradlew compileJava --quiet >&2; then
    echo "[on-stop] compileJava 실패. 위 오류를 먼저 고쳐라." >&2
    exit 2
fi

echo "[on-stop] compileJava 통과" >&2
exit 0

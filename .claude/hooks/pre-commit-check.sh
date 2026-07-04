#!/usr/bin/env bash
set -uo pipefail

INPUT=$(cat)

case "$INPUT" in
    *"git commit"*) ;;
    *) exit 0 ;;
esac

echo "[pre-commit] git commit 감지 → gradlew test" >&2
if ! ./gradlew test --quiet >&2; then
    echo "[pre-commit] test 실패. 커밋 중단." >&2
    exit 2
fi

echo "[pre-commit] test 통과" >&2
exit 0

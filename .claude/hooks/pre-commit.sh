#!/bin/bash
# Pre-commit: run affected domain tests before git commit
# Hook: PreToolUse (matcher: Bash)
# git commit 명령 감지 시 스테이징된 .java 파일의 도메인 테스트를 자동 실행.
# 테스트 실패 시 exit 2 로 커밋 차단.

INPUT=$(cat)

# Bash 명령 추출 (post-edit-compile.sh 와 동일한 패턴)
CMD=$(echo "$INPUT" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tool_input',{}).get('command',''))" 2>/dev/null)
[ -z "$CMD" ] && CMD=$(echo "$INPUT" | sed -n 's/.*"command":"\([^"]*\)".*/\1/p' | head -1)

# git commit 명령이 아니면 스킵 (--no-verify 는 차단 불가이므로 그냥 pass)
echo "$CMD" | grep -qiE "git\s+commit" || exit 0

# 스테이징된 .java 파일 목록 (git은 항상 슬래시 사용)
STAGED=$(git diff --cached --name-only 2>/dev/null | grep '\.java$')

if [ -z "$STAGED" ]; then
  exit 0
fi

# domain/{DOMAIN}/ 경로에서 도메인 이름 추출
DOMAINS=$(echo "$STAGED" | grep -oE '(domain|global)/[^/]+' | sed 's|.*/||' | sort -u)

if [ -z "$DOMAINS" ]; then
  exit 0
fi

# 테스트 디렉토리가 존재하는 도메인만 필터링
GRADLE_ARGS=("test" "--continue")
TARGETS=()

for d in $DOMAINS; do
  # domain 패키지와 global 패키지 모두 지원
  if ls src/test/java/com/example/appcenter_project/domain/"$d"/**/*Test.java 2>/dev/null | grep -q .; then
    GRADLE_ARGS+=("--tests" "com.example.appcenter_project.domain.${d}.*")
    TARGETS+=("$d")
  elif ls src/test/java/com/example/appcenter_project/global/"$d"/**/*Test.java 2>/dev/null | grep -q .; then
    GRADLE_ARGS+=("--tests" "com.example.appcenter_project.global.${d}.*")
    TARGETS+=("global/$d")
  fi
done

# 테스트 대상 없으면 통과
if [ ${#TARGETS[@]} -eq 0 ]; then
  echo "[pre-commit] 스테이징 파일에 대응하는 테스트 없음 — 커밋 진행."
  exit 0
fi

echo "[pre-commit] 테스트 대상 도메인: ${TARGETS[*]}"
echo "[pre-commit] ./gradlew ${GRADLE_ARGS[*]}"
echo ""

./gradlew "${GRADLE_ARGS[@]}" 2>&1
RESULT=$?

if [ $RESULT -ne 0 ]; then
  echo ""
  echo "[pre-commit] 테스트 실패 — 커밋이 차단됐습니다."
  echo "[pre-commit] 수동 확인: ./gradlew test --tests 'com.example.appcenter_project.domain.{도메인}.*'"
  exit 2
fi

echo ""
echo "[pre-commit] 모든 테스트 통과. 커밋을 진행합니다."
exit 0

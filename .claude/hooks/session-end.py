#!/usr/bin/env python3
"""
Session End Hook — Records errors Claude encountered during the session to CLAUDE.md.

핵심 신호: transcript에서 is_error=true 인 tool_result 블록.
Claude가 도구를 실행했을 때 실제로 오류가 났던 케이스만 기록한다.

예) 훅 차단, 컴파일 오류, 파일 없음, 권한 오류 등

결과를 CLAUDE.md '## 세션 실패 기록' 섹션에 최신순 추가. 최대 5개 유지.
"""
import json
import os
import re
import sys
from datetime import datetime, timezone

CLAUDE_MD = "CLAUDE.md"
SECTION_HEADER = "## 세션 실패 기록"
MAX_SESSIONS = 5


# ── transcript 파싱 ────────────────────────────────────────────────────────────

def load_entries(path: str) -> list[dict]:
    if not path or not os.path.exists(path):
        return []
    entries = []
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for raw in f:
            raw = raw.strip()
            if not raw:
                continue
            try:
                entries.append(json.loads(raw))
            except json.JSONDecodeError:
                continue
    return entries


def collect_tool_uses(entries: list[dict]) -> dict[str, dict]:
    """tool_use_id → {name, input_summary} 맵 구성."""
    uses: dict[str, dict] = {}
    for entry in entries:
        msg = entry.get("message", {})
        if not isinstance(msg, dict):
            continue
        for block in _iter_blocks(msg.get("content", [])):
            if block.get("type") == "tool_use":
                tid = block.get("id", "")
                name = block.get("name", "")
                inp = block.get("input", {})
                # 대표 입력값 추출 (command / file_path / 첫 번째 값)
                summary = (
                    inp.get("command")
                    or inp.get("file_path")
                    or next(iter(inp.values()), "")
                )
                uses[tid] = {"name": name, "summary": str(summary)[:80]}
    return uses


def find_tool_errors(entries: list[dict], tool_uses: dict[str, dict]) -> list[dict]:
    """is_error=True 인 tool_result 블록을 수집."""
    errors: list[dict] = []
    seen: set[str] = set()

    for entry in entries:
        msg = entry.get("message", {})
        if not isinstance(msg, dict):
            continue
        for block in _iter_blocks(msg.get("content", [])):
            if block.get("type") != "tool_result":
                continue
            if not block.get("is_error"):
                continue

            tid = block.get("tool_use_id", "")
            tool_info = tool_uses.get(tid, {})

            # 오류 텍스트 추출
            raw = block.get("content", "")
            if isinstance(raw, str):
                error_text = raw
            elif isinstance(raw, list):
                parts = [
                    b.get("text", "")
                    for b in raw
                    if isinstance(b, dict) and b.get("type") == "text"
                ]
                error_text = " ".join(parts)
            else:
                error_text = ""

            error_text = error_text.strip()
            if not error_text:
                continue

            # 중복 제거: 같은 도구 + 오류 첫 60자
            dedup_key = f"{tool_info.get('name', '')}:{error_text[:60]}"
            if dedup_key in seen:
                continue
            seen.add(dedup_key)

            errors.append({
                "tool": tool_info.get("name", "unknown"),
                "input": tool_info.get("summary", ""),
                "error": _first_meaningful_line(error_text),
            })

    return errors


def _iter_blocks(content):
    """content(str or list)에서 블록 딕셔너리를 순회."""
    if isinstance(content, list):
        for block in content:
            if isinstance(block, dict):
                yield block


def _first_meaningful_line(text: str) -> str:
    """오류 텍스트에서 가장 의미 있는 첫 줄 추출 (최대 140자)."""
    for line in text.splitlines():
        line = line.strip()
        if len(line) > 10:
            return line[:140]
    return text[:140].strip()


# ── CLAUDE.md 업데이트 ────────────────────────────────────────────────────────

def build_entry(errors: list[dict], session_id: str) -> str:
    now = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M UTC")
    sid = (session_id or "unknown")[:8]
    lines = [f"\n### {now} (세션: {sid})\n"]

    for e in errors:
        tool = e["tool"]
        inp = f"({e['input']})" if e["input"] else ""
        err = e["error"]
        lines.append(f"- `{tool}{inp}` → `{err}`")

    lines.append("")
    return "\n".join(lines)


def trim_old_sessions(after_header: str) -> str:
    markers = [m.start() for m in re.finditer(r"\n### \d{4}-\d{2}-\d{2}", after_header)]
    if len(markers) >= MAX_SESSIONS:
        return after_header[: markers[MAX_SESSIONS - 1]]
    return after_header


def update_claude_md(errors: list[dict], session_id: str) -> None:
    if not errors:
        return
    if not os.path.exists(CLAUDE_MD):
        return

    try:
        with open(CLAUDE_MD, "r", encoding="utf-8") as f:
            content = f.read()

        entry = build_entry(errors, session_id)

        if SECTION_HEADER in content:
            idx = content.index(SECTION_HEADER) + len(SECTION_HEADER)
            after = trim_old_sessions(content[idx:])
            updated = content[:idx] + entry + after
        else:
            updated = content.rstrip() + f"\n\n{SECTION_HEADER}\n{entry}"

        with open(CLAUDE_MD, "w", encoding="utf-8") as f:
            f.write(updated)
    except (IOError, OSError):
        pass


# ── 진입점 ────────────────────────────────────────────────────────────────────

def main() -> None:
    try:
        data = json.load(sys.stdin)
    except json.JSONDecodeError:
        sys.exit(0)

    if data.get("hook_event_name") != "SessionEnd":
        sys.exit(0)

    session_id: str = data.get("session_id", "unknown")
    transcript_path: str = data.get("transcript_path", "")

    entries = load_entries(transcript_path)
    tool_uses = collect_tool_uses(entries)
    errors = find_tool_errors(entries, tool_uses)

    update_claude_md(errors, session_id)
    sys.exit(0)


if __name__ == "__main__":
    main()

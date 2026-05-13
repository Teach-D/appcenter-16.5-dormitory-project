#!/usr/bin/env python3
"""
Context Usage Tracker with Auto-Compact
Hook: UserPromptSubmit (pre) + Stop (post)

- Estimates token usage from transcript
- Prints a progress bar to stderr after every response
- Automatically triggers /compact when context >= 40%
"""
import json
import os
import sys
import tempfile

CONTEXT_LIMIT = 200_000      # Claude Sonnet 4.6 context window
AUTO_COMPACT_AT = 0.40       # trigger /compact at 40%
BAR_WIDTH = 20


def state_file(session_id: str) -> str:
    return os.path.join(tempfile.gettempdir(), f"claude-ctx-{session_id}.json")


def estimate_tokens(text: str) -> int:
    """~4 chars/token for English; Korean chars cost more so divide by 2 for CJK blocks."""
    ascii_chars = sum(1 for c in text if ord(c) < 128)
    cjk_chars = len(text) - ascii_chars
    return max((ascii_chars // 4) + (cjk_chars // 2), 1)


def read_transcript(path: str) -> str:
    if not path or not os.path.exists(path):
        return ""
    parts = []
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            try:
                entry = json.loads(line.strip())
                msg = entry.get("message", {})
                content = msg.get("content", "")
                if isinstance(content, str):
                    parts.append(content)
                elif isinstance(content, list):
                    for block in content:
                        if isinstance(block, dict) and block.get("type") == "text":
                            parts.append(block.get("text", ""))
            except (json.JSONDecodeError, AttributeError):
                continue
    return "\n".join(parts)


def progress_bar(pct: float) -> str:
    filled = int(pct / 100 * BAR_WIDTH)
    if pct >= 80:
        char = "█"
    elif pct >= 40:
        char = "▓"
    else:
        char = "░"
    return char * filled + "░" * (BAR_WIDTH - filled)


def on_user_prompt_submit(data: dict) -> None:
    tokens = estimate_tokens(read_transcript(data.get("transcript_path", "")))
    sf = state_file(data.get("session_id", "default"))
    with open(sf, "w") as f:
        json.dump({"pre_tokens": tokens}, f)


def on_stop(data: dict) -> None:
    current = estimate_tokens(read_transcript(data.get("transcript_path", "")))
    sf = state_file(data.get("session_id", "default"))

    pre = 0
    if os.path.exists(sf):
        try:
            with open(sf) as f:
                pre = json.load(f).get("pre_tokens", 0)
            os.remove(sf)
        except (json.JSONDecodeError, IOError, OSError):
            pass

    delta = current - pre
    pct = current / CONTEXT_LIMIT * 100
    remaining = CONTEXT_LIMIT - current

    bar = progress_bar(pct)
    print(f"[CTX] [{bar}] {pct:.1f}%  ~{current:,} / {CONTEXT_LIMIT:,} tokens", file=sys.stderr)
    if delta > 0:
        print(f"[CTX] This turn: +{delta:,}  |  Remaining: ~{remaining:,}", file=sys.stderr)

    if pct >= AUTO_COMPACT_AT * 100:
        print(f"[CTX] {pct:.1f}% >= {AUTO_COMPACT_AT*100:.0f}% — auto-triggering /compact", file=sys.stderr)
        print(json.dumps({"continuePrompt": "/compact"}))


def main():
    try:
        data = json.load(sys.stdin)
    except json.JSONDecodeError:
        sys.exit(0)

    event = data.get("hook_event_name", "")
    if event == "UserPromptSubmit":
        on_user_prompt_submit(data)
    elif event == "Stop":
        on_stop(data)

    sys.exit(0)


if __name__ == "__main__":
    main()

#!/usr/bin/env bash
set -euo pipefail

AGENT="codex"
ITERATIONS=5
CHECK_CMD="./gradlew :app:compileDebugKotlin"
AUTO_PUSH=false
STOP_ON_PASS=false

print_help() {
  cat <<'EOF'
Usage: ai_optimize_loop.sh [options]

Options:
  --agent codex|claude|opencode   AI agent adapter (default: codex)
  --iterations N                  Max optimization iterations (default: 5)
  --check-cmd "COMMAND"           Check command (default: ./gradlew :app:compileDebugKotlin)
  --auto-push                     Push after successful commit
  --stop-on-pass                  Stop immediately on first passing check
  -h, --help                      Show this help
EOF
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --agent)
      AGENT="${2:-}"
      shift 2
      ;;
    --iterations)
      ITERATIONS="${2:-}"
      shift 2
      ;;
    --check-cmd)
      CHECK_CMD="${2:-}"
      shift 2
      ;;
    --auto-push)
      AUTO_PUSH=true
      shift
      ;;
    --stop-on-pass)
      STOP_ON_PASS=true
      shift
      ;;
    -h|--help)
      print_help
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      print_help
      exit 1
      ;;
  esac
done

if ! [[ "$ITERATIONS" =~ ^[0-9]+$ ]] || [[ "$ITERATIONS" -le 0 ]]; then
  echo "--iterations must be a positive integer" >&2
  exit 1
fi

if [[ "$AGENT" != "codex" && "$AGENT" != "claude" && "$AGENT" != "opencode" ]]; then
  echo "--agent must be one of: codex, claude, opencode" >&2
  exit 1
fi

run_agent_prompt() {
  local prompt="$1"
  case "$AGENT" in
    codex)
      if command -v codex >/dev/null 2>&1; then
        codex exec "$prompt" || true
      else
        echo "[warn] codex CLI not found. Skipping prompt."
      fi
      ;;
    claude)
      if command -v claude >/dev/null 2>&1; then
        claude -p "$prompt" || true
      else
        echo "[warn] claude CLI not found. Skipping prompt."
      fi
      ;;
    opencode)
      if command -v opencode >/dev/null 2>&1; then
        opencode run "$prompt" || true
      else
        echo "[warn] opencode CLI not found. Skipping prompt."
      fi
      ;;
  esac
}

commit_if_changed() {
  if [[ -n "$(git status --porcelain)" ]]; then
    git add -A
    git commit -m "chore(ai-loop): pass checks via ${AGENT}" >/dev/null
    echo "[ok] committed passing iteration."
    if [[ "$AUTO_PUSH" == "true" ]]; then
      git push
      echo "[ok] pushed."
    fi
  else
    echo "[info] no changes to commit."
  fi
}

for (( i=1; i<=ITERATIONS; i++ )); do
  echo "=============================="
  echo "AI optimize iteration ${i}/${ITERATIONS}"
  echo "=============================="

  run_agent_prompt "Discover one concrete issue in this codebase and explain impact briefly."
  run_agent_prompt "Fix the discovered issue with minimal, clean code changes."
  run_agent_prompt "Improve related docs/code/tests for the fix to stay maintainable."
  run_agent_prompt "Refactor touched code while preserving behavior and readability."

  echo "[run] ${CHECK_CMD}"
  if bash -lc "${CHECK_CMD}"; then
    echo "[ok] checks passed."
    commit_if_changed
    if [[ "$STOP_ON_PASS" == "true" ]]; then
      echo "[done] stop-on-pass enabled."
      exit 0
    fi
  else
    echo "[fail] checks failed. Triggering AI re-fix phase."
    run_agent_prompt "Checks failed. Diagnose failure output and apply a focused re-fix."
  fi
done

echo "[done] reached max iterations: ${ITERATIONS}"

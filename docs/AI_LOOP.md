# AI Iterative Optimization Loop

## Goal

`tools/ai_optimize_loop.sh` automates a repetitive AI-assisted improvement cycle for this repository:

1. AI discovers an issue
2. AI proposes/fixes code
3. AI improves docs/code/tests
4. AI refactors
5. Run checks
6. If checks fail, AI re-fixes and loops
7. If checks pass, commit changes

## Script Entry

```bash
./tools/ai_optimize_loop.sh [options]
```

## Options

- `--agent codex|claude|opencode`
  - Selects AI adapter used for prompt execution
- `--iterations N`
  - Max loop iterations (default: `5`)
- `--check-cmd "COMMAND"`
  - Validation command executed every iteration
  - Example: `--check-cmd "./gradlew :app:compileDebugKotlin"`
- `--auto-push`
  - Pushes commit after passing checks
- `--stop-on-pass`
  - Stops loop immediately on first passing iteration
- `-h`, `--help`
  - Show help

## Behavior Notes

- The script commits only when checks pass.
- If no file changes exist, commit is skipped.
- `--auto-push` is optional and disabled by default.
- AI CLI availability is validated at runtime; missing CLI is reported clearly.

## Recommended Usage

```bash
./tools/ai_optimize_loop.sh \
  --agent codex \
  --iterations 3 \
  --check-cmd "./gradlew :app:compileDebugKotlin" \
  --stop-on-pass
```

## Team Workflow Suggestion

- Use small iteration counts in CI/local runs (`2~3`)
- Use stronger checks before release (`assembleDebug`, tests)
- Keep prompts focused (single defect/scope per loop)

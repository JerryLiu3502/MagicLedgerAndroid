# MagicLedger Android

A Kotlin + Jetpack Compose daily bill tracking app inspired by **“魔法记账”**.

## Tech Stack

- Kotlin, Android, Jetpack Compose, Material3
- MVVM + Repository
- Room + Coroutines + StateFlow

## Core MVP Features

- Add income/expense transaction (`amount`, `type`, `category`, `note`, `date`)
- Transaction list grouped by day (filtered by selected month)
- Monthly summary (income, expense, balance) with month switch
- Transaction edit/delete
- Category management (preset + custom)
- Category-level monthly statistics (income/expense ratio)
- Monthly budget setting with over-budget warning text
- Data export/import (JSON backup and restore)
- Built-in sample data for first launch

## Project Structure

- `app/` Android app module
- `docs/ARCHITECTURE.md` architecture notes
- `docs/AI_LOOP.md` AI iterative workflow guide
- `tools/ai_optimize_loop.sh` iterative optimization script

## Build & Run

1. Ensure Android SDK exists at:
   - `/Users/jerry/Library/Android/sdk`
2. `local.properties` is already configured with:
   - `sdk.dir=/Users/jerry/Library/Android/sdk`
3. Run checks:
   - `./gradlew :app:compileDebugKotlin`
4. Build debug APK:
   - `./gradlew :app:assembleDebug`

## Quick Start

- Open with Android Studio Giraffe+ (or newer)
- Sync Gradle
- Run app on emulator/device

## Roadmap

- Widgets and reminders
- Cloud backup & multi-device sync

# Architecture

## Overview

This project uses a clean, practical Android architecture:

- **UI**: Jetpack Compose + Material3
- **Presentation**: MVVM (`ViewModel`, `StateFlow`)
- **Data Access**: Repository pattern
- **Persistence**: Room (SQLite)
- **Async**: Kotlin Coroutines + Flow

## Layers

### 1) UI Layer (`ui/`)

- Composable screens:
  - `HomeScreen`: transaction input, daily grouped list, monthly summary
  - `CategoryScreen`: preset/custom category management
  - `BudgetScreen`: budget setup and warning panel
- Reads immutable state via `collectAsStateWithLifecycle()`
- Sends user intents to `ViewModel`

### 2) Presentation Layer (`viewmodel/`)

- `HomeViewModel`
  - Manages transaction form state
  - Combines transactions + categories + monthly summary + budget with `StateFlow`
  - Computes day grouping and warning text
- `CategoryViewModel`
  - Manages custom category creation
  - Exposes all categories for display
- `BudgetViewModel`
  - Manages monthly budget update
  - Combines budget and expense totals for over-budget signals

### 3) Data Layer (`data/`)

- `LedgerRepository` defines app-facing data contract
- `LedgerRepositoryImpl` orchestrates DAOs and maps models
- One-time seed process inserts:
  - Preset categories
  - Sample transactions
  - Default monthly budget

## Room Design

### Entities

- `TransactionEntity`
  - `amount`, `type`, `categoryId`, `note`, `dateMillis`
- `CategoryEntity`
  - `name`, `type`, `isPreset`
- `BudgetEntity`
  - `monthKey`, `budgetAmount`

### DAOs

- `TransactionDao`
  - Inserts records
  - Emits joined transaction list (`categoryName`)
  - Emits monthly summary aggregates
- `CategoryDao`
  - Emits all/by-type categories
  - Supports duplicate-check lookup
- `BudgetDao`
  - Upserts and observes monthly budget

## State & Data Flow

1. User action in Compose screen
2. `ViewModel` validates and calls repository
3. Repository writes via Room DAO
4. Room Flow emits updated rows
5. `ViewModel` combines and transforms to UI state
6. Compose recomposes with new state

## Why This Fits MVP

- Small code surface, clear boundaries
- Reactive updates with minimal boilerplate
- Easy to extend for edit/delete, charts, exports, and cloud sync

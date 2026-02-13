# Sentinel's Journal 🛡️

## 2024-05-22: Input Validation Vulnerability in GameArgs

**Type:** Validation
**Severity:** MEDIUM
**Component:** `sharedUI` / `RootComponent`

**Finding:**
Input validation was missing for `GameArgs.pairCount`.
Specifically, Deep Links could inject arbitrary integer values (negative, zero, or excessively large) into the game state via `RootComponent.handleDeepLink`.
While `MemoryGameLogic` might crash or handle empty lists for negative values, passing unchecked inputs to domain objects violates the "Secure by Design" principle.

**Remediation:**
1.  Enforce strict invariants in `GameArgs` using `init { require(...) }`.
2.  Sanitize inputs at the boundary (`handleDeepLink`) using `coerceIn`.

## 2024-05-22: Input Validation Fix in PlayerEconomyRepository

**Type:** Validation
**Severity:** MEDIUM
**Component:** `shared/data` / `PlayerEconomyRepositoryImpl`

**Finding:**
`addCurrency` and `deductCurrency` methods did not validate that the `amount` was non-negative.
This could allow potential exploits where negative amounts were used to manipulate the balance unexpectedly (e.g., deducting a negative amount to increase balance).

**Remediation:**
1.  Added `require(amount >= 0)` checks in both methods.
2.  Added unit tests to verify exceptions are thrown for negative inputs.

## 2024-05-22: Input Validation Fix in ScoringConfig

**Type:** Validation
**Severity:** MEDIUM
**Component:** `shared/core` / `ScoringConfig`

**Finding:**
`ScoringConfig` lacked validation for several parameters (e.g., `potMismatchPenalty`, `timePenaltyPerSecond`, `comboBonusPoints`).
Invalid values (negative bonuses, penalties > 100%) could lead to logic corruption or arithmetic overflows in `MemoryGameLogic` and `ScoringCalculator`.

**Remediation:**
1.  Added strict `require` checks in the `init` block for `potMismatchPenalty` (0.0..1.0) and non-negative values for other fields.
2.  Added unit tests in `ScoringConfigTest` to verify the validation logic.

## 2024-05-22: Input Validation Fix in SavedGame and GameStateMachine

**Type:** Validation
**Severity:** MEDIUM
**Component:** `shared/core` / `SavedGame`, `GameStateMachine`

**Finding:**
`SavedGame` allowed negative `elapsedTimeSeconds`, and `GameStateMachine` allowed negative `initialTimeSeconds`.
This could lead to invalid game states (e.g., starting with negative time in Time Attack mode) if a save file was corrupted or tampered with.

**Remediation:**
1.  Added `require(elapsedTimeSeconds >= 0)` in `SavedGame` init block.
2.  Added `require(initialTimeSeconds >= 0)` in `GameStateMachine` init block.
    3.  Added reproduction test `SecurityReproductionTest` to verify the fix (and then removed it).

## 2026-02-08 - [Seed Injection in Daily Challenge]
**Vulnerability:** Seed and Pair Count Injection via Deep Links
**Learning:** `DAILY_CHALLENGE` mode was intended to be deterministic across all players (same board for everyone), but the implementation allowed overriding the `seed` and `pairCount` through `GameArgs`. This meant a user could force a specific board layout (e.g., an extremely easy one or one already solved) using a Deep Link while still being in `DAILY_CHALLENGE` mode, potentially cheating on leaderboards.
**Prevention:** Enforce domain-specific invariants at the game initialization boundary. For special modes like `DAILY_CHALLENGE`, explicitly ignore or validate external parameters that must be derived from the system's source of truth (like the date-based seed).


## 2024-05-22: Parameter Tampering in BuyItemUseCase

**Type:** Business Logic / Parameter Tampering
**Severity:** MEDIUM
**Component:** `shared/core` / `BuyItemUseCase`

**Finding:**
`BuyItemUseCase` accepted `cost` as a parameter from the UI layer (`DefaultShopComponent`).
This allowed a potential attacker (or a bug in the UI) to purchase items for free or reduced cost by manipulating the `ShopItem` object passed to the use case.

**Remediation:**
1.  Refactored `BuyItemUseCase` to accept only `itemId`.
2.  Injected `ShopItemRepository` into `BuyItemUseCase` to fetch the authoritative price.
3.  Updated `DefaultShopComponent` to pass only `itemId`.

## 2026-02-09 - [Leaderboard Validation Vulnerability]
**Vulnerability:** Input Validation
**Severity:** MEDIUM
**Component:** `shared/core` / `LeaderboardEntry`

**Finding:**
`LeaderboardEntry` lacked input validation, allowing the creation of entries with negative scores, times, moves, or invalid pair counts.
This could lead to a corrupted leaderboard and potential logic errors in the UI or stats calculations.

**Remediation:**
1.  Added `init` block to `LeaderboardEntry` with strict `require` checks.
2.  Validated `pairCount > 0`, `score >= 0`, `timeSeconds >= 0`, and `moves >= 0`.
3.  Verified with unit tests in `LeaderboardEntryTest.kt`.

## 2026-02-09 - [Unauthorized Cosmetic Equipment]
**Vulnerability:** IDOR / Logic Bypass
**Severity:** MEDIUM
**Component:** `shared/core` / `SetActiveCosmeticUseCase`

**Finding:**
`SetActiveCosmeticUseCase` allowed equipping any cosmetic item (theme, skin, music, powerup) without checking if the user actually owned/unlocked it.
This would allow an attacker (or a bug) to bypass the economy system.

**Remediation:**
1.  Updated `SetActiveCosmeticUseCase` to enforce ownership check using `playerEconomyRepository.isItemUnlocked(itemId)`.
2.  Explicitly allowed default items (e.g., `CardBackTheme.GEOMETRIC`) to be equipped even if not in the unlocked list.
3.  Added `SetActiveCosmeticUseCaseSecurityTest` to verify the fix and prevent regression.

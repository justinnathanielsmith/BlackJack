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

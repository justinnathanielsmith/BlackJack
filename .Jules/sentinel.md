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

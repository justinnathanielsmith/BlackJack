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

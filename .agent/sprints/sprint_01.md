# Sprint Plan 01 - [STATUS: PLANNED]

## Production Goal
**First-Pass Economy Loop Integration**
Connect the game loop to the economy system by awarding currency on win and displaying the player's bankroll in the game HUD.

## Sprint Backlog

### Feature Requests
- [ ] **Implement Game Over Rewards**
    - **Context**: Players currently earn score but no currency. The `EarnCurrencyUseCase` is ready but unused in the game loop.
    - **Acceptance Criteria**:
        - Update `GameStateMachine` or `DefaultGameComponent` to trigger `EarnCurrencyUseCase` when a game is won.
        - Award currency based on the final score from `ScoreBreakdown`.
- [ ] **Add Bankroll Indicator to Game HUD**
    - **Context**: Players cannot see their balance while playing.
    - **Acceptance Criteria**:
        - Add a currency/balance display to `GameTopBar.kt`.
        - Use the `AppIcons.Medal` or similar to represent currency.
        - Ensure it updates when currency is earned or spent.

### Issues
- [ ] **Verify Currency Persistence**
    - **Context**: We need to ensure that earned currency survives app restarts.
    - **Action**: Add a developer test or manual verification step to confirm Room database storage for `PlayerEconomyRepository`.

### Bug Tickets
- [ ] **Fix Mismatch in Daily Challenge Bonus**
    - **Context**: `ScoringCalculator.kt` has a hardcoded `DAILY_CHALLENGE_CURRENCY_BONUS = 500`. Ensure this is correctly applied and displayed in the results screen.

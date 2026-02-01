# 📝 Economy System & Shop TODOs

## Gameplay Integration
- [ ] **Game Over Rewards**: Implement logic to call `EarnCurrencyUseCase` based on final score/performance in `GameStateMachine`.
- [ ] **Daily Challenge Bonus**: Add specific currency rewards for completing the daily challenge.

## Shop Implementation (Link to Behavior)
- [ ] **Theme Support**: Update card rendering components to use the selected/purchased theme from `PlayerEconomyRepository`.
- [ ] **Power-up Logic**:
    - [ ] **Time Bank**: Implement the ability to use the "Time Bank" item to extend the timer in `GameStateMachine`.
    - [ ] **Pocket Aces**: Implement the "Peek" functionality to briefly reveal cards.
- [ ] **Consumable Deductions**: Ensure consumable items are properly "spent" (decremented) in the database once used in-game.

## Shop Catalog
- [ ] **Dynamic Catalog**: Move the hardcoded list in `GetShopItemsUseCase` to a JSON resource file or database table.
- [ ] **More Items**: Add additional themes and sound packs.

## UI / UX
- [ ] **Purchase Feedback**: Add a "Success" animation or haptic feedback when a purchase completes.
- [ ] **Item Previews**: Show a preview of what a theme looks like before buying.
- [ ] **Bankroll Indicator**: Add a subtle bankroll display to the `GameTopBar` so players can see their balance during gameplay.

## Technical / Testing
- [ ] **Persistence Verification**: Verify Room database migrations and storage work correctly across app restarts on all platforms.

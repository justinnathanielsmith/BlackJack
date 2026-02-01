# Sprint Plan 0x - [STATUS: COMPLETED]

## Production Goal
**Stabilize Core Game Loop & Enforce Code Quality**
Focus on clearing technical debt (Detekt/Spotless), fixing UI inconsistencies in the Start Screen, and verifying the "Daily Challenge" integration.

## Sprint Backlog

### Refactor / Cleanup
- [x] **Remove Daily Challenge from Start UI**
    - **Context**: The `StartTopActions` composable has an unused `onDailyChallengeClick` parameter. The user decided to exclude this from the UI for now.
    - **Acceptance Criteria**:
        - Remove `onDailyChallengeClick` parameter from `StartTopActions`.
        - Remove `onDailyChallengeClick` parameter from `StartContent` (and cascade up if needed).
        - Ensure no "Daily Challenge" button is visible in the Top Bar.

### Issues
- [x] **Enforce Code Style (Spotless)**
    - **Context**: `spotlessCheck` failed due to formatting issues in `StartContent.kt`.
    - **Action**: Run `./gradlew spotlessApply`.
- [x] **Resolve Detekt Violations**
    - **Context**: Unused parameters `state` and `onDailyChallengeClick` in `StartContent.kt`.
    - **Action**: Remove them.

### Bug Tickets
- [x] **Fix Misleading Comment in StartTopActions**
    - **Context**: Line 191 in `StartContent.kt` says `// Top Start Action Row (Daily Challenge)`, but the code below it implements the `ShoppingCart` (Shop) button.
    - **Severity**: Low (Confusion).

# Static Analysis Report

## Findings

### 1. Duplicate Code
- **Issue**: The `checkForMatch` function was duplicated. It existed as a private method inside the `MemoryGameLogic` object and as a top-level private function in the same file.
- **Impact**: This leads to maintenance issues where changes in one version might not be reflected in the other, causing inconsistent behavior.
- **Fix**: Removed the top-level duplicate and consolidated all logic within the `MemoryGameLogic` object.

### 2. Poor Encapsulation
- **Issue**: Functions like `handleMatchFailure`, `resetErrorCards`, `resetUnmatchedCards`, and `handleMirageSwap` were defined as top-level functions (some public, some private) rather than being encapsulated within the `MemoryGameLogic` object.
- **Impact**: This pollutes the package namespace and makes the `MemoryGameLogic` object less cohesive.
- **Fix**: Moved all these functions inside the `MemoryGameLogic` object with appropriate visibility modifiers. `MemoryGameActions` now delegates to these methods.

### 3. Magic Numbers
- **Issue**: The code contained several magic numbers, such as `5` (Mirage move interval), `2` (pair size), and `1` (single card).
- **Impact**: Magic numbers make the code harder to understand and maintain.
- **Fix**: Extracted these values into named constants:
  - `MIRAGE_MOVE_INTERVAL = 5`
  - `PAIR_SIZE = 2`
  - `SINGLE_CARD = 1`

## "Spotless" Fix

The `MemoryGameLogic.kt` file has been refactored to follow Kotlin idioms and formatting standards. Here is a snippet of the improved code structure:

```kotlin
object MemoryGameLogic {
    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3
    private const val MIRAGE_MOVE_INTERVAL = 5
    private const val PAIR_SIZE = 2
    private const val SINGLE_CARD = 1

    // ...

    fun flipCard(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        // ...
        return when {
            // ...
            state.cards.count { it.isFaceUp && !it.isMatched } >= PAIR_SIZE -> state to null
            else -> {
                // ...
                when (activeCards.size) {
                    SINGLE_CARD -> newState to GameDomainEvent.CardFlipped
                    PAIR_SIZE -> checkForMatch(newState, activeCards)
                    else -> newState to null
                }
            }
        }
    }
}
```

## Recommended Detekt Rule

To prevent Magic Numbers in the future, I recommend enforcing the `MagicNumber` rule more strictly in the `detekt-config.yml`, specifically for domain logic files.

Current configuration allows ignoring numbers like `0`, `1`, `2`. However, for game logic, even small numbers can be significant (e.g., `2` for pair size vs `2` for multiplier).

**Proposed Configuration Tweak:**

```yaml
style:
  MagicNumber:
    active: true
    excludes: ['**/test/**', '**/commonTest/**', '**/androidTest/**', '**/ui/**', '**/*Screen.kt', '**/theme/**']
    ignoreEnums: true
    ignoreNumbers: ['-1', '0'] # Removed '1', '2' to force named constants for things like pair sizes
    ignoreAnnotated: ['androidx.compose.runtime.Composable']
```

Alternatively, you can add a custom exclusion for specific property names if needed, but generally, named constants are preferred.

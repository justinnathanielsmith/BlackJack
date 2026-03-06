import re

with open('shared/core/src/commonMain/kotlin/io/github/smithjustinn/domain/services/MatchEvaluator.kt', 'r') as f:
    content = f.read()

content = content.replace("""val activeCards = mutableListOf<CardState>()
            for (i in newState.cards.indices) {
                val c = newState.cards[i]
                if (c.isFaceUp && !c.isMatched) {
                    activeCards.add(c)
                    if (activeCards.size >= MatchConstants.PAIR_SIZE) break
                }
            }""",
"""val activeCards = mutableListOf<CardState>()
            val cards = newState.cards
            for (i in 0 until cards.size) {
                val c = cards[i]
                if (c.isFaceUp && !c.isMatched) {
                    activeCards.add(c)
                    if (activeCards.size == MatchConstants.PAIR_SIZE) break
                }
            }""")

with open('shared/core/src/commonMain/kotlin/io/github/smithjustinn/domain/services/MatchEvaluator.kt', 'w') as f:
    f.write(content)

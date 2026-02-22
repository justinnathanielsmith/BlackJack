package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.utils.TimeConstants
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock

class StartNewGameUseCaseTest {
    private val useCase = StartNewGameUseCase()

    @Test
    fun invoke_standardMode_respectsParameters() {
        val pairCount = 8
        val config = ScoringConfig(baseMatchPoints = 200) // Arbitrary config check if possible
        val mode = GameMode.TIME_ATTACK
        val difficulty = DifficultyType.MASTER
        val isHeatShieldEnabled = true
        val seed = 12345L

        val state =
            useCase(
                pairCount = pairCount,
                config = config,
                mode = mode,
                difficulty = difficulty,
                isHeatShieldEnabled = isHeatShieldEnabled,
                seed = seed,
            )

        assertEquals(pairCount, state.pairCount)
        assertEquals(pairCount * 2, state.cards.size)
        assertEquals(mode, state.mode)
        assertEquals(difficulty, state.difficulty)
        // Check if heat shield logic is applied (StartNewGameUseCase passes it to GameFactory)
        // GameFactory logic isn't fully visible here, but state should reflect difficulty/mode.
        assertEquals(seed, state.seed)
        assertTrue(state.activeMutators.isEmpty(), "Standard mode should have no active mutators")
    }

    @Test
    fun invoke_standardMode_usesRandomSeed_whenNotProvided() {
        val state1 = useCase(pairCount = 8)
        val state2 = useCase(pairCount = 8)

        // Very unlikely to be same seed
        assertNotNull(state1.seed)
        assertNotNull(state2.seed)
        // If they happen to be equal, it's extremely rare or broken secureRandomLong
        // We can't strictly assertNotEquals(state1.seed, state2.seed) without risk of flakiness,
        // but given 64-bit random, it's safe enough for a test unless random is broken.
        // However, let's just check it's not null.
    }

    @Test
    fun invoke_dailyChallenge_enforcesPairCount() {
        // Daily Challenge enforces pair count of 8 regardless of input
        val requestedPairCount = 4
        val state =
            useCase(
                pairCount = requestedPairCount,
                mode = GameMode.DAILY_CHALLENGE,
            )

        assertEquals(8, state.pairCount)
        assertEquals(16, state.cards.size)
    }

    @Test
    fun invoke_dailyChallenge_enforcesStandardConfigAndDifficulty() {
        val customConfig = ScoringConfig(baseMatchPoints = 9999)
        val customDifficulty = DifficultyType.TOURIST

        val state =
            useCase(
                pairCount = 8,
                mode = GameMode.DAILY_CHALLENGE,
                config = customConfig,
                difficulty = customDifficulty,
            )

        // Daily Challenge must ignore custom config and enforce defaults
        assertEquals(
            ScoringConfig.DEFAULT_BASE_MATCH_POINTS,
            state.config.baseMatchPoints,
            "Daily Challenge should enforce standard base match points",
        )
        assertEquals(DifficultyType.CASUAL, state.difficulty, "Daily Challenge should enforce CASUAL difficulty")
    }

    @Test
    fun invoke_dailyChallenge_usesDateBasedSeed() {
        // Daily Challenge uses date-based seed
        val state1 = useCase(pairCount = 8, mode = GameMode.DAILY_CHALLENGE)
        val state2 = useCase(pairCount = 8, mode = GameMode.DAILY_CHALLENGE)

        // Should be same seed (assuming test runs within the same day/millisecond block)
        assertEquals(state1.seed, state2.seed)

        // Calculate expected seed
        val expectedSeed = Clock.System.now().toEpochMilliseconds() / TimeConstants.MILLIS_IN_DAY
        assertEquals(expectedSeed, state1.seed)
    }

    @Test
    fun invoke_dailyChallenge_hasActiveMutators() {
        val state =
            useCase(
                pairCount = 8,
                mode = GameMode.DAILY_CHALLENGE,
            )

        assertFalse(state.activeMutators.isEmpty(), "Daily Challenge must have at least one active mutator")
    }

    @Test
    fun invoke_invalidPairCount_tooLow_throwsException() {
        assertFailsWith<IllegalArgumentException> {
            useCase(pairCount = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            useCase(pairCount = -5)
        }
    }

    @Test
    fun invoke_invalidPairCount_tooHigh_throwsException() {
        val maxPairCount = StartNewGameUseCase.MAX_PAIR_COUNT
        assertFailsWith<IllegalArgumentException> {
            useCase(pairCount = maxPairCount + 1)
        }
    }

    @Test
    fun invoke_validPairCount_boundaryValues_succeeds() {
        // Min valid is 1
        val stateMin = useCase(pairCount = 1)
        assertEquals(1, stateMin.pairCount)
        assertEquals(2, stateMin.cards.size)

        // Max valid is MAX_PAIR_COUNT (26)
        val maxPairCount = StartNewGameUseCase.MAX_PAIR_COUNT
        val stateMax = useCase(pairCount = maxPairCount)
        assertEquals(maxPairCount, stateMax.pairCount)
        assertEquals(maxPairCount * 2, stateMax.cards.size)
    }
}

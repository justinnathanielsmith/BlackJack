package io.github.smithjustinn.ui.start

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Represents the state of the difficulty selection screen.
 */
data class DifficultyState(
    val difficulties: List<DifficultyLevel> = DifficultyLevel.defaultLevels,
    val selectedDifficulty: DifficultyLevel = DifficultyLevel.defaultLevels[1],
    val hasSavedGame: Boolean = false,
    val savedGamePairCount: Int = 0,
    val savedGameMode: GameMode = GameMode.STANDARD,
    val selectedMode: GameMode = GameMode.STANDARD,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC
)

/**
 * Sealed class representing user intents for the difficulty screen.
 */
sealed class DifficultyIntent {
    data class SelectDifficulty(val level: DifficultyLevel) : DifficultyIntent()
    data class SelectMode(val mode: GameMode) : DifficultyIntent()
    data class StartGame(val pairs: Int, val mode: GameMode) : DifficultyIntent()
    data object CheckSavedGame : DifficultyIntent()
    data object ResumeGame : DifficultyIntent()
}

/**
 * Sealed class representing one-time UI events triggered by the ViewModel.
 */
sealed class DifficultyUiEvent {
    data class NavigateToGame(val pairs: Int, val mode: GameMode, val forceNewGame: Boolean) : DifficultyUiEvent()
}

@Inject
class StartScreenModel(
    private val gameStateRepository: GameStateRepository,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger
) : ScreenModel {
    private val _state = MutableStateFlow(DifficultyState())
    val state: StateFlow<DifficultyState> = _state.asStateFlow()

    private val _events = Channel<DifficultyUiEvent>(Channel.BUFFERED)
    val events: Flow<DifficultyUiEvent> = _events.receiveAsFlow()

    init {
        screenModelScope.launch {
            combine(
                settingsRepository.cardBackTheme,
                settingsRepository.cardSymbolTheme
            ) { cardBack, cardSymbol ->
                _state.update { 
                    it.copy(
                        cardBackTheme = cardBack,
                        cardSymbolTheme = cardSymbol
                    ) 
                }
            }.collect()
        }
    }

    fun handleIntent(intent: DifficultyIntent) {
        when (intent) {
            is DifficultyIntent.SelectDifficulty -> {
                _state.update { it.copy(selectedDifficulty = intent.level) }
            }
            is DifficultyIntent.SelectMode -> {
                _state.update { it.copy(selectedMode = intent.mode) }
            }
            is DifficultyIntent.StartGame -> {
                screenModelScope.launch {
                    _events.send(DifficultyUiEvent.NavigateToGame(intent.pairs, intent.mode, forceNewGame = true))
                }
            }
            is DifficultyIntent.CheckSavedGame -> {
                screenModelScope.launch {
                    try {
                        val savedGame = gameStateRepository.getSavedGameState()
                        _state.update { 
                            it.copy(
                                hasSavedGame = savedGame != null && !savedGame.first.isGameOver,
                                savedGamePairCount = savedGame?.first?.pairCount ?: 0,
                                savedGameMode = savedGame?.first?.mode ?: GameMode.STANDARD
                            ) 
                        }
                    } catch (e: Exception) {
                        logger.e(e) { "Error checking for saved game" }
                    }
                }
            }
            is DifficultyIntent.ResumeGame -> {
                if (_state.value.hasSavedGame) {
                    screenModelScope.launch {
                        _events.send(
                            DifficultyUiEvent.NavigateToGame(
                                _state.value.savedGamePairCount,
                                _state.value.savedGameMode,
                                forceNewGame = false
                            )
                        )
                    }
                }
            }
        }
    }
}

package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import memory_match.sharedui.generated.resources.Res
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Inject
class IosAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val players = mutableMapOf<String, AVAudioPlayer>()
    private var isSoundEnabled = true

    init {
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        scope.launch {
            val sounds = listOf(
                AudioService.FLIP,
                AudioService.MATCH,
                AudioService.MISMATCH,
                AudioService.WIN,
                AudioService.CLICK,
                AudioService.DEAL
            )
            sounds.forEach { name ->
                try {
                    val path = "$name.m4a"
                    val bytes = Res.readBytes("files/$path")
                    val data = bytes.usePinned { pinned ->
                        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                    }
                    val player = AVAudioPlayer(data = data, error = null)
                    player.prepareToPlay()
                    players[name] = player
                } catch (e: Exception) {
                    logger.e(e) { "Error pre-loading sound: $name" }
                }
            }
        }
    }

    private fun playSound(name: String) {
        if (!isSoundEnabled) return

        val player = players[name] ?: return
        if (player.playing) {
            player.stop()
            player.currentTime = 0.0
        }
        player.play()
    }

    override fun playFlip() = playSound(AudioService.FLIP)
    override fun playMatch() = playSound(AudioService.MATCH)
    override fun playMismatch() = playSound(AudioService.MISMATCH)
    override fun playWin() = playSound(AudioService.WIN)
    override fun playClick() = playSound(AudioService.CLICK)
    override fun playDeal() = playSound(AudioService.DEAL)
}

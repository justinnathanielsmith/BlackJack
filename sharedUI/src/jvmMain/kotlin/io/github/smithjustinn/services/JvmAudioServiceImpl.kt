package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import memory_match.sharedui.generated.resources.Res
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

@Inject
class JvmAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val clips = ConcurrentHashMap<String, Clip>()
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
                AudioService.LOSE,
                AudioService.HIGH_SCORE,
                AudioService.CLICK,
                AudioService.DEAL
            )
            sounds.forEach { name ->
                try {
                    val path = "$name.wav"
                    val bytes = Res.readBytes("files/$path")
                    val inputStream = ByteArrayInputStream(bytes)
                    val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(inputStream))
                    val clip = AudioSystem.getClip()
                    clip.open(audioStream)
                    clips[name] = clip
                } catch (e: Exception) {
                    logger.e(e) { "Error pre-loading sound: $name" }
                }
            }
        }
    }

    private fun playSound(name: String) {
        if (!isSoundEnabled) return

        val clip = clips[name] ?: return
        if (clip.isRunning) {
            clip.stop()
        }
        clip.framePosition = 0
        clip.start()
    }

    override fun playFlip() = playSound(AudioService.FLIP)
    override fun playMatch() = playSound(AudioService.MATCH)
    override fun playMismatch() = playSound(AudioService.MISMATCH)
    override fun playWin() = playSound(AudioService.WIN)
    override fun playLose() = playSound(AudioService.LOSE)
    override fun playHighScore() = playSound(AudioService.HIGH_SCORE)
    override fun playClick() = playSound(AudioService.CLICK)
    override fun playDeal() = playSound(AudioService.DEAL)
}

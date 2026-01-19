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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import kotlin.math.log10

@Inject
class JvmAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val clips = ConcurrentHashMap<StringResource, Clip>()
    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private var soundVolume = 1.0f
    private var musicVolume = 1.0f
    private var musicClip: Clip? = null

    init {
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        settingsRepository.soundVolume
            .onEach { volume ->
                soundVolume = volume
                clips.values.forEach { it.setVolume(volume) }
            }
            .launchIn(scope)

        settingsRepository.isMusicEnabled
            .onEach { enabled ->
                isMusicEnabled = enabled
                updateMusicPlayback()
            }
            .launchIn(scope)

        settingsRepository.musicVolume
            .onEach { volume ->
                musicVolume = volume
                musicClip?.setVolume(volume)
            }
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
            sounds.forEach { resource ->
                try {
                    val name = getString(resource)
                    val path = "$name.wav"
                    val bytes = Res.readBytes("files/$path")
                    val inputStream = ByteArrayInputStream(bytes)
                    val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(inputStream))
                    val clip = AudioSystem.getClip()
                    clip.open(audioStream)
                    clip.setVolume(soundVolume)
                    clips[resource] = clip
                } catch (e: Exception) {
                    logger.e(e) { "Error pre-loading sound resource: $resource" }
                }
            }
        }
    }

    private fun Clip.setVolume(volume: Float) {
        try {
            if (isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                val gainControl = getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
                val dB = (log10(volume.coerceAtLeast(0.0001f).toDouble()) * 20.0).toFloat()
                gainControl.value = dB.coerceIn(gainControl.minimum, gainControl.maximum)
            }
        } catch (e: Exception) {
            logger.e(e) { "Error setting volume for clip" }
        }
    }

    private fun playSound(resource: StringResource) {
        if (!isSoundEnabled) return

        val clip = clips[resource] ?: return
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

    override fun startMusic() {
        isMusicRequested = true
        updateMusicPlayback()
    }

    override fun stopMusic() {
        isMusicRequested = false
        updateMusicPlayback()
    }

    private fun updateMusicPlayback() {
        if (isMusicRequested && isMusicEnabled) {
            if (musicClip?.isRunning != true) {
                actuallyStartMusic()
            }
        } else {
            actuallyStopMusic()
        }
    }

    private fun actuallyStartMusic() {
        scope.launch {
            try {
                if (musicClip == null) {
                    val name = getString(AudioService.MUSIC)
                    val path = "$name.wav"
                    val bytes = Res.readBytes("files/$path")
                    val inputStream = ByteArrayInputStream(bytes)
                    val audioStream = AudioSystem.getAudioInputStream(BufferedInputStream(inputStream))
                    musicClip = AudioSystem.getClip().apply {
                        open(audioStream)
                    }
                }
                
                musicClip?.apply {
                    setVolume(musicVolume)
                    if (!isRunning && isMusicRequested && isMusicEnabled) {
                        framePosition = 0
                        loop(Clip.LOOP_CONTINUOUSLY)
                        start()
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Error starting music" }
            }
        }
    }

    private fun actuallyStopMusic() {
        musicClip?.stop()
        musicClip?.close()
        musicClip = null
    }
}

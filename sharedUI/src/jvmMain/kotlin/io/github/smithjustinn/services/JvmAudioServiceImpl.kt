package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.services.AudioService.Companion.toResource
import javafx.application.Platform
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import javafx.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

class JvmAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository,
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val players = ConcurrentHashMap<StringResource, MediaPlayer>()
    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private var soundVolume = 1.0f
    private var musicVolume = 1.0f
    private var musicPlayer: MediaPlayer? = null

    private val tempCacheDir =
        File(System.getProperty("java.io.tmpdir"), "memory_match_audio").apply {
            if (!exists()) mkdirs()
        }

    init {
        // Ensure JavaFX platform is initialized
        // Using Platform.startup now that we have all usage dependencies (base, graphics, controls)
        try {
            Platform.startup {}
        } catch (e: IllegalStateException) {
            // Already initialized
        } catch (e: Exception) {
            logger.e(e) { "Error initializing JavaFX toolkit" }
        }

        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        settingsRepository.soundVolume
            .onEach { volume ->
                soundVolume = volume
                players.values.forEach { it.volume = volume.toDouble() }
            }.launchIn(scope)

        settingsRepository.isMusicEnabled
            .onEach { enabled ->
                isMusicEnabled = enabled
                updateMusicPlayback()
            }.launchIn(scope)

        settingsRepository.musicVolume
            .onEach { volume ->
                musicVolume = volume
                musicPlayer?.volume = volume.toDouble()
            }.launchIn(scope)

        scope.launch {
            AudioService.SoundEffect.entries.forEach { effect ->
                loadSound(effect.toResource())
            }
        }
    }

    private suspend fun loadSound(resource: StringResource): MediaPlayer? =
        withContext(Dispatchers.IO) {
            try {
                val name = getString(resource)
                val fileName = "$name.m4a"
                val tempFile = File(tempCacheDir, fileName)

                if (!tempFile.exists()) {
                    val bytes = Res.readBytes("files/$fileName")
                    FileOutputStream(tempFile).use { it.write(bytes) }
                }

                val media = Media(tempFile.toURI().toString())
                val player = MediaPlayer(media)
                player.volume = soundVolume.toDouble()
                players[resource] = player
                player
            } catch (e: Exception) {
                logger.e(e) { "Error pre-loading sound resource: $resource" }
                null
            }
        }

    private fun playSound(resource: StringResource) {
        if (!isSoundEnabled) return

        val player = players[resource] ?: return
        player.stop()
        player.seek(Duration.ZERO)
        player.play()
    }

    override fun playEffect(effect: AudioService.SoundEffect) {
        playSound(effect.toResource())
    }

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
            if (musicPlayer?.status != MediaPlayer.Status.PLAYING) {
                actuallyStartMusic()
            }
        } else {
            actuallyStopMusic()
        }
    }

    private fun actuallyStartMusic() {
        scope.launch {
            try {
                if (musicPlayer == null) {
                    val name = getString(AudioService.MUSIC)
                    val fileName = "$name.m4a"
                    val tempFile = File(tempCacheDir, fileName)

                    if (!tempFile.exists()) {
                        val bytes = Res.readBytes("files/$fileName")
                        FileOutputStream(tempFile).use { it.write(bytes) }
                    }

                    val media = Media(tempFile.toURI().toString())
                    musicPlayer =
                        MediaPlayer(media).apply {
                            cycleCount = MediaPlayer.INDEFINITE
                            volume = musicVolume.toDouble()
                        }
                }

                if (isMusicRequested && isMusicEnabled) {
                    musicPlayer?.play()
                }
            } catch (e: Exception) {
                logger.e(e) { "Error starting music" }
            }
        }
    }

    private fun actuallyStopMusic() {
        musicPlayer?.stop()
        musicPlayer?.dispose()
        musicPlayer = null
    }
}

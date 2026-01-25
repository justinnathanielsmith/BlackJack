package io.github.smithjustinn.services

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import memory_match.sharedui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

@Inject
class AndroidAudioServiceImpl(
    private val context: Context,
    private val logger: Logger,
    settingsRepository: SettingsRepository,
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_STREAMS)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val soundMap = ConcurrentHashMap<StringResource, Int>()
    private val resourceToName = ConcurrentHashMap<StringResource, String>()
    private val loadedSounds = ConcurrentHashMap.newKeySet<Int>()
    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private var soundVolume = 1.0f
    private var musicVolume = 1.0f
    private var musicPlayer: MediaPlayer? = null

    init {
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        settingsRepository.soundVolume
            .onEach { soundVolume = it }
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
                musicPlayer?.setVolume(volume, volume)
            }
            .launchIn(scope)

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }

        scope.launch {
            listOf(
                AudioService.FLIP,
                AudioService.MATCH,
                AudioService.MISMATCH,
                AudioService.WIN,
                AudioService.LOSE,
                AudioService.HIGH_SCORE,
                AudioService.CLICK,
                AudioService.DEAL,
            ).forEach { resource ->
                loadSound(resource)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadSound(resource: StringResource): Int? = try {
        val name = getString(resource)
        resourceToName[resource] = name
        val fileName = "$name.m4a"
        val bytes = Res.readBytes("files/$fileName")
        val tempFile = File(context.cacheDir, fileName)
        withContext(Dispatchers.IO) {
            FileOutputStream(tempFile).use { it.write(bytes) }
        }
        val id = soundPool.load(tempFile.absolutePath, 1)
        soundMap[resource] = id
        id
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        logger.e(e) { "Error loading sound resource: $resource" }
        null
    }

    private fun playSound(resource: StringResource) {
        if (!isSoundEnabled) return

        val soundId = soundMap[resource]
        if (soundId != null && loadedSounds.contains(soundId)) {
            val streamId = soundPool.play(soundId, soundVolume, soundVolume, 1, 0, 1f)
            if (streamId == 0) {
                playFallback(resource)
            }
        } else {
            playFallback(resource)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun playFallback(resource: StringResource) {
        val name = resourceToName[resource] ?: return
        val fileName = "$name.m4a"
        scope.launch {
            try {
                val tempFile = File(context.cacheDir, fileName)
                if (tempFile.exists()) {
                    MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        setVolume(soundVolume, soundVolume)
                        prepare()
                        setOnCompletionListener { release() }
                        start()
                    }
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.e(e) { "Error playing fallback sound: $name" }
            }
        }
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
            if (musicPlayer?.isPlaying != true) {
                actuallyStartMusic()
            }
        } else {
            actuallyStopMusic()
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun actuallyStartMusic() {
        scope.launch {
            try {
                val name = getString(AudioService.MUSIC)
                val fileName = "$name.m4a"
                val tempFile = File(context.cacheDir, fileName)

                if (!tempFile.exists()) {
                    val bytes = Res.readBytes("files/$fileName")
                    withContext(Dispatchers.IO) {
                        FileOutputStream(tempFile).use { it.write(bytes) }
                    }
                }

                withContext(Dispatchers.Main) {
                    if (!isMusicRequested || !isMusicEnabled || musicPlayer?.isPlaying == true) return@withContext

                    musicPlayer?.release()
                    musicPlayer = MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        isLooping = true
                        setVolume(musicVolume, musicVolume)
                        prepare()
                        start()
                    }
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.e(e) { "Error starting music" }
            }
        }
    }

    private fun actuallyStopMusic() {
        musicPlayer?.stop()
        musicPlayer?.release()
        musicPlayer = null
    }

    companion object {
        private const val MAX_STREAMS = 10
    }
}

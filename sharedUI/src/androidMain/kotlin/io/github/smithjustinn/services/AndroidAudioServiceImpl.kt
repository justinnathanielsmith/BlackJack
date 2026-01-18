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
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap

@Inject
class AndroidAudioServiceImpl(
    private val context: Context,
    private val logger: Logger,
    settingsRepository: SettingsRepository
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundMap = ConcurrentHashMap<String, Int>()
    private val loadedSounds = ConcurrentHashMap.newKeySet<Int>()
    private var isSoundEnabled = true

    init {
        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
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
                AudioService.CLICK,
                AudioService.DEAL
            ).forEach { name ->
                loadSound(name)
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun loadSound(name: String): Int? {
        val fileName = "$name.m4a"
        return try {
            val bytes = Res.readBytes("files/$fileName")
            val tempFile = File(context.cacheDir, fileName)
            withContext(Dispatchers.IO) {
                FileOutputStream(tempFile).use { it.write(bytes) }
            }
            val id = soundPool.load(tempFile.absolutePath, 1)
            soundMap[name] = id
            id
        } catch (e: Exception) {
            logger.e(e) { "Error loading sound: $name" }
            null
        }
    }

    private fun playSound(name: String) {
        if (!isSoundEnabled) return

        val soundId = soundMap[name]
        if (soundId != null && loadedSounds.contains(soundId)) {
            val streamId = soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            if (streamId == 0) {
                playFallback(name)
            }
        } else {
            playFallback(name)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    private fun playFallback(name: String) {
        val fileName = "$name.m4a"
        scope.launch {
            try {
                val tempFile = File(context.cacheDir, fileName)
                if (tempFile.exists()) {
                    MediaPlayer().apply {
                        setDataSource(tempFile.absolutePath)
                        prepare()
                        setOnCompletionListener { release() }
                        start()
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Error playing fallback sound: $name" }
            }
        }
    }

    override fun playFlip() = playSound(AudioService.FLIP)
    override fun playMatch() = playSound(AudioService.MATCH)
    override fun playMismatch() = playSound(AudioService.MISMATCH)
    override fun playWin() = playSound(AudioService.WIN)
    override fun playLose() = playSound(AudioService.LOSE)
    override fun playClick() = playSound(AudioService.CLICK)
    override fun playDeal() = playSound(AudioService.DEAL)
}

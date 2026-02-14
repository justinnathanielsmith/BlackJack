package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.services.AudioService.Companion.toResource
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.Foundation.NSData
import platform.Foundation.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosAudioServiceImpl(
    private val logger: Logger,
    settingsRepository: SettingsRepository,
) : AudioService {
    private val scope = CoroutineScope(Dispatchers.Main)

    // Pool of players for each sound effect to support polyphony
    private val playerPools = mutableMapOf<StringResource, MutableList<AVAudioPlayer>>()
    private val poolIndices = mutableMapOf<StringResource, Int>()

    private var isSoundEnabled = true
    private var isMusicEnabled = true
    private var isMusicRequested = false
    private var soundVolume = 1.0f
    private var musicVolume = 1.0f
    private var musicPlayer: AVAudioPlayer? = null
    private var musicLoadingJob: Job? = null

    init {
        setupAudioSession()

        settingsRepository.isSoundEnabled
            .onEach { isSoundEnabled = it }
            .launchIn(scope)

        settingsRepository.soundVolume
            .onEach { volume ->
                soundVolume = volume
                playerPools.values.flatten().forEach { it.volume = volume }
            }.launchIn(scope)

        settingsRepository.isMusicEnabled
            .onEach { enabled ->
                isMusicEnabled = enabled
                updateMusicPlayback()
            }.launchIn(scope)

        settingsRepository.musicVolume
            .onEach { volume ->
                musicVolume = volume
                musicPlayer?.volume = volume
            }.launchIn(scope)

        preloadSounds()
    }

    private fun setupAudioSession() {
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
            session.setActive(true, error = null)
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            logger.e(e) { "Error setting up AVAudioSession" }
        }
    }

    private fun preloadSounds() {
        scope.launch {
            AudioService.SoundEffect.entries.forEach { effect ->
                try {
                    val resource = effect.toResource()
                    val name = getString(resource)
                    val path = "$name.m4a"
                    val bytes = Res.readBytes("files/$path")
                    if (bytes.isEmpty()) {
                        logger.w { "Sound file is empty: $name" }
                        return@forEach
                    }
                    val data =
                        bytes.usePinned { pinned ->
                            NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                        }

                    // Create a pool of players for polyphony support
                    val pool = mutableListOf<AVAudioPlayer>()
                    repeat(PLAYER_POOL_SIZE) {
                        val player = AVAudioPlayer(data = data, error = null)
                        player.volume = soundVolume
                        player.prepareToPlay()
                        pool.add(player)
                    }

                    playerPools[resource] = pool
                    poolIndices[resource] = 0
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    logger.e(e) { "Error pre-loading sound effect: $effect" }
                }
            }
        }
    }

    private fun playSound(resource: StringResource) {
        if (!isSoundEnabled) return

        val pool = playerPools[resource]
        if (pool == null || pool.isEmpty()) {
            logger.w { "Sound not loaded yet: $resource" }
            return
        }

        // Round-robin player selection for polyphony
        val currentIndex = poolIndices[resource] ?: 0
        val player = pool[currentIndex]

        // Stop and rewind if already playing
        if (player.playing) {
            player.stop()
            player.currentTime = 0.0
        }

        player.play()

        // Update index for next call
        poolIndices[resource] = (currentIndex + 1) % pool.size
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
            actuallyStartMusic()
        } else {
            actuallyStopMusic()
        }
    }

    private fun actuallyStartMusic() {
        if (musicPlayer?.playing == true) return
        if (musicLoadingJob?.isActive == true) return

        musicLoadingJob =
            scope.launch {
                try {
                    if (musicPlayer == null) {
                        musicPlayer = createMusicPlayer()
                    }

                    if (isMusicRequested && isMusicEnabled) {
                        musicPlayer?.play()
                    }
                } catch (
                    @Suppress("TooGenericExceptionCaught") e: Exception,
                ) {
                    logger.e(e) { "Error starting music" }
                } finally {
                    musicLoadingJob = null
                }
            }
    }

    private suspend fun createMusicPlayer(): AVAudioPlayer? =
        withContext(Dispatchers.Default) {
            val name = getString(AudioService.MUSIC)
            val path = "$name.m4a"
            val bytes = Res.readBytes("files/$path")
            if (bytes.isNotEmpty()) {
                val data =
                    bytes.usePinned { pinned ->
                        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
                    }
                AVAudioPlayer(data = data, error = null).apply {
                    numberOfLoops = -1
                    volume = musicVolume
                    prepareToPlay()
                }
            } else {
                null
            }
        }

    private fun actuallyStopMusic() {
        musicLoadingJob?.cancel()
        musicLoadingJob = null
        musicPlayer?.stop()
    }

    companion object {
        private const val PLAYER_POOL_SIZE = 3
    }
}

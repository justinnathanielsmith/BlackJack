package io.github.smithjustinn.services

import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.audio_click
import memory_match.sharedui.generated.resources.audio_deal
import memory_match.sharedui.generated.resources.audio_flip
import memory_match.sharedui.generated.resources.audio_highscore
import memory_match.sharedui.generated.resources.audio_lose
import memory_match.sharedui.generated.resources.audio_match
import memory_match.sharedui.generated.resources.audio_mismatch
import memory_match.sharedui.generated.resources.audio_music
import memory_match.sharedui.generated.resources.audio_win
import org.jetbrains.compose.resources.StringResource

interface AudioService {
    fun playEffect(effect: SoundEffect)
    fun startMusic()
    fun stopMusic()

    enum class SoundEffect {
        FLIP,
        MATCH,
        MISMATCH,
        WIN,
        LOSE,
        HIGH_SCORE,
        CLICK,
        DEAL,
    }

    companion object {
        fun SoundEffect.toResource(): StringResource =
            when (this) {
                SoundEffect.FLIP -> Res.string.audio_flip
                SoundEffect.MATCH -> Res.string.audio_match
                SoundEffect.MISMATCH -> Res.string.audio_mismatch
                SoundEffect.WIN -> Res.string.audio_win
                SoundEffect.LOSE -> Res.string.audio_lose
                SoundEffect.HIGH_SCORE -> Res.string.audio_highscore
                SoundEffect.CLICK -> Res.string.audio_click
                SoundEffect.DEAL -> Res.string.audio_deal
            }

        val MUSIC: StringResource = Res.string.audio_music
    }
}

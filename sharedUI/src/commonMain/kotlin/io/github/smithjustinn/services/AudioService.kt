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
    fun playFlip()
    fun playMatch()
    fun playMismatch()
    fun playWin()
    fun playLose()
    fun playHighScore()
    fun playClick()
    fun playDeal()
    
    fun startMusic()
    fun stopMusic()

    companion object {
        val FLIP: StringResource = Res.string.audio_flip
        val MATCH: StringResource = Res.string.audio_match
        val MISMATCH: StringResource = Res.string.audio_mismatch
        val WIN: StringResource = Res.string.audio_win
        val LOSE: StringResource = Res.string.audio_lose
        val HIGH_SCORE: StringResource = Res.string.audio_highscore
        val CLICK: StringResource = Res.string.audio_click
        val DEAL: StringResource = Res.string.audio_deal
        val MUSIC: StringResource = Res.string.audio_music
    }
}

package io.github.smithjustinn.services

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
        const val FLIP = "flip"
        const val MATCH = "match"
        const val MISMATCH = "mismatch"
        const val WIN = "win"
        const val LOSE = "lose"
        const val HIGH_SCORE = "highscore"
        const val CLICK = "click"
        const val DEAL = "deal"
        const val MUSIC = "music"
    }
}

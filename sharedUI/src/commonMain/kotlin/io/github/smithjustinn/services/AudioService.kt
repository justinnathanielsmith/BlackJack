package io.github.smithjustinn.services

interface AudioService {
    fun playFlip()
    fun playMatch()
    fun playMismatch()
    fun playWin()
    fun playClick()
    fun playDeal()

    companion object {
        const val FLIP = "flip"
        const val MATCH = "match"
        const val MISMATCH = "mismatch"
        const val WIN = "win"
        const val CLICK = "click"
        const val DEAL = "deal"
    }
}

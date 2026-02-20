package io.github.smithjustinn.ui.root

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.game.GameArgs
import io.github.smithjustinn.utils.Constants
import io.ktor.http.URLBuilder
import io.ktor.http.Url

data class DeepLinkParams(
    val mode: GameMode,
    val pairs: Int,
    val seed: Long?,
)

object DeepLinkUtils {
    private const val DEFAULT_PAIR_COUNT = 8

    fun parseDeepLink(url: String): DeepLinkParams? {
        if (!url.startsWith(Constants.DEEP_LINK_PREFIX)) return null

        return try {
            val urlParams = Url(url).parameters
            val modeStr = urlParams[Constants.QUERY_PARAM_MODE]
            val pairsStr = urlParams[Constants.QUERY_PARAM_PAIRS]
            val seedStr = urlParams[Constants.QUERY_PARAM_SEED]

            val mode =
                modeStr?.let {
                    try {
                        GameMode.valueOf(it)
                    } catch (e: IllegalArgumentException) {
                        GameMode.TIME_ATTACK
                    }
                } ?: GameMode.TIME_ATTACK

            val pairs =
                (pairsStr?.toIntOrNull() ?: DEFAULT_PAIR_COUNT)
                    .coerceIn(GameArgs.MIN_PAIRS, GameArgs.MAX_PAIRS)

            val seed = seedStr?.toLongOrNull()

            DeepLinkParams(mode, pairs, seed)
        } catch (e: Exception) {
            // Log error if needed, but here we just return null to ignore invalid deep links
            null
        }
    }

    fun sanitizeForLogging(url: String): String =
        try {
            val parsedUrl = Url(url)
            val builder = URLBuilder(parsedUrl)
            val keys = builder.parameters.names().toSet()
            builder.parameters.clear()
            keys.forEach { key ->
                builder.parameters.append(key, "REDACTED")
            }
            builder.buildString()
        } catch (e: Exception) {
            "Malformatted Deep Link"
        }
}

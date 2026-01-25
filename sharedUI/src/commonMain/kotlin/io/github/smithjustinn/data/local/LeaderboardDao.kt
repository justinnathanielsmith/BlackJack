package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.github.smithjustinn.domain.models.GameMode
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {
    @Query(
        "SELECT * FROM leaderboard " +
            "WHERE pairCount = :pairCount AND gameMode = :gameMode " +
            "ORDER BY score DESC, timeSeconds ASC LIMIT 10",
    )
    fun getTopEntries(
        pairCount: Int,
        gameMode: GameMode,
    ): Flow<List<LeaderboardEntity>>

    @Insert
    suspend fun insertEntry(entry: LeaderboardEntity)
}

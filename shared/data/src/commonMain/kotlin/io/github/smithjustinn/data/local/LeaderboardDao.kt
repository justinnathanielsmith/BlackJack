package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
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

    @RewriteQueriesToDropUnusedColumns
    @Query(
        "SELECT * FROM (" +
            "SELECT *, ROW_NUMBER() OVER(PARTITION BY pairCount ORDER BY score DESC, timeSeconds ASC) as rn " +
            "FROM leaderboard " +
            "WHERE gameMode = :gameMode" +
            ") WHERE rn <= 10",
    )
    fun getAllTopEntries(gameMode: GameMode): Flow<List<LeaderboardEntity>>

    @Insert
    suspend fun insertEntry(entry: LeaderboardEntity)
}

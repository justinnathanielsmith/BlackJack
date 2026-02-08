package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {
    @Query("SELECT * FROM game_stats WHERE pairCount = :pairCount")
    fun getStatsForDifficulty(pairCount: Int): Flow<GameStatsEntity?>

    @Query("SELECT * FROM game_stats")
    fun getAllStats(): Flow<List<GameStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: GameStatsEntity)
}

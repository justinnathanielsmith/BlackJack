package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyChallengeDao {
    @Query("SELECT * FROM daily_challenges WHERE date = :date")
    fun getDailyChallenge(date: Long): Flow<DailyChallengeEntity?>

    @Query("SELECT * FROM daily_challenges WHERE date = :date")
    suspend fun getDailyChallengeSync(date: Long): DailyChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(dailyChallenge: DailyChallengeEntity)
}

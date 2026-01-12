package io.github.smithjustinn.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GameStateDao {
    @Query("SELECT * FROM saved_game_state WHERE id = 0")
    suspend fun getSavedGameState(): GameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(entity: GameStateEntity)

    @Query("DELETE FROM saved_game_state WHERE id = 0")
    suspend fun clearSavedGameState()
}

package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_challenges")
data class DailyChallengeEntity(
    @PrimaryKey
    val date: Long, // Stored as epoch days or a simplified date representation
    val isCompleted: Boolean,
    val score: Int,
    val timeSeconds: Long,
    val moves: Int,
)

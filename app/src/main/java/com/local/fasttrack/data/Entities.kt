package com.local.fasttrack.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One fasting session. If endTime is null, the fast is currently in progress.
 * goalHours is the target length the user picked (16, 18, or 20).
 */
@Entity(tableName = "fast_sessions")
data class FastSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,          // epoch millis
    val endTime: Long? = null,    // epoch millis, null while fasting is active
    val goalHours: Int
)

@Entity(tableName = "weight_entries")
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,   // epoch millis
    val weightKg: Double
)

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,   // epoch millis
    val amountMl: Int
)

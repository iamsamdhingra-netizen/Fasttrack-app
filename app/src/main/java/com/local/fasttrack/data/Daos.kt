package com.local.fasttrack.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FastSessionDao {
    @Insert
    suspend fun insert(session: FastSession): Long

    @Update
    suspend fun update(session: FastSession)

    @Query("SELECT * FROM fast_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getActiveSession(): FastSession?

    @Query("SELECT * FROM fast_sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun observeActiveSession(): Flow<FastSession?>

    @Query("SELECT * FROM fast_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    fun observeHistory(): Flow<List<FastSession>>

    @Query("SELECT * FROM fast_sessions WHERE endTime IS NOT NULL ORDER BY startTime DESC")
    suspend fun getHistory(): List<FastSession>

    @Query("DELETE FROM fast_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface WeightDao {
    @Insert
    suspend fun insert(entry: WeightEntry): Long

    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weight_entries ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<WeightEntry?>

    @Query("DELETE FROM weight_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface WaterDao {
    @Insert
    suspend fun insert(entry: WaterEntry): Long

    @Query("SELECT * FROM water_entries WHERE timestamp >= :dayStart AND timestamp < :dayEnd ORDER BY timestamp DESC")
    fun observeForDay(dayStart: Long, dayEnd: Long): Flow<List<WaterEntry>>

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}

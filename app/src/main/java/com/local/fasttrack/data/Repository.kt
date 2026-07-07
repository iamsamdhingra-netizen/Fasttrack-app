package com.local.fasttrack.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class FastingRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val fastDao = db.fastSessionDao()
    private val weightDao = db.weightDao()
    private val waterDao = db.waterDao()

    // --- Fasting ---
    suspend fun getActiveSession(): FastSession? = fastDao.getActiveSession()
    fun observeActiveSession(): Flow<FastSession?> = fastDao.observeActiveSession()
    fun observeHistory(): Flow<List<FastSession>> = fastDao.observeHistory()
    suspend fun getHistory(): List<FastSession> = fastDao.getHistory()

    suspend fun startFast(goalHours: Int): Long =
        fastDao.insert(FastSession(startTime = System.currentTimeMillis(), goalHours = goalHours))

    suspend fun endFast(session: FastSession) =
        fastDao.update(session.copy(endTime = System.currentTimeMillis()))

    suspend fun deleteFast(id: Long) = fastDao.deleteById(id)

    // --- Weight ---
    fun observeWeights(): Flow<List<WeightEntry>> = weightDao.observeAll()
    fun observeLatestWeight(): Flow<WeightEntry?> = weightDao.observeLatest()
    suspend fun logWeight(kg: Double) =
        weightDao.insert(WeightEntry(timestamp = System.currentTimeMillis(), weightKg = kg))
    suspend fun deleteWeight(id: Long) = weightDao.deleteById(id)

    // --- Water ---
    fun observeWaterForDay(dayStartMillis: Long, dayEndMillis: Long): Flow<List<WaterEntry>> =
        waterDao.observeForDay(dayStartMillis, dayEndMillis)
    suspend fun logWater(ml: Int) =
        waterDao.insert(WaterEntry(timestamp = System.currentTimeMillis(), amountMl = ml))
    suspend fun deleteWater(id: Long) = waterDao.deleteById(id)

    // --- Stats helpers ---
    suspend fun computeStats(): FastingStats {
        val history = getHistory()
        if (history.isEmpty()) return FastingStats(0, 0.0, 0.0, 0)
        val durationsHrs = history.map { (it.endTime!! - it.startTime) / 3_600_000.0 }
        val avg = durationsHrs.average()
        val longest = durationsHrs.max()

        // current streak: consecutive days (going backwards from most recent) with a completed fast
        var streak = 0
        val cal = java.util.Calendar.getInstance()
        val daysWithFast = history.map { dayKeyFor(it.endTime!!) }.toSet()
        var cursor = dayKeyFor(System.currentTimeMillis())
        while (daysWithFast.contains(cursor)) {
            streak++
            cal.timeInMillis = cursor
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            cursor = dayKeyFor(cal.timeInMillis)
        }
        return FastingStats(history.size, avg, longest, streak)
    }

    private fun dayKeyFor(millis: Long): Long {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

data class FastingStats(
    val totalFasts: Int,
    val averageHours: Double,
    val longestHours: Double,
    val currentStreakDays: Int
)

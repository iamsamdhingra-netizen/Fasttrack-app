package com.local.fasttrack.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.local.fasttrack.data.FastSession
import com.local.fasttrack.data.FastingRepository
import com.local.fasttrack.data.FastingStage
import com.local.fasttrack.data.FastingStats
import com.local.fasttrack.data.WaterEntry
import com.local.fasttrack.data.WeightEntry
import com.local.fasttrack.widget.updateFastingWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FastingViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FastingRepository(app)

    val activeSession: StateFlow<FastSession?> =
        repo.observeActiveSession().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val history: StateFlow<List<FastSession>> =
        repo.observeHistory().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weights: StateFlow<List<WeightEntry>> =
        repo.observeWeights().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val dayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    private val dayEnd = dayStart + 24 * 3_600_000L

    val todaysWater: StateFlow<List<WaterEntry>> =
        repo.observeWaterForDay(dayStart, dayEnd).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _stats = MutableStateFlow(FastingStats(0, 0.0, 0.0, 0))
    val stats: StateFlow<FastingStats> = _stats

    // Ticks once a second so the on-screen timer stays live.
    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    val nowMillis: StateFlow<Long> = _nowMillis

    init {
        viewModelScope.launch {
            while (true) {
                _nowMillis.value = System.currentTimeMillis()
                delay(1000)
            }
        }
        viewModelScope.launch {
            history.collect { refreshStats() }
        }
    }

    private suspend fun refreshStats() {
        _stats.value = repo.computeStats()
    }

    fun startFast(goalHours: Int) = viewModelScope.launch {
        repo.startFast(goalHours)
        updateFastingWidget(getApplication())
    }

    fun endFast() = viewModelScope.launch {
        activeSession.value?.let {
            repo.endFast(it)
            updateFastingWidget(getApplication())
        }
    }

    fun deleteFast(id: Long) = viewModelScope.launch { repo.deleteFast(id) }

    fun logWeight(kg: Double) = viewModelScope.launch { repo.logWeight(kg) }
    fun deleteWeight(id: Long) = viewModelScope.launch { repo.deleteWeight(id) }

    fun logWater(ml: Int) = viewModelScope.launch {
        repo.logWater(ml)
        updateFastingWidget(getApplication())
    }

    fun elapsedHours(session: FastSession, now: Long): Double =
        (now - session.startTime) / 3_600_000.0

    fun stageFor(session: FastSession, now: Long): FastingStage =
        FastingStage.forElapsedHours(elapsedHours(session, now))
}

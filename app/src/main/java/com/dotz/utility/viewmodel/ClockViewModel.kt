package com.dotz.utility.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Stopwatch ───────────────────────────────────────────────────────────────

data class StopwatchState(
    val elapsedMs: Long = 0,
    val isRunning: Boolean = false,
    val laps: List<Long> = emptyList(),
)

// ── Countdown Timer ─────────────────────────────────────────────────────────

data class TimerState(
    val totalMs: Long = 0,
    val remainingMs: Long = 0,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false,
)

class ClockViewModel : ViewModel() {

    // ── Stopwatch ────────────────────────────────────────────────────────────
    private val _stopwatch = MutableStateFlow(StopwatchState())
    val stopwatch = _stopwatch.asStateFlow()

    private var swJob: Job? = null
    private var swStartTime: Long = 0
    private var swAccumulated: Long = 0

    fun startStopwatch() {
        if (_stopwatch.value.isRunning) return
        swStartTime = System.currentTimeMillis()
        _stopwatch.update { it.copy(isRunning = true) }
        swJob = viewModelScope.launch {
            while (true) {
                delay(10)
                val elapsed = swAccumulated + (System.currentTimeMillis() - swStartTime)
                _stopwatch.update { it.copy(elapsedMs = elapsed) }
            }
        }
    }

    fun pauseStopwatch() {
        swJob?.cancel()
        swAccumulated += System.currentTimeMillis() - swStartTime
        _stopwatch.update { it.copy(isRunning = false) }
    }

    fun resetStopwatch() {
        swJob?.cancel()
        swAccumulated = 0
        _stopwatch.value = StopwatchState()
    }

    fun lapStopwatch() {
        val elapsed = _stopwatch.value.elapsedMs
        _stopwatch.update { it.copy(laps = it.laps + elapsed) }
    }

    // ── Countdown Timer ───────────────────────────────────────────────────────
    private val _timer = MutableStateFlow(TimerState())
    val timer = _timer.asStateFlow()

    private var timerJob: Job? = null

    fun setTimer(hours: Int, minutes: Int, seconds: Int) {
        val ms = ((hours * 3600L) + (minutes * 60L) + seconds) * 1000L
        _timer.value = TimerState(totalMs = ms, remainingMs = ms)
    }

    fun startTimer() {
        val t = _timer.value
        if (t.isRunning || t.remainingMs <= 0) return
        _timer.update { it.copy(isRunning = true, isFinished = false) }
        timerJob = viewModelScope.launch {
            var last = System.currentTimeMillis()
            while (_timer.value.remainingMs > 0) {
                delay(50)
                val now = System.currentTimeMillis()
                val delta = now - last
                last = now
                _timer.update { s ->
                    val remaining = (s.remainingMs - delta).coerceAtLeast(0)
                    s.copy(remainingMs = remaining)
                }
            }
            _timer.update { it.copy(isRunning = false, isFinished = true) }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timer.update { it.copy(isRunning = false) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        _timer.update { it.copy(remainingMs = it.totalMs, isRunning = false, isFinished = false) }
    }
}

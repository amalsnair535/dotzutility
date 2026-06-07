package com.dotz.utility.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dotz.utility.data.AppDatabase
import com.dotz.utility.data.clock.AlarmEntity
import com.dotz.utility.service.AlarmReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

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

class ClockViewModel(application: Application) : AndroidViewModel(application) {

    private val alarmDao = AppDatabase.get(application).alarmDao()
    private val alarmManager = application.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val alarms: StateFlow<List<AlarmEntity>> = alarmDao.getAllAlarms()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addAlarm(hour: Int, minute: Int, label: String = "") {
        viewModelScope.launch {
            val alarm = AlarmEntity(hour = hour, minute = minute, label = label)
            val id = alarmDao.insert(alarm).toInt()
            if (alarm.isEnabled) {
                scheduleAlarm(alarm.copy(id = id))
            }
        }
    }

    fun toggleAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            val updated = alarm.copy(isEnabled = !alarm.isEnabled)
            alarmDao.update(updated)
            if (updated.isEnabled) {
                scheduleAlarm(updated)
            } else {
                cancelAlarm(updated)
            }
        }
    }

    fun deleteAlarm(alarm: AlarmEntity) {
        viewModelScope.launch {
            alarmDao.delete(alarm)
            cancelAlarm(alarm)
        }
    }

    private fun scheduleAlarm(alarm: AlarmEntity) {
        val intent = Intent(getApplication(), AlarmReceiver::class.java).apply {
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_ID", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Handle cases where exact alarm permission isn't granted (Android 12+)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(alarm: AlarmEntity) {
        val intent = Intent(getApplication(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            getApplication(),
            alarm.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

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
